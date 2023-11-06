package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class BlurRadialRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
    float u_sampleDistance;
    float u_sampleStrength;
    vec2 u_origin;
""".trimIndent(), """
    vec4 effect() {
        const float cInvSqrtPi = 0.39894228040143267793994605993439;
        const float cInvPi = 0.31830988618379067153776752674503;
        const float cSamples[10] = float[](-0.08, -0.05, -0.03, -0.02, -0.01, 0.01, 0.02, 0.03, 0.05, 0.08);

        vec2 direction = u_origin - v_texcoord;
        float distance = sqrt(direction.x * direction.x + direction.y * direction.y);
        
        direction = direction / distance;
        vec4 color = texture(u_texture, v_texcoord);
        vec4 sum = color;
        
        for (int i = 0; i < 10; i++) {
            sum += texture(u_texture, v_texcoord + direction * cSamples[i] * u_sampleDistance);
        }
        
        sum /= 11.0;
        float t = distance * u_sampleStrength;
        t = clamp(t, 0.0, 1.0);
        
        return mix(color, sum, t);
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    var sampleDistance = 1.0f
    var sampleStrength = 2.2f
    var originX = 0.5f
    var originY = 0.5f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
        shaderProgram.setUniformf("u_size", pass.height.toFloat())
        shaderProgram.setUniformf("u_sampleDistance", sampleDistance)
        shaderProgram.setUniformf("u_sampleStrength", sampleStrength)
        shaderProgram.setUniformf("u_origin", originX, originY)
    }
}