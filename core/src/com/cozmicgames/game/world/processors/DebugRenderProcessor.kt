package com.cozmicgames.game.world.processors

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.scene.SceneProcessor

class DebugRenderProcessor : SceneProcessor() {
    private val blockPreviewNinePatch: NinePatch

    private val pathColor = Color(0.0f, 0.0f, 1.0f, 0.66f)

    private var isActive = false

    init {
        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        if (Game.input.isKeyJustDown(Keys.F12))
            isActive = !isActive

        return isActive
    }

    override fun process(delta: Float) {
        Game.physics.forEachOverlappingRectangle(Game.player.camera.rectangle.x, Game.player.camera.rectangle.y, Game.player.camera.rectangle.width, Game.player.camera.rectangle.height) { body ->
            Game.graphics2d.submit<NinepatchRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_END
                it.ninePatch = blockPreviewNinePatch
                it.color = pathColor
                it.x = body.positionX
                it.y = body.positionY
                it.width = body.scaleX
                it.height = body.scaleY
            }
        }
    }
}