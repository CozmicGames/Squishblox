package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency

class CutOutRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
    vec4 u_borderColor;
    vec4 u_cutout;
""".trimIndent(), """
    float rectangle(vec2 uv, float x, float y, float width, float height) {
	    float t = 0.0;
	    if ((uv.x > x - width * 0.5) && (uv.x < x + width * 0.5) && (uv.y > y - height * 0.5) && (uv.y < y + height * 0.5))
	        t = 1.0;
	    return t;
    }
    
    vec4 effect() {
        float inside = rectangle(v_texcoord, u_cutout.x, u_cutout.y, u_cutout.z, u_cutout.w);
        vec4 outColor = texture(u_texture, v_texcoord);
        if (inside != 1.0)
            outColor *= uBorderColor;
        return outColor;
    }
""".trimIndent()), Disposable {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    var borderColor = Color.BLACK
    var cutOutX = 0.5f
    var cutOutY = 0.5f
    var cutOutWidth = 1.0f
    var cutOutHeight = 1.0f

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
        shaderProgram.setUniformf("u_borderColor", borderColor)
        shaderProgram.setUniformf("u_cutout", cutOutX, cutOutY, cutOutWidth, cutOutHeight)
    }
}