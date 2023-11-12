package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent

class PlayerBlockProcessor(val world: World) : SceneProcessor() {
    private var editingId: Int? = null
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var isResizingFlag = 0

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    private fun edit(scene: WorldScene, block: PlayerBlockComponent): Boolean {
        val isTopHovered = Game.player.isHovered(block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.minY, block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.minY + WorldConstants.RESIZE_BORDER_SIZE)
        val isLeftHovered = Game.player.isHovered(block.minX, block.minY + WorldConstants.RESIZE_BORDER_SIZE, block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.maxY - WorldConstants.RESIZE_BORDER_SIZE)
        val isRightHovered = Game.player.isHovered(block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.minY + WorldConstants.RESIZE_BORDER_SIZE, block.maxX, block.maxY - WorldConstants.RESIZE_BORDER_SIZE)
        val isBottomHovered = Game.player.isHovered(block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.maxY - WorldConstants.RESIZE_BORDER_SIZE, block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.maxY)

        if (isResizingFlag == 0) {
            if (isTopHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
            else if (isLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
            else if (isRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
            else if (isBottomHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
            else
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        }

        if (Game.input.isButtonJustDown(0)) {
            isResizingFlag = if (isTopHovered) {
                offsetX = 0.0f
                offsetY = Game.player.inputY - block.minY
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
                1 shl 0
            } else if (isLeftHovered) {
                offsetX = Game.player.inputX - block.minX
                offsetY = 0.0f
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
                1 shl 1
            } else if (isRightHovered) {
                offsetX = Game.player.inputX - (block.maxX - WorldConstants.WORLD_CELL_SIZE)
                offsetY = 0.0f
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
                1 shl 2
            } else if (isBottomHovered) {
                offsetX = 0.0f
                offsetY = Game.player.inputY - (block.maxY - WorldConstants.WORLD_CELL_SIZE)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
                1 shl 3
            } else
                0

            if (isResizingFlag != 0)
                editingId = block.id
        }

        if (Game.input.justTouchedUp) {
            isResizingFlag = 0
            offsetX = 0.0f
            offsetY = 0.0f
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
            return false
        }

        var newMinX = block.minX
        var newMinY = block.minY
        var newMaxX = block.maxX
        var newMaxY = block.maxY

        if (isResizingFlag and (1 shl 0) != 0) {
            newMinY = Game.player.inputY - offsetY
            if (newMinY >= block.maxY) newMinY = block.maxY - WorldConstants.WORLD_CELL_SIZE

            if (newMinY < block.minY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinY = collidingBlocks.minOf { scene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
            }

            block.adjustHeight(newMinY, newMaxY)
        }

        if (isResizingFlag and (1 shl 1) != 0) {
            newMinX = Game.player.inputX - offsetX
            if (newMinX >= block.maxX) newMinX = block.maxX - WorldConstants.WORLD_CELL_SIZE

            if (newMinX < block.minX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinX = collidingBlocks.minOf { scene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
            }

            block.adjustWidth(newMinX, newMaxX)
        }

        if (isResizingFlag and (1 shl 2) != 0) {
            newMaxX = Game.player.inputX - offsetX + WorldConstants.WORLD_CELL_SIZE
            if (newMaxX <= block.minX) newMaxX = block.minX + WorldConstants.WORLD_CELL_SIZE

            if (newMaxX > block.maxX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxX = collidingBlocks.minOf { scene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
            }

            block.adjustWidth(newMinX, newMaxX)
        }

        if (isResizingFlag and (1 shl 3) != 0) {
            newMaxY = Game.player.inputY - offsetY + WorldConstants.WORLD_CELL_SIZE
            if (newMaxY <= block.minY) newMaxY = block.minY + WorldConstants.WORLD_CELL_SIZE

            if (newMaxY > block.maxY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxY = collidingBlocks.minOf { scene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
            }

            block.adjustHeight(newMinY, newMaxY)
        }

        if (isResizingFlag != 0)
            world.updateBlock(block.id, WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.maxY))

        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene as? WorldScene ?: return

        if (editingId != null) {
            scene.findGameObjectsWithComponent<PlayerBlockComponent> {
                val blockComponent = it.getComponent<PlayerBlockComponent>()!!
                if (blockComponent.id == editingId) {
                    val isStillEditing = edit(scene, blockComponent)
                    if (!isStillEditing)
                        editingId = null
                }
            }

            return
        }

        val hoveredId = world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (hoveredId != null) {
            scene.findGameObjectsWithComponent<PlayerBlockComponent> {
                val blockComponent = it.getComponent<PlayerBlockComponent>()!!
                if (blockComponent.id == hoveredId)
                    edit(scene, blockComponent)
            }
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}