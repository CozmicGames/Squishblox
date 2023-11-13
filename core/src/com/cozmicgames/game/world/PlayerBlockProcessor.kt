package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Cursor
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectByComponent
import com.dongbat.jbump.Collisions

class PlayerBlockProcessor(val worldScene: WorldScene) : SceneProcessor() {
    private var isEditing = false
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var isResizingFlag = 0
    private var jumpTime = 0.0f
    private var isJumping = false
    private val tempCollisions = Collisions()

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    private fun edit(block: PlayerBlockComponent): Boolean {
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
            if (newMinY >= block.maxY) newMinY = block.maxY - WorldConstants.WORLD_CELL_SIZE

            if (newMinY < block.minY) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
            }

            block.adjustHeight(newMinY, newMaxY)
        }

        if (isResizingFlag and (1 shl 1) != 0) {
            newMinX = Game.player.inputX - offsetX
            if (newMinX >= block.maxX) newMinX = block.maxX - WorldConstants.WORLD_CELL_SIZE

            if (newMinX < block.minX) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
            }

            block.adjustWidth(newMinX, newMaxX)
        }

        if (isResizingFlag and (1 shl 2) != 0) {
            newMaxX = Game.player.inputX - offsetX + WorldConstants.WORLD_CELL_SIZE
            if (newMaxX <= block.minX) newMaxX = block.minX + WorldConstants.WORLD_CELL_SIZE

            if (newMaxX > block.maxX) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
            }

            block.adjustWidth(newMinX, newMaxX)
        }

        if (isResizingFlag and (1 shl 3) != 0) {
            newMaxY = Game.player.inputY - offsetY + WorldConstants.WORLD_CELL_SIZE
            if (newMaxY <= block.minY) newMaxY = block.minY + WorldConstants.WORLD_CELL_SIZE

            if (newMaxY > block.maxY) {
                val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
            }

            block.adjustHeight(newMinY, newMaxY)
        }

        if (isResizingFlag != 0)
            worldScene.world.updateBlock(block.id, WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.maxY))

        return true
    }

    override fun process(delta: Float) {
        val playerBlockComponent = worldScene.findGameObjectByComponent<PlayerBlockComponent> { true }?.getComponent<PlayerBlockComponent>() ?: return

        playerBlockComponent.deltaX = WorldUtils.approach(playerBlockComponent.deltaX, 0.0f, WorldConstants.FRICTION * delta)
        val left = Game.input.isKeyDown(Input.Keys.A)
        val right = Game.input.isKeyDown(Input.Keys.D)
        val jump = Game.input.isKeyDown(Input.Keys.SPACE)
        val jumpJustPressed = Game.input.isKeyJustDown(Input.Keys.SPACE)

        if (right)
            playerBlockComponent.deltaX = WorldUtils.approach(playerBlockComponent.deltaX, WorldConstants.RUN_SPEED, WorldConstants.RUN_ACCELERATION * delta)
        else if (left)
            playerBlockComponent.deltaX = WorldUtils.approach(playerBlockComponent.deltaX, -WorldConstants.RUN_SPEED, WorldConstants.RUN_ACCELERATION * delta)
        else
            playerBlockComponent.deltaX = WorldUtils.approach(playerBlockComponent.deltaX, 0.0f, WorldConstants.RUN_ACCELERATION * delta)

        if (!jump)
            isJumping = false

        if (jumpJustPressed) {
            worldScene.physicsWorld.project(playerBlockComponent.id, 0.0f, -0.1f, tempCollisions)
            if (tempCollisions.size() > 0)
                isJumping = true
        }

        if (jump && isJumping && jumpTime < WorldConstants.JUMP_MAX_TIME) {
            playerBlockComponent.deltaY = WorldConstants.JUMP_SPEED
            jumpTime += delta
        }

        playerBlockComponent.deltaX += delta * playerBlockComponent.gravityX
        playerBlockComponent.deltaY += if (playerBlockComponent.deltaY < 0.0f) delta * playerBlockComponent.gravityY * WorldConstants.GRAVITY_FALLING_FACTOR else delta * playerBlockComponent.gravityY
        val amountX = delta * playerBlockComponent.deltaX
        val amountY = delta * playerBlockComponent.deltaY

        var isInAir = true
        var hitWall = false
        worldScene.physicsWorld.move(playerBlockComponent.id, amountX, amountY)?.let { result ->
            repeat(result.projectedCollisions.size()) {
                val collision = result.projectedCollisions[it]

                if (collision.other.userData is Int) {
                    //TODO: Differentiate based on the other block type (consume, hurting, ...)

                    if (collision.normal.x != 0) {
                        playerBlockComponent.deltaX = 0.0f
                        hitWall = true
                    }
                    if (collision.normal.y != 0) {
                        playerBlockComponent.deltaY = 0.0f
                        jumpTime = WorldConstants.JUMP_MAX_TIME
                        if (collision.normal.y == 1) {
                            jumpTime = 0.0f
                            isJumping = false
                            isInAir = false
                        }
                    }
                }
            }
        }

        val playerWidth = playerBlockComponent.maxX - playerBlockComponent.minX
        val playerHeight = playerBlockComponent.maxY - playerBlockComponent.minY
        val playerRect = worldScene.physicsWorld.getRect(playerBlockComponent.id)!!
        playerBlockComponent.minX = playerRect.x
        playerBlockComponent.minY = playerRect.y
        playerBlockComponent.maxX = playerBlockComponent.minX + playerWidth
        playerBlockComponent.maxY = playerBlockComponent.minY + playerHeight

        if (isEditing) {
            val isStillEditing = edit(playerBlockComponent)
            if (!isStillEditing)
                isEditing = false
        }

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (hoveredId == playerBlockComponent.id) {
            edit(playerBlockComponent)
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}