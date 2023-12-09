package com.cozmicgames.game.graphics.engine.rendergraph

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.HdpiUtils

object GraphicsUtils {
    private var currentFramebuffer: FrameBuffer? = null

    fun beginFramebuffer(framebuffer: FrameBuffer?) {
        if (framebuffer == currentFramebuffer)
            return

        currentFramebuffer = framebuffer
        if (framebuffer != null)
            framebuffer.begin()
        else {
            FrameBuffer.unbind()
            HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        }
    }

    fun renderToFramebuffer(framebuffer: FrameBuffer?, block: () -> Unit) {
        val previousFramebuffer = currentFramebuffer
        beginFramebuffer(framebuffer)
        block()
        beginFramebuffer(previousFramebuffer)
    }
}