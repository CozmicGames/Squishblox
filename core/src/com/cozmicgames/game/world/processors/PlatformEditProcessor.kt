package com.cozmicgames.game.world.processors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.common.utils.maths.distance
import com.cozmicgames.common.utils.maths.toDegrees
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.PlatformData
import com.cozmicgames.game.world.dataValues.ScaleData
import kotlin.math.atan2

class PlatformEditProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private var editingId: Int? = null
    private val pathColorClear = Color(0.2f, 1.0f, 0.2f, 0.66f)
    private val pathColorBlocked = Color(1.0f, 0.2f, 0.2f, 0.66f)
    private var pathColor = pathColorClear
    private var offsetX = 0.0f
    private var offsetY = 0.0f

    private var targetMinX = 0
    private var targetMinY = 0

    private val blockPreviewNinePatch: NinePatch
    private val pathNinePatch: NinePatch

    init {
        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)

        val pathTexture = TextureRegion(Game.textures.getTexture("textures/platform_path.png"))
        pathNinePatch = NinePatch(pathTexture, pathTexture.regionWidth / 2 - 1, pathTexture.regionWidth / 2 - 1, pathTexture.regionHeight / 2 - 1, pathTexture.regionHeight / 2 - 1)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.EDIT && worldScene.editState == WorldScene.EditState.EDIT_PLATFORM
    }

    private fun isPathClear(minX: Int, minY: Int, maxX: Int, maxY: Int, block: WorldBlock): Boolean {
        var result = true
        worldScene.world.forEachBlock(minX, minY, maxX, maxY) {
            if (it != block.id && worldScene.getBlockFromId(it) !is PlayerBlock)
                result = false
        }
        return result
    }

    private fun edit(block: WorldBlock): Boolean {
        val worldTargetMinX = WorldUtils.toWorldCoord(targetMinX)
        val worldTargetMinY = WorldUtils.toWorldCoord(targetMinY)
        val worldTargetMaxX = worldTargetMinX + block.maxX - block.minX
        val worldTargetMaxY = worldTargetMinY + block.maxY - block.minY

        val isPathClear = !(targetMinX == WorldUtils.toCellCoord(block.minX, WorldUtils.CoordRounding.FLOOR) && targetMinY == WorldUtils.toCellCoord(block.minY, WorldUtils.CoordRounding.FLOOR)) && isPathClear(targetMinX, targetMinY, WorldUtils.toCellCoord(worldTargetMaxX, WorldUtils.CoordRounding.FLOOR), WorldUtils.toCellCoord(worldTargetMaxY, WorldUtils.CoordRounding.FLOOR), block)

        pathColor = if (isPathClear)
            pathColorClear
        else
            pathColorBlocked

        if (Game.input.isButtonJustDown(0)) {
            offsetX = Game.player.inputX - block.minX
            offsetY = Game.player.inputY - block.minY
            block.removeData<PlatformData>()
            block.removeData<ScaleData>()
        }

        if (Game.input.isButtonDown(0)) {
            editingId = block.id
            targetMinX = WorldUtils.toCellCoord(Game.player.inputX - offsetX, WorldUtils.CoordRounding.ROUND)
            targetMinY = WorldUtils.toCellCoord(Game.player.inputY - offsetY, WorldUtils.CoordRounding.ROUND)
            return true
        }

        if (Game.input.isButtonJustUp(0) && isPathClear) {
            val platformData = PlatformData()
            platformData.fromMinX = block.minX
            platformData.fromMinY = block.minY
            platformData.toMinX = worldTargetMinX
            platformData.toMinY = worldTargetMinY
            block.setData(platformData)
        }

        return false
    }

    override fun process(delta: Float) {
        if (!Game.player.isCursorPositionVisible())
            return

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (editingId != null) {
            worldScene.findGameObjectsWithComponent<WorldBlock> {
                val block = it.getComponent<WorldBlock>()!!
                if (block.id == editingId) {
                    Game.graphics2d.submit<NinepatchRenderable2D> {
                        it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                        it.ninePatch = blockPreviewNinePatch
                        it.color = pathColor
                        it.x = block.minX
                        it.y = block.minY
                        it.width = block.maxX - block.minX
                        it.height = block.maxY - block.minY
                    }

                    val worldTargetMinX = WorldUtils.toWorldCoord(targetMinX)
                    val worldTargetMinY = WorldUtils.toWorldCoord(targetMinY)

                    Game.graphics2d.submit<NinepatchRenderable2D> {
                        it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                        it.ninePatch = blockPreviewNinePatch
                        it.color = pathColor
                        it.x = worldTargetMinX
                        it.y = worldTargetMinY
                        it.width = block.maxX - block.minX
                        it.height = block.maxY - block.minY
                    }

                    val movementPathLength = distance(block.minX, block.minY, worldTargetMinX, worldTargetMinY) + WorldConstants.WORLD_CELL_SIZE
                    val centerBlockX = block.minX + (block.maxX - block.minX) * 0.5f
                    val centerBlockY = block.minY + (block.maxY - block.minY) * 0.5f

                    Game.graphics2d.submit<NinepatchRenderable2D> {
                        it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                        it.ninePatch = pathNinePatch
                        it.color = pathColor
                        it.x = centerBlockX - WorldConstants.WORLD_CELL_SIZE * 0.5f
                        it.y = centerBlockY - WorldConstants.WORLD_CELL_SIZE * 0.5f
                        it.width = movementPathLength
                        it.height = WorldConstants.WORLD_CELL_SIZE
                        it.originX = WorldConstants.WORLD_CELL_SIZE * 0.5f
                        it.originY = WorldConstants.WORLD_CELL_SIZE * 0.5f
                        it.rotation = toDegrees(atan2(worldTargetMinY - block.minY, worldTargetMinX - block.minX))
                    }

                    val isStillEditing = edit(block)
                    if (!isStillEditing)
                        editingId = null
                }
            }

            return
        }

        if (hoveredId != null) {
            worldScene.findGameObjectsWithComponent<WorldBlock> {
                val block = it.getComponent<WorldBlock>()!!

                if (block.id == hoveredId) {
                    Game.graphics2d.submit<NinepatchRenderable2D> {
                        it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                        it.ninePatch = blockPreviewNinePatch
                        it.color = Color.WHITE
                        it.x = block.minX
                        it.y = block.minY
                        it.width = block.maxX - block.minX
                        it.height = block.maxY - block.minY
                    }

                    edit(block)
                }
            }
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}