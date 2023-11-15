package com.cozmicgames.game.world.processors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.textures
import com.cozmicgames.game.utils.maths.distance
import com.cozmicgames.game.utils.maths.toDegrees
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.PlatformData
import kotlin.math.atan2

class PlatformRenderProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private val blockPreviewNinePatch: NinePatch
    private val pathNinePatch: NinePatch

    private val pathColor = Color(1.0f, 1.0f, 1.0f, 0.66f)

    init {
        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)

        val pathTexture = TextureRegion(Game.textures.getTexture("textures/platform_path.png"))
        pathNinePatch = NinePatch(pathTexture, pathTexture.regionWidth / 2 - 1, pathTexture.regionWidth / 2 - 1, pathTexture.regionHeight / 2 - 1, pathTexture.regionHeight / 2 - 1)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.EDIT
    }

    override fun process(delta: Float) {
        worldScene.findGameObjectsWithComponent<WorldBlock> {
            val block = it.getComponent<WorldBlock>()!!
            block.getData<PlatformData>()?.let {
                Game.graphics2d.submit<NinepatchRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                    it.ninePatch = blockPreviewNinePatch
                    it.color = pathColor
                    it.x = block.minX
                    it.y = block.minY
                    it.width = block.maxX - block.minX
                    it.height = block.maxY - block.minY
                }

                val worldTargetMinX = it.toMinX
                val worldTargetMinY = it.toMinY

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
            }
        }
    }
}