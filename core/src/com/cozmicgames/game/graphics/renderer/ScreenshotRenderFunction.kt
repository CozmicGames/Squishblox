package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.functions.FullscreenRenderFunction

class ScreenshotRenderFunction : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
""".trimIndent(), """
    vec4 effect() {
        return texture(u_texture, v_texcoord);
    }
""".trimIndent()), Disposable {
    private val texture: Texture

    init {
        val pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun setUniforms(shaderProgram: ShaderProgram) {
        texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }

    override fun dispose() {
        texture.dispose()
    }
}