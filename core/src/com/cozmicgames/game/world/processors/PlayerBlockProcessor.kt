package com.cozmicgames.game.world.processors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Cursor
import com.cozmicgames.game.*
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectByComponent
import com.cozmicgames.game.utils.maths.intersectPointRect
import com.cozmicgames.game.world.*
import kotlin.math.abs

class PlayerBlockProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private var isEditing = false
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var isResizingFlag = 0

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.PLAY
    }

    private fun edit(block: PlayerBlock): Boolean {
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
                isEditing = true
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
            if (newMinY >= block.maxY - WorldConstants.WORLD_CELL_SIZE) newMinY = block.maxY - WorldConstants.WORLD_CELL_SIZE

            val area = (block.maxX - block.minX) * (block.maxY - block.minY)
            val targetHeight = (block.maxY - newMinY)
            val targetWidth = area / targetHeight
            if (targetWidth < WorldConstants.WORLD_CELL_SIZE) {
                val availableHeight = area / WorldConstants.WORLD_CELL_SIZE
                newMinY = block.maxY - availableHeight
            }

            if (newMinY < block.minY) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
            }

            block.adjustHeight(newMinY, newMaxY)
        }

        if (isResizingFlag and (1 shl 1) != 0) {
            newMinX = Game.player.inputX - offsetX
            if (newMinX >= block.maxX - WorldConstants.WORLD_CELL_SIZE) newMinX = block.maxX - WorldConstants.WORLD_CELL_SIZE

            val area = (block.maxX - block.minX) * (block.maxY - block.minY)
            val targetWidth = (block.maxX - newMinX)
            val targetHeight = area / targetWidth
            if (targetHeight < WorldConstants.WORLD_CELL_SIZE) {
                val availableWidth = area / WorldConstants.WORLD_CELL_SIZE
                newMinX = block.maxX - availableWidth
            }

            if (newMinX < block.minX) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
            }

            block.adjustWidth(newMinX, newMaxX)
        }

        if (isResizingFlag and (1 shl 2) != 0) {
            newMaxX = Game.player.inputX - offsetX + WorldConstants.WORLD_CELL_SIZE
            if (newMaxX <= block.minX + WorldConstants.WORLD_CELL_SIZE) newMaxX = block.minX + WorldConstants.WORLD_CELL_SIZE

            val area = (block.maxX - block.minX) * (block.maxY - block.minY)
            val targetWidth = (newMaxX - block.minX)
            val targetHeight = area / targetWidth
            if (targetHeight < WorldConstants.WORLD_CELL_SIZE) {
                val availableWidth = area / WorldConstants.WORLD_CELL_SIZE
                newMaxX = block.minX + availableWidth
            }

            if (newMaxX > block.maxX) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
            }

            block.adjustWidth(newMinX, newMaxX)
        }

        if (isResizingFlag and (1 shl 3) != 0) {
            newMaxY = Game.player.inputY - offsetY + WorldConstants.WORLD_CELL_SIZE
            if (newMaxY <= block.minY + WorldConstants.WORLD_CELL_SIZE) newMaxY = block.minY + WorldConstants.WORLD_CELL_SIZE

            val area = (block.maxX - block.minX) * (block.maxY - block.minY)
            val targetHeight = (newMaxY - block.minY)
            val targetWidth = area / targetHeight
            if (targetWidth < WorldConstants.WORLD_CELL_SIZE) {
                val availableHeight = area / WorldConstants.WORLD_CELL_SIZE
                newMaxY = block.minY + availableHeight
            }

            if (newMaxY > block.maxY) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
            }

            block.adjustHeight(newMinY, newMaxY)
        }

        return true
    }

    override fun process(delta: Float) {
        val playerBlock = worldScene.findGameObjectByComponent<PlayerBlock> { true }?.getComponent<PlayerBlock>() ?: return

        val left = Game.input.isKeyDown(Keys.A)
        val right = Game.input.isKeyDown(Keys.D)
        val jump = Game.input.isKeyDown(Keys.SPACE)
        val jumpJustPressed = Game.input.isKeyJustDown(Keys.SPACE)

        var moveAmount = 0.0f

        if (left)
            moveAmount -= 1.0f

        if (right)
            moveAmount += 1.0f

        if (abs(moveAmount) > 0.0f)
            playerBlock.isFacingRight = moveAmount > 0.0f

        if (playerBlock.controller.isOnGround && jumpJustPressed)
            Game.audio.getSound("sounds/jump.wav")?.play()

        playerBlock.controller.move(moveAmount, false, jump, delta)

        if (isEditing) {
            val isStillEditing = edit(playerBlock)
            if (!isStillEditing)
                isEditing = false
        }

        if (intersectPointRect(Game.player.inputX, Game.player.inputY, playerBlock.minX, playerBlock.minY, playerBlock.maxX, playerBlock.maxY))
            edit(playerBlock)
        else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)

        if (Game.input.isKeyDown(Keys.E))
            playerBlock.addSize(1.0f)
    }
}