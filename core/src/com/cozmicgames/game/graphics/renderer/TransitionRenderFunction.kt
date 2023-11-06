package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency
import com.cozmicgames.game.graphics.engine.rendergraph.functions.FullscreenRenderFunction
import com.cozmicgames.game.states.Transition

class TransitionRenderFunction(private val transition: Transition) : FullscreenRenderFunction("""
    uniform sampler2D u_textureFrom;
    uniform sampler2D u_textureTo;
    uniform float u_progress;
    ${transition.uniforms}
""".trimIndent(), """
    vec4 getFromColor(vec2 uv) {
        return texture(u_textureFrom, uv);
    }
    
    vec4 getToColor(vec2 uv) {
        return texture(u_textureTo, uv);
    }
    
    ${transition.source}
""".trimIndent()) {
    private val colorInputFrom = colorRenderTargetDependency(Renderer2D.TRANSITION_FROM, 0)
    private val colorInputTo = colorRenderTargetDependency(Renderer2D.TRANSITION_TO, 0)

    var progress = 0.0f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformi("u_textureFrom", 0)
        shaderProgram.setUniformi("u_textureTo", 1)
        shaderProgram.setUniformf("u_progress", progress)
        colorInputFrom.texture.bind(0)
        colorInputTo.texture.bind(1)
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
        transition.setUniforms(shaderProgram)
    }
}