package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class SharpenRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
""".trimIndent(), """
    vec4 effect() {
        float amount = 0.2;
        vec2 texSize = textureSize(u_texture, 0).xy;

        float neighbor = -amount;
        float center = amount * 4.0 + 1.0;

        vec3 color = texture(u_texture, vec2(gl_FragCoord.x + 0, gl_FragCoord.y + 1) / texSize).rgb * neighbor
                   + texture(u_texture, vec2(gl_FragCoord.x - 1, gl_FragCoord.y) / texSize).rgb * neighbor
                   + texture(u_texture, vec2(gl_FragCoord.x + 0, gl_FragCoord.y) / texSize).rgb * center
                   + texture(u_texture, vec2(gl_FragCoord.x + 1, gl_FragCoord.y) / texSize).rgb * neighbor
                   + texture(u_texture, vec2(gl_FragCoord.x + 0, gl_FragCoord.y - 1) / texSize).rgb * neighbor;

        return vec4(color, texture(u_texture, v_texcoord).a);
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }
}