package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.GL_TRIANGLES
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction

abstract class FullscreenRenderFunction(effectUniforms: String, effectSource: String) : RenderFunction(), Disposable {
    private companion object {
        private val tempBuffer = BufferUtils.newIntBuffer(1)
    }

    private val shaderProgram = ShaderProgram("""
        #version 150
        
        varying vec2 v_texcoord;

        void main() {
            float x = -1.0 + float((gl_VertexID & 1) << 2);
            float y = -1.0 + float((gl_VertexID & 2) << 1);
            v_texcoord.x = (x + 1.0) * 0.5;
            v_texcoord.y = (y + 1.0) * 0.5;
            gl_Position = vec4(x, y, 0.0, 1.0);
        }
    """.trimIndent(), """
        #version 150
        
        varying vec2 v_texcoord;
        
        $effectUniforms
        
        $effectSource
    
        void main() {
             gl_FragColor = effect();
        }
    """.trimIndent())

    private val emptyVertexArrayObject: Int

    init {
        if (!shaderProgram.isCompiled)
            throw RuntimeException(shaderProgram.log)

        tempBuffer.clear()
        Gdx.gl30.glGenVertexArrays(1, tempBuffer)
        emptyVertexArrayObject = tempBuffer.get()
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(Color.CLEAR)

        shaderProgram.bind()
        setUniforms(shaderProgram)
        Gdx.gl30.glBindVertexArray(emptyVertexArrayObject)
        Gdx.gl.glDrawArrays(GL_TRIANGLES, 0, 3)
    }

    protected abstract fun setUniforms(shaderProgram: ShaderProgram)

    override fun dispose() {
        shaderProgram.dispose()
        tempBuffer.clear()
        tempBuffer.put(emptyVertexArrayObject)
        tempBuffer.flip()
        Gdx.gl30.glDeleteVertexArrays(1, tempBuffer)
    }
}