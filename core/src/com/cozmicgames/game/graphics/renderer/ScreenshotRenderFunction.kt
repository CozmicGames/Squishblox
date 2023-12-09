package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.functions.FullscreenRenderFunction
import com.cozmicgames.game.renderer2d

class ScreenshotRenderFunction : FullscreenRenderFunction(
    """
    uniform sampler2D u_texture;
""".trimIndent(), """
    vec4 effect() {
        return texture(u_texture, v_texcoord);
    }
""".trimIndent()
), Disposable {
    private lateinit var texture: Texture

    init {
        Game.renderer2d.takeScreenshot {
            texture = Texture(it)
        }
    }

    override fun setUniforms(shaderProgram: ShaderProgram) {
        if (!::texture.isInitialized)
            return

        texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }

    override fun dispose() {
        if (!::texture.isInitialized)
            return

        texture.dispose()
    }
}