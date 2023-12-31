package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class FXAARenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
""".trimIndent(), """
    
    #define FXAA_REDUCE_MIN   (1.0/ 128.0)
    #define FXAA_REDUCE_MUL   (1.0 / 8.0)
    #define FXAA_SPAN_MAX     8.0
        
    vec4 effect() {
        vec2 resolution = textureSize(u_texture, 0).xy;

        vec2 inverseVP = 1.0 / resolution.xy;
	    vec2 v_rgbNW = v_texcoord + vec2(-1.0, -1.0) * inverseVP;
	    vec2 v_rgbNE = v_texcoord + vec2(1.0, -1.0) * inverseVP;
	    vec2 v_rgbSW = v_texcoord + vec2(-1.0, 1.0) * inverseVP;
	    vec2 v_rgbSE = v_texcoord + vec2(1.0, 1.0) * inverseVP;
	    vec2 v_rgbM = vec2(v_texcoord * inverseVP);

        vec4 color;
        vec3 rgbNW = texture(u_texture, v_rgbNW).xyz;
        vec3 rgbNE = texture(u_texture, v_rgbNE).xyz;
        vec3 rgbSW = texture(u_texture, v_rgbSW).xyz;
        vec3 rgbSE = texture(u_texture, v_rgbSE).xyz;
        vec4 texColor = texture(u_texture, v_rgbM);
        vec3 rgbM  = texColor.xyz;
        vec3 luma = vec3(0.299, 0.587, 0.114);
        float lumaNW = dot(rgbNW, luma);
        float lumaNE = dot(rgbNE, luma);
        float lumaSW = dot(rgbSW, luma);
        float lumaSE = dot(rgbSE, luma);
        float lumaM  = dot(rgbM,  luma);
        float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
        float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
        
        vec2 dir;
        dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
        dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));
        
        float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) *
                              (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
        
        float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
        dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
                  max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
                  dir * rcpDirMin)) * inverseVP;
        
        vec3 rgbA = 0.5 * (
            texture(u_texture, v_texcoord + dir * (1.0 / 3.0 - 0.5)).xyz +
            texture(u_texture, v_texcoord + dir * (2.0 / 3.0 - 0.5)).xyz);
        vec3 rgbB = rgbA * 0.5 + 0.25 * (
            texture(u_texture, v_texcoord + dir * -0.5).xyz +
            texture(u_texture, v_texcoord + dir * 0.5).xyz);
    
        float lumaB = dot(rgbB, luma);
        if ((lumaB < lumaMin) || (lumaB > lumaMax))
            color = vec4(rgbA, texColor.a);
        else
            color = vec4(rgbB, texColor.a);
        return color;
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }
}