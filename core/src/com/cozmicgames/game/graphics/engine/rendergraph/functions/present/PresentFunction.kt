package com.cozmicgames.game.graphics.engine.rendergraph.functions.present

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency
import com.cozmicgames.game.graphics.engine.rendergraph.functions.FullscreenRenderFunction

abstract class PresentFunction(effectSource: String, dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
""".trimIndent(), """
    vec4 getColor() {
        return texture(u_texture, v_texcoord);
    }
    
    $effectSource
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }
}