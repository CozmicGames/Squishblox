package com.cozmicgames.game.world

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

    init {
        val texture = TextureRegion(Game.textures.getTexture("textures/block_32.png"))
        blockNinePatch = NinePatch(texture, texture.regionWidth / 3, texture.regionWidth / 3, texture.regionHeight / 3, texture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene ?: return

        for (gameObject in scene.activeGameObjects) {
            val blockComponent = gameObject.getComponent<BlockComponent>() ?: continue
            val transformComponent = gameObject.getComponent<TransformComponent>() ?: continue

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