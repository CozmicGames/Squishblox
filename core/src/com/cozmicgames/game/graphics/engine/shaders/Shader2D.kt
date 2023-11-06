package com.cozmicgames.game.graphics.engine.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram

class Shader2D(shaderProgram: ShaderProgram) : Shader(shaderProgram) {
    companion object {
        private const val UNIFORMS_SECTION = "#UNIFORMS"
        private const val IMPLEMENTATION_SECTION = "#IMPLEMENTATION"

        private const val BASE_VERTEX_SHADER_SOURCE = """
            #version 150
            
            attribute vec4 a_position;
            attribute vec2 a_texCoord0;
            attribute vec4 a_color;

            varying vec4 v_position;
            varying vec2 v_texcoord;
            varying vec4 v_color;
            
            struct Vertex {
                vec4 position;
                vec2 texcoord;
                vec4 color;
            };
            
            uniform mat4 u_projTrans;
            
            #UNIFORMS

            void vertex(inout Vertex v);

            void main() {
                Vertex v;
                v.position = a_position;
                v.texcoord = a_texCoord0;
                v.color = a_color;
            
                vertex(v);
            
                gl_Position = u_projTrans * v.position;
                v_position = v.position;
                v_texcoord = v.texcoord;
                v_color = v.color;
            }    
            
            #IMPLEMENTATION
        """

        private const val BASE_FRAGMENT_SHADER_SOURCE = """
            #version 150
            
            varying vec4 v_position;
            varying vec2 v_texcoord;
            varying vec4 v_color;
            
            struct Fragment {
                vec4 position;
                vec2 texcoord;
                vec4 color;
            };
            
            uniform sampler2D u_texture;
            
            #UNIFORMS

            vec4 fragment(Fragment f);

            void main() {
                Fragment f;
                f.position = v_position;
                f.texcoord = v_texcoord;
                f.color = v_color;
                gl_FragColor = fragment(f);
            }
            
            vec4 sampleTexture(vec2 texcoord) {
                return texture(u_texture, texcoord);
            }
            
            #IMPLEMENTATION
        """

        private fun createShaderProgram(source: ShaderSource): ShaderProgram {
            var vertexSource = BASE_VERTEX_SHADER_SOURCE
            vertexSource = vertexSource.replace(UNIFORMS_SECTION, source.uniforms)
            vertexSource = vertexSource.replace(IMPLEMENTATION_SECTION, source.vertex.ifBlank { "void vertex(inout Vertex v) {}" })

            var fragmentSource = BASE_FRAGMENT_SHADER_SOURCE
            fragmentSource = fragmentSource.replace(UNIFORMS_SECTION, source.uniforms)
            fragmentSource = fragmentSource.replace(IMPLEMENTATION_SECTION, source.fragment.ifBlank { "vec4 fragment(Fragment f) { return f.color; }" })

            var shaderProgram = ShaderProgram(vertexSource, fragmentSource)

            if (!shaderProgram.isCompiled) {
                Gdx.app.log("ERROR", "Failed to compile shader:\n${shaderProgram.log}")
                shaderProgram.dispose()
                shaderProgram = SpriteBatch.createDefaultShader()
            }

            return shaderProgram
        }
    }

    constructor(source: ShaderSource) : this(createShaderProgram(source))

    constructor(source: String) : this(ShaderSource(source))
}