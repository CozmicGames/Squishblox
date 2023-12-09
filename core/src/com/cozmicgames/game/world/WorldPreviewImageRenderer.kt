package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.absoluteResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat
import com.cozmicgames.game.graphics.renderer.WorldRenderer
import com.cozmicgames.game.player.PlayerCamera

class WorldPreviewImageRenderer(val width: Int, val height: Int) : Disposable {
    private val scene = WorldScene()
    private val renderPass = RenderPass(absoluteResolution(width, height)) {
        addColorRenderTarget(TextureFormat.RGBA8_UNORM)
    }
    private val worldRenderer = WorldRenderer()
    private val framebufferHandleBuffer = BufferUtils.newIntBuffer(1)
    private val viewportBuffer = BufferUtils.newIntBuffer(4)

    fun renderToImage(camera: PlayerCamera, data: String, indexInFrame: Int): Pixmap {
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, framebufferHandleBuffer)
        val previousFramebuffer = framebufferHandleBuffer.get()
        framebufferHandleBuffer.flip()

        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, viewportBuffer)
        val viewportX = viewportBuffer[0]
        val viewportY = viewportBuffer[1]
        val viewportWidth = viewportBuffer[2]
        val viewportHeight = viewportBuffer[3]

        renderPass.begin()
        scene.initialize(data)
        scene.baseRenderLayer = RenderLayers.WORLD_PREVIEW_BASE_LAYER + RenderLayers.WORLD_PREVIEW_BASE_LAYER_INCREMENT * indexInFrame
        scene.update(0.0f)
        worldRenderer.render(0.0f, camera, scene)

        val image = Pixmap.createFromFrameBuffer(0, 0, renderPass.width, renderPass.height)

        Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, previousFramebuffer)
        Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight)

        return image
    }

    override fun dispose() {
        renderPass.dispose()
    }
}