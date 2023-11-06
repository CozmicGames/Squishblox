package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class BloomRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
    uniform float u_threshold;
""".trimIndent(), """
    vec4 effect() {
        vec3 luminanceVector = vec3(0.2125, 0.7154, 0.0721);
        vec4 outColor = texture(u_texture, v_texcoord);

        float luminance = dot(luminanceVector, outColor.xyz);
        luminance = max(0.0, luminance - u_threshold);

        outColor.xyz *= sign(luminance);
        outColor.a = 1.0;
    
        return outColor;
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    var threshold = 0.75f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
        shaderProgram.setUniformf("u_threshold", threshold)
    }
}