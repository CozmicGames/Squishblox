package com.cozmicgames.game.world.processors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.PlatformData
import kotlin.math.abs

class BlockEditProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private var editingId: Int? = null
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var isResizingFlag = 0

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.EDIT && worldScene.editState == WorldScene.EditState.EDIT
    }

    private fun edit(block: WorldBlock): Boolean {
        val world = worldScene.world

        val isTopLeftHovered = Game.player.isHovered(block.minX, block.minY, block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.minY + WorldConstants.RESIZE_BORDER_SIZE)
        val isTopCenterHovered = Game.player.isHovered(block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.minY, block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.minY + WorldConstants.RESIZE_BORDER_SIZE)
        val isTopRightHovered = Game.player.isHovered(block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.minY, block.maxX, block.minY + WorldConstants.RESIZE_BORDER_SIZE)

        val isCenterLeftHovered = Game.player.isHovered(block.minX, block.minY + WorldConstants.RESIZE_BORDER_SIZE, block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.maxY - WorldConstants.RESIZE_BORDER_SIZE)
        val isCenterHovered = Game.player.isHovered(block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.minY + WorldConstants.RESIZE_BORDER_SIZE, block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.maxY - WorldConstants.RESIZE_BORDER_SIZE)
        val isCenterRightHovered = Game.player.isHovered(block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.minY + WorldConstants.RESIZE_BORDER_SIZE, block.maxX, block.maxY - WorldConstants.RESIZE_BORDER_SIZE)

        val isBottomLeftHovered = Game.player.isHovered(block.minX, block.maxY - WorldConstants.RESIZE_BORDER_SIZE, block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.maxY)
        val isBottomCenterHovered = Game.player.isHovered(block.minX + WorldConstants.RESIZE_BORDER_SIZE, block.maxY - WorldConstants.RESIZE_BORDER_SIZE, block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.maxY)
        val isBottomRightHovered = Game.player.isHovered(block.maxX - WorldConstants.RESIZE_BORDER_SIZE, block.maxY - WorldConstants.RESIZE_BORDER_SIZE, block.maxX, block.maxY)

        if (isResizingFlag == 0) {
            if (isTopLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
            else if (isTopCenterHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
            else if (isTopRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
            else if (isCenterLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
            else if (isCenterRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
            else if (isBottomLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
            else if (isBottomCenterHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
            else if (isBottomRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
            else if (isCenterHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.AllResize)
            else
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        }

        if (Game.input.isButtonJustDown(0)) {
            isResizingFlag = if (isTopLeftHovered) {
                offsetX = Game.player.inputX - block.minX
                offsetY = Game.player.inputY - block.minY
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
                1 shl 0
            } else if (isTopCenterHovered) {
                offsetX = 0.0f
                offsetY = Game.player.inputY - block.minY
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
                1 shl 1
            } else if (isTopRightHovered) {
                offsetX = Game.player.inputX - (block.maxX - WorldConstants.WORLD_CELL_SIZE)
                offsetY = Game.player.inputY - block.minY
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
                1 shl 2
            } else if (isCenterLeftHovered) {
                offsetX = Game.player.inputX - block.minX
                offsetY = 0.0f
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
                1 shl 3
            } else if (isCenterRightHovered) {
                offsetX = Game.player.inputX - (block.maxX - WorldConstants.WORLD_CELL_SIZE)
                offsetY = 0.0f
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
                1 shl 4
            } else if (isBottomLeftHovered) {
                offsetX = Game.player.inputX - block.minX
                offsetY = Game.player.inputY - (block.maxY - WorldConstants.WORLD_CELL_SIZE)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
                1 shl 5
            } else if (isBottomCenterHovered) {
                offsetX = 0.0f
                offsetY = Game.player.inputY - (block.maxY - WorldConstants.WORLD_CELL_SIZE)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
                1 shl 6
            } else if (isBottomRightHovered) {
                offsetX = Game.player.inputX - (block.maxX - WorldConstants.WORLD_CELL_SIZE)
                offsetY = Game.player.inputY - (block.maxY - WorldConstants.WORLD_CELL_SIZE)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
                1 shl 7
            } else if (isCenterHovered) {
                offsetX = Game.player.inputX - block.minX
                offsetY = Game.player.inputY - block.minY
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.AllResize)
                1 shl 8
            } else
                0

            if (isResizingFlag != 0)
                editingId = block.id
        }

        if (Game.input.isButtonJustUp(0)) {
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
            newMinX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX)
            newMinY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY)
            if (newMinX >= block.maxX) newMinX = block.maxX - WorldConstants.WORLD_CELL_SIZE
            if (newMinY >= block.maxY) newMinY = block.maxY - WorldConstants.WORLD_CELL_SIZE

            if (newMinX < block.minX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
            }

            if (newMinY < block.minY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 1) != 0) {
            newMinY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY)
            if (newMinY >= block.maxY) newMinY = block.maxY - WorldConstants.WORLD_CELL_SIZE

            if (newMinY < block.minY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 2) != 0) {
            newMaxX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX) + WorldConstants.WORLD_CELL_SIZE
            newMinY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY)
            if (newMaxX <= block.minX) newMaxX = block.minX + WorldConstants.WORLD_CELL_SIZE
            if (newMinY >= block.maxY) newMinY = block.maxY - WorldConstants.WORLD_CELL_SIZE

            if (newMaxX > block.maxX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
            }

            if (newMinY < block.minY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 3) != 0) {
            newMinX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX)
            if (newMinX >= block.maxX) newMinX = block.maxX - WorldConstants.WORLD_CELL_SIZE

            if (newMinX < block.minX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 4) != 0) {
            newMaxX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX) + WorldConstants.WORLD_CELL_SIZE
            if (newMaxX <= block.minX) newMaxX = block.minX + WorldConstants.WORLD_CELL_SIZE

            if (newMaxX > block.maxX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 5) != 0) {
            newMinX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX)
            newMaxY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY) + WorldConstants.WORLD_CELL_SIZE
            if (newMinX >= block.maxX) newMinX = block.maxX - WorldConstants.WORLD_CELL_SIZE
            if (newMaxY <= block.minY) newMaxY = block.minY + WorldConstants.WORLD_CELL_SIZE

            if (newMinX < block.minX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMinX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
            }

            if (newMaxY > block.maxY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 6) != 0) {
            newMaxY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY) + WorldConstants.WORLD_CELL_SIZE
            if (newMaxY <= block.minY) newMaxY = block.minY + WorldConstants.WORLD_CELL_SIZE

            if (newMaxY > block.maxY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 7) != 0) {
            newMaxX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX) + WorldConstants.WORLD_CELL_SIZE
            newMaxY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY) + WorldConstants.WORLD_CELL_SIZE
            if (newMaxX <= block.minX) newMaxX = block.minX + WorldConstants.WORLD_CELL_SIZE
            if (newMaxY <= block.minY) newMaxY = block.minY + WorldConstants.WORLD_CELL_SIZE

            if (newMaxX > block.maxX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
            }

            if (newMaxY > block.maxY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty())
                    newMaxY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
            }
        }

        if (isResizingFlag and (1 shl 8) != 0) {
            val width = abs(block.maxX - block.minX)
            val height = abs(block.maxY - block.minY)

            newMinX = WorldUtils.roundWorldToCellCoord(Game.player.inputX - offsetX)
            newMinY = WorldUtils.roundWorldToCellCoord(Game.player.inputY - offsetY)
            newMaxX = newMinX + width
            newMaxY = newMinY + height

            if (newMinX < block.minX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(newMinX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty()) {
                    newMinX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxX ?: -Float.MAX_VALUE }
                    newMaxX = newMinX + width
                }
            }

            if (newMinY < block.minY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(newMinY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY)) { it != block.id }
                if (collidingBlocks.isNotEmpty()) {
                    newMinY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.maxY ?: -Float.MAX_VALUE }
                    newMaxY = newMinY + height
                }
            }

            if (newMaxX > block.maxX) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(block.maxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty()) {
                    newMaxX = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }
                    newMinX = newMaxX - width
                }
            }

            if (newMaxY > block.maxY) {
                val collidingBlocks = world.getBlocks(WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.maxY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(newMaxY)) { it != block.id }
                if (collidingBlocks.isNotEmpty()) {
                    newMaxY = collidingBlocks.minOf { worldScene.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }
                    newMinY = newMaxY - height
                }
            }
        }

        if (isResizingFlag != 0) {
            if (block.minX != newMinX || block.minY != newMinY || block.maxX != newMaxX || block.maxY != newMaxY)
                block.removeData<PlatformData>()

            block.minX = newMinX
            block.minY = newMinY
            block.maxX = newMaxX
            block.maxY = newMaxY
            block.updatePhysicsBlock()

            world.updateBlock(block.id, WorldUtils.toCellCoord(block.minX), WorldUtils.toCellCoord(block.minY), WorldUtils.toCellCoord(block.maxX), WorldUtils.toCellCoord(block.maxY))
        }

        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene as? WorldScene ?: return

        if (editingId != null) {
            scene.findGameObjectsWithComponent<WorldBlock> {
                val block = it.getComponent<WorldBlock>()!!
                if (block.id == editingId) {
                    val isStillEditing = edit(block)
                    if (!isStillEditing)
                        editingId = null
                }
            }

            return
        }

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (hoveredId != null) {
            scene.findGameObjectsWithComponent<WorldBlock> {
                val block = it.getComponent<WorldBlock>()!!
                if (block.id == hoveredId)
                    edit(block)
            }
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}