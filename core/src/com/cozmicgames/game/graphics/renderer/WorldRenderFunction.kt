package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency
import com.cozmicgames.game.graphics.engine.rendergraph.functions.FullscreenRenderFunction
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.player

class WorldRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction("""
    uniform sampler2D u_texture;
""".trimIndent(), """
    vec4 effect() {
        return texture(u_texture, v_texcoord);
    }
""".trimIndent()) {
    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }

    override fun render(delta: Float) {
        super.render(delta)

        Game.graphics2d.render(Game.player.camera.camera) { it in RenderLayers.WORLD_LAYER_BEGIN..RenderLayers.WORLD_LAYER_END }
    }
}