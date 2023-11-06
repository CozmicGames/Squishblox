package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class CombineRenderFunction(dependencyName0: String, dependencyIndex0: Int, dependencyName1: String, dependencyIndex1: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture0;
    uniform sampler2D u_texture1;
""".trimIndent(), """
    vec4 effect() {
        return texture(u_texture0, v_texcoord) + texture(u_texture1, v_texcoord);
    }
""".trimIndent()), Disposable {
    private val colorInput0 = colorRenderTargetDependency(dependencyName0, dependencyIndex0)
    private val colorInput1 = colorRenderTargetDependency(dependencyName1, dependencyIndex1)

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput0.texture.bind(0)
        shaderProgram.setUniformi("u_texture0", 0)
        colorInput1.texture.bind(1)
        shaderProgram.setUniformi("u_texture1", 1)
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
    }
}