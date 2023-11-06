package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class BlurHorizontalRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
    uniform float u_size;
    uniform float u_scale;
""".trimIndent(), """
    vec4 effect() {
        float blurSize = u_scale / u_size;
        vec4 sum = vec4(0.0);
        sum += texture(u_texture, vec2(v_texcoord.x - 4.0 * blurSize, v_texcoord.y)) * 0.06;
        sum += texture(u_texture, vec2(v_texcoord.x - 3.0 * blurSize, v_texcoord.y)) * 0.09;
        sum += texture(u_texture, vec2(v_texcoord.x - 2.0 * blurSize, v_texcoord.y)) * 0.12;
        sum += texture(u_texture, vec2(v_texcoord.x - blurSize, v_texcoord.y)) * 0.15;
        sum += texture(u_texture, vec2(v_texcoord.x, v_texcoord.y)) * 0.16;
        sum += texture(u_texture, vec2(v_texcoord.x + blurSize, v_texcoord.y)) * 0.15;
        sum += texture(u_texture, vec2(v_texcoord.x + 2.0 * blurSize, v_texcoord.y)) * 0.12;
        sum += texture(u_texture, vec2(v_texcoord.x + 3.0 * blurSize, v_texcoord.y)) * 0.09;
        sum += texture(u_texture, vec2(v_texcoord.x + 4.0 * blurSize, v_texcoord.y)) * 0.06;
        return sum;
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    var scale = 1.0f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
        shaderProgram.setUniformf("u_size", pass.width.toFloat())
        shaderProgram.setUniformf("u_scale", scale)
    }
}