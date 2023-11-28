package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.engine.rendergraph.colorRenderTargetDependency
import com.cozmicgames.game.graphics.engine.rendergraph.functions.FullscreenRenderFunction
import com.cozmicgames.game.world.WorldConstants
import kotlin.math.ceil
import kotlin.math.sin

class WorldRenderFunction(dependencyName: String, dependencyIndex: Int) : FullscreenRenderFunction(
    """
    uniform sampler2D u_texture;
""".trimIndent(), """
    vec4 effect() {
        return texture(u_texture, v_texcoord);
    }
""".trimIndent()
) {
    private class ParallaxLayer(val texture: String, var x: Float, var y: Float, val color: Color)

    private val layers = arrayOf(
        ParallaxLayer("textures/parallax_water.png", 64.0f, 16.0f, Color(0xBAF0FFFF.toInt())),
        ParallaxLayer("textures/parallax_water.png", 32.0f, 12.0f, Color(0x005DBDFF)),
        ParallaxLayer("textures/parallax_water.png", 0.0f, 8.0f, Color(0x228FE5FF))
    )

    private val colorInput = colorRenderTargetDependency(dependencyName, dependencyIndex)

    override fun setUniforms(shaderProgram: ShaderProgram) {
        colorInput.texture.bind(0)
        shaderProgram.setUniformi("u_texture", 0)
    }

    private fun drawParallaxLayer(layer: ParallaxLayer) {
        val region = Game.textures.getTexture(layer.texture)
        val camera = Game.player.camera

        val backgroundTileWidth = region.regionWidth.toFloat()
        val backgroundTileHeight = region.regionHeight.toFloat()

        val numBackgroundTilesX = ceil(camera.rectangle.width / backgroundTileWidth).toInt() + 2
        var backgroundTileX = camera.rectangle.x - (camera.rectangle.x + layer.x) % backgroundTileWidth - backgroundTileWidth
        if (camera.position.x < backgroundTileWidth)
            backgroundTileX -= backgroundTileWidth

        repeat(numBackgroundTilesX) {
            Game.graphics2d.submit<DirectRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_WATER
                it.texture = layer.texture
                it.color = layer.color
                it.x = backgroundTileX
                it.y = layer.y + WorldConstants.WORLD_WATER_Y
                it.width = backgroundTileWidth + 1.0f
                it.height = backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }
    }

    override fun render(delta: Float) {
        super.render(delta)

        layers.forEachIndexed { index, layer ->
            layer.x += sin(Game.time.sinceStart * 3.0f * ((index + 1).toFloat() / layers.size)) * 0.5f
            layer.y += sin(Game.time.sinceStart * ((index + 1).toFloat() / layers.size)) * 0.05f
            drawParallaxLayer(layer)
        }

        Game.graphics2d.render(Game.player.camera.camera) { it in RenderLayers.WORLD_LAYER_BEGIN..RenderLayers.WORLD_LAYER_END }
    }
}