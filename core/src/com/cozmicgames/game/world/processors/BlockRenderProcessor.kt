package com.cozmicgames.game.world.processors

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.components.TransformComponent
import com.cozmicgames.game.textures
import com.cozmicgames.game.world.BlockComponent
import com.cozmicgames.game.world.WorldConstants

class BlockRenderProcessor : SceneProcessor() {
    private val blockNinePatch: NinePatch

    init {
        val blockTexture = TextureRegion(Game.textures.getTexture("textures/block.png"))
        blockNinePatch = NinePatch(blockTexture, blockTexture.regionWidth / 3, blockTexture.regionWidth / 3, blockTexture.regionHeight / 3, blockTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene ?: return

        for (gameObject in scene.activeGameObjects) {
            val transformComponent = gameObject.getComponent<TransformComponent>() ?: continue

            gameObject.getComponent<BlockComponent>()?.let { blockComponent ->
                Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BLOCK_SHADOW
                    it.texture = "blank"
                    it.color = WorldConstants.SHADOW_COLOR
                    it.x = transformComponent.transform.x + WorldConstants.SHADOW_OFFSET.x
                    it.y = transformComponent.transform.y - WorldConstants.SHADOW_OFFSET.y
                    it.width = transformComponent.transform.scaleX
                    it.height = transformComponent.transform.scaleY
                }

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
        }
    }
}