package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class VignetteRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
    uniform float u_intensity;
""".trimIndent(), """
    vec4 effect() {
        float vignette = max((distance(v_texcoord, vec2(0.5, 0.5)) - 0.25) * 1.25 * u_intensity, 0.0);
        vec4 color = texture(u_texture, v_texcoord);
        return vec4(color.rgb - vignette, color.a);
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    var intensity = 1.0f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
        shaderProgram.setUniformf("u_intensity", intensity)
    }
}