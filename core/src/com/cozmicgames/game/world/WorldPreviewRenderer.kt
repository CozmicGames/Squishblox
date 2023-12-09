package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.common.utils.Updatable
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.absoluteResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat
import com.cozmicgames.game.graphics.renderer.WorldRenderer
import com.cozmicgames.game.player.PlayerCamera

class WorldPreviewRenderer(val width: Int, val height: Int) : Updatable, Disposable {
    var camera: PlayerCamera? = null

    val texture get() = renderPass.getColorTexture(0)

    private val scene = WorldScene()
    private val renderPass = RenderPass(absoluteResolution(width, height)) {
        addColorRenderTarget(TextureFormat.RGBA8_UNORM)
    }
    private val worldRenderer = WorldRenderer()

    init {
        scene.baseRenderLayer = RenderLayers.WORLD_PREVIEW_BASE_LAYER
    }

    fun setLevelData(data: String) {
        scene.initialize(data)
    }

    override fun update(delta: Float) {
        val camera = camera ?: return
        renderPass.begin()
        scene.update(delta)
        worldRenderer.render(delta, camera, scene)

        FrameBuffer.unbind()
    }

    override fun dispose() {
        renderPass.dispose()
    }
}