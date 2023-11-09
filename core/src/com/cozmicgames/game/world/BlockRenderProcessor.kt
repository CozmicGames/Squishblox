package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.components.TransformComponent
import com.cozmicgames.game.textures

class BlockRenderProcessor : SceneProcessor() {
    private val blockNinePatch: NinePatch
    private val blockPreviewNinePatch: NinePatch

    init {
        val blockTexture = TextureRegion(Game.textures.getTexture("textures/block_32.png"))
        blockNinePatch = NinePatch(blockTexture, blockTexture.regionWidth / 3, blockTexture.regionWidth / 3, blockTexture.regionHeight / 3, blockTexture.regionHeight / 3)

        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene ?: return

        for (gameObject in scene.activeGameObjects) {
            val transformComponent = gameObject.getComponent<TransformComponent>() ?: continue

            gameObject.getComponent<BlockComponent>()?.let { blockComponent ->
                Game.graphics2d.submit<NinepatchRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BLOCK
                    it.ninePatch = blockNinePatch
                    it.color = blockComponent.color
                    it.x = transformComponent.transform.x
                    it.y = transformComponent.transform.y
                    it.width = transformComponent.transform.scaleX
                    it.height = transformComponent.transform.scaleY
                }
            }

            gameObject.getComponent<BlockPreviewComponent>()?.let { blockPreviewComponent ->
                Game.graphics2d.submit<NinepatchRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                    it.ninePatch = blockPreviewNinePatch
                    it.color = if (blockPreviewComponent.isBuildable) Color.WHITE else Color.RED
                    it.x = transformComponent.transform.x
                    it.y = transformComponent.transform.y
                    it.width = transformComponent.transform.scaleX
                    it.height = transformComponent.transform.scaleY
                }
            }
        }
    }
}