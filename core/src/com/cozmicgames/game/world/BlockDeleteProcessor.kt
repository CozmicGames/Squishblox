package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent

class BlockDeleteProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private val blockPreviewNinePatch: NinePatch

    init {
        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return worldScene.editState == WorldScene.EditState.DELETE
    }

    override fun process(delta: Float) {
        val scene = this.scene as? WorldScene ?: return

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (hoveredId != null) {
            scene.findGameObjectsWithComponent<WorldBlockComponent> {
                val blockComponent = it.getComponent<WorldBlockComponent>()!!

                if (blockComponent.id == hoveredId) {
                    Game.graphics2d.submit<NinepatchRenderable2D> {
                        it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                        it.ninePatch = blockPreviewNinePatch
                        it.color = Color.RED
                        it.x = blockComponent.minX
                        it.y = blockComponent.minY
                        it.width = blockComponent.maxX - blockComponent.minX
                        it.height = blockComponent.maxY - blockComponent.minY
                    }

                    if (Game.input.isButtonJustDown(0))
                        scene.removeBlock(hoveredId)
                }
            }
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}