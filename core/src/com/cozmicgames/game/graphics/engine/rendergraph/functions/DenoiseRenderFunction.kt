package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class DenoiseRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
    uniform float u_threshold;
""".trimIndent(), """
    vec4 effect() {
        const float cInvSqrtPi = 0.39894228040143267793994605993439;
        const float cInvPi = 0.31830988618379067153776752674503;

        float radius = round(u_sigma * u_sigmaCoefficient);
        float radiusSquared = radius * radius;
        
        float invSigmaQx2 = 0.5 / (u_sigma * u_sigma);
        float invSigmaQx2PI = cInvPi * invSigmaQx2;
        
        float invThresholdSqx2 = 0.5 / (u_threshold * u_threshold);
        float invThresholdSqrt2PI = cInvSqrtPi / u_threshold;
        
        vec4 centrPx = texture(u_texture, v_texcoord); 
        
        float zBuff = 0.0;
        vec4 aBuff = vec4(0.0);
        vec2 size = vec2(textureSize(u_texture, 0));

        vec2 d;
        for (d.x = -radius; d.x <= radius; d.x++) {
            float pt = sqrt(radiusSquared - d.x * d.x);
            for (d.y = -pt; d.y <= pt; d.y++) {
                float blurFactor = exp(-dot(d, d) * invSigmaQx2) * invSigmaQx2PI;
        
                vec4 walkPx =  texture(u_texture, v_texcoord + d / size);
                vec4 dC = walkPx - centrPx;
                float deltaFactor = exp(-dot(dC, dC) * invThresholdSqx2) * invThresholdSqrt2PI * blurFactor;
        
                zBuff += deltaFactor;
                aBuff += deltaFactor * walkPx;
            }
        }
        
        return aBuff / zBuff;
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    var sigma = 5.0f
    var threshold = 0.1f
    var sigmaCoefficient = 2.0f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
        shaderProgram.setUniformf("u_sigma", sigma)
        shaderProgram.setUniformf("u_threshold", threshold)
        shaderProgram.setUniformf("u_sigmaCoefficient", sigmaCoefficient)
    }
}