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
import com.cozmicgames.game.world.*

class BlockRenderProcessor : SceneProcessor() {
    private val blockNinePatch: NinePatch
    private val playerBlockNinePatch: NinePatch

    init {
        val blockTexture = TextureRegion(Game.textures.getTexture("textures/block.png"))
        blockNinePatch = NinePatch(blockTexture, blockTexture.regionWidth / 3, blockTexture.regionWidth / 3, blockTexture.regionHeight / 3, blockTexture.regionHeight / 3)

        val playerBlockTexture = TextureRegion(Game.textures.getTexture("textures/player.png"))
        playerBlockNinePatch = NinePatch(playerBlockTexture, playerBlockTexture.regionWidth / 3, playerBlockTexture.regionWidth / 3, playerBlockTexture.regionHeight / 3, playerBlockTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene ?: return

        for (gameObject in scene.activeGameObjects) {
            val transformComponent = gameObject.getComponent<TransformComponent>() ?: continue

            gameObject.getComponent<BlockComponent>()?.let { block ->
                Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BLOCK_SHADOW
                    it.texture = "blank"
                    it.color = WorldConstants.SHADOW_COLOR
                    it.x = transformComponent.transform.x + WorldConstants.SHADOW_OFFSET.x
                    it.y = transformComponent.transform.y - WorldConstants.SHADOW_OFFSET.y
                    it.width = transformComponent.transform.scaleX
                    it.height = transformComponent.transform.scaleY
                }

                when (block) {
                    is WorldBlock -> {
                        Game.graphics2d.submit<NinepatchRenderable2D> {
                            it.layer = RenderLayers.WORLD_LAYER_BLOCK
                            it.ninePatch = blockNinePatch
                            it.color = block.color
                            it.x = transformComponent.transform.x
                            it.y = transformComponent.transform.y
                            it.width = transformComponent.transform.scaleX
                            it.height = transformComponent.transform.scaleY
                        }
                    }

                    is EntityBlock -> {
                        Game.graphics2d.submit<NinepatchRenderable2D> {
                            it.layer = RenderLayers.WORLD_LAYER_BLOCK
                            it.ninePatch = playerBlockNinePatch
                            it.color = block.color
                            it.x = transformComponent.transform.x
                            it.y = transformComponent.transform.y
                            it.width = transformComponent.transform.scaleX
                            it.height = transformComponent.transform.scaleY
                        }

                        if (!block.isBlinking) {
                            val playerEyesTexture = TextureRegion(Game.textures.getTexture("textures/player_eyes.png"))
                            Game.graphics2d.submit<DirectRenderable2D> {
                                it.layer = RenderLayers.WORLD_LAYER_BLOCK_FOREGROUND
                                it.texture = "textures/player_eyes.png"
                                it.color = block.color
                                it.x = transformComponent.transform.x + (transformComponent.transform.scaleX - playerEyesTexture.regionWidth) * 0.5f
                                it.y = transformComponent.transform.y + (transformComponent.transform.scaleY - playerEyesTexture.regionHeight) * 0.5f
                                it.width = playerEyesTexture.regionWidth.toFloat()
                                it.height = playerEyesTexture.regionHeight.toFloat()
                            }
                        }
                    }
                }
            }
        }
    }
}
