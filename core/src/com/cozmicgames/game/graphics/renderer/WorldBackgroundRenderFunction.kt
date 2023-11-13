package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.player
import com.cozmicgames.game.textures
import com.cozmicgames.game.utils.maths.intersectRectRect
import com.cozmicgames.game.utils.maths.randomFloat
import com.cozmicgames.game.world.WorldConstants
import kotlin.math.ceil
import kotlin.math.floor

class WorldBackgroundRenderFunction : RenderFunction() {
    private class Cloud(val texture: String, var x: Float, var y: Float, var speedFactor: Float)

    private class ParallaxLayer(val texture: String, val factor: Float, val color: Color)

    private val textures = arrayOf(
        "textures/cloud_0.png",
        "textures/cloud_1.png",
        "textures/cloud_2.png",
        "textures/cloud_3.png",
        "textures/cloud_4.png"
    )

    private val layers = arrayOf(
        ParallaxLayer("textures/parallax_background.png", WorldConstants.WORLD_CELL_SIZE * 1.00f, Color(1.0f, 1.0f, 1.0f, 1.0f).fromHsv(48.0f, 0.25f, 0.7f)),
        ParallaxLayer("textures/parallax_background.png", WorldConstants.WORLD_CELL_SIZE * 0.07f, Color(1.0f, 1.0f, 1.0f, 1.0f).fromHsv(48.0f, 0.18f, 0.8f)),
        ParallaxLayer("textures/parallax_background.png", WorldConstants.WORLD_CELL_SIZE * 0.02f, Color(1.0f, 1.0f, 1.0f, 1.0f).fromHsv(48.0f, 0.09f, 1.0f)),
    )

    private val clouds = Array(WorldConstants.CLOUDS_COUNT) {
        Cloud(textures.random(), 0.0f, 0.0f, 1.0f + (randomFloat() - 0.5f) * WorldConstants.CLOUD_SPEED_SPREAD)
    }

    private var isFirstRender = true

    private fun findCloudSpawnPosition(cloud: Cloud, isInitialSpawn: Boolean) {
        fun attemptSpawn() {
            cloud.x = WorldConstants.WORLD_MIN_X + randomFloat() * (WorldConstants.WORLD_MAX_X - WorldConstants.WORLD_MIN_X)
            cloud.y = WorldConstants.CLOUD_Y + (randomFloat() - 0.5f) * WorldConstants.CLOUD_Y_SPREAD
            val texture = Game.textures.getTexture(cloud.texture)

            if (!isInitialSpawn && cloud.x in (Game.player.camera.rectangle.x..Game.player.camera.rectangle.x + Game.player.camera.rectangle.width))
                attemptSpawn()

            val cloudHeight = WorldConstants.CLOUD_SIZE * texture.regionHeight.toFloat() / texture.regionWidth.toFloat()

            for (other in clouds) {
                if (other === cloud)
                    continue

                val otherTexture = Game.textures.getTexture(other.texture)
                val otherHeight = WorldConstants.CLOUD_SIZE * otherTexture.regionHeight.toFloat() / otherTexture.regionWidth.toFloat()

                if (intersectRectRect(cloud.x, cloud.y, WorldConstants.CLOUD_SIZE, cloudHeight, other.x, other.y, WorldConstants.CLOUD_SIZE, otherHeight)) {
                    attemptSpawn()
                    break
                }
            }
        }

        attemptSpawn()
    }

    private fun drawGrid() {
        val backgroundTileWidth = 8 * WorldConstants.WORLD_CELL_SIZE
        val backgroundTileHeight = 8 * WorldConstants.WORLD_CELL_SIZE

        val numBackgroundTilesX = ceil(Game.player.camera.rectangle.width / backgroundTileWidth).toInt() + 1
        val numBackgroundTilesY = ceil(Game.player.camera.rectangle.height / backgroundTileHeight).toInt() + 1

        var backgroundTileX = floor((Game.player.camera.position.x - Game.player.camera.rectangle.width * 0.5f) / backgroundTileWidth) * backgroundTileWidth

        repeat(numBackgroundTilesX) {
            var backgroundTileY = floor((Game.player.camera.position.y - Game.player.camera.rectangle.height * 0.5f) / backgroundTileHeight) * backgroundTileHeight

            repeat(numBackgroundTilesY) {
                Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BACKGROUND
                    it.texture = "textures/grid_background_8x8.png"
                    it.x = backgroundTileX
                    it.y = backgroundTileY
                    it.width = backgroundTileWidth
                    it.height = backgroundTileHeight
                    it.color = Color.LIGHT_GRAY
                }

                backgroundTileY += backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }
    }

    fun drawClouds(delta: Float) {
        for (cloud in clouds) {
            cloud.x -= cloud.speedFactor * delta * WorldConstants.CLOUD_SPEED

            if (cloud.x + WorldConstants.CLOUD_SIZE <= WorldConstants.WORLD_MIN_X) {
                findCloudSpawnPosition(cloud, false)
                cloud.speedFactor = 1.0f + (randomFloat() * 0.5f - 0.5f) * WorldConstants.CLOUD_SPEED_SPREAD
                while (cloud.x in (Game.player.camera.rectangle.x..Game.player.camera.rectangle.x + Game.player.camera.rectangle.width))
                    cloud.x = WorldConstants.WORLD_MIN_X + randomFloat() * (WorldConstants.WORLD_MAX_X - WorldConstants.WORLD_MIN_X)
            }

            val texture = Game.textures.getTexture(cloud.texture)
            Game.graphics2d.submit<DirectRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_CLOUD_SHADOW
                it.color = WorldConstants.SHADOW_COLOR
                it.texture = cloud.texture
                it.x = cloud.x + WorldConstants.SHADOW_OFFSET.x
                it.y = cloud.y - WorldConstants.SHADOW_OFFSET.y
                it.width = WorldConstants.CLOUD_SIZE
                it.height = WorldConstants.CLOUD_SIZE * texture.regionHeight.toFloat() / texture.regionWidth.toFloat()
            }

            Game.graphics2d.submit<DirectRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_CLOUD
                it.texture = cloud.texture
                it.x = cloud.x
                it.y = cloud.y
                it.width = WorldConstants.CLOUD_SIZE
                it.height = WorldConstants.CLOUD_SIZE * texture.regionHeight.toFloat() / texture.regionWidth.toFloat()
            }
        }
    }

    private fun drawParallaxLayer(layer: ParallaxLayer, index: Int) {
        val region = Game.textures.getTexture(layer.texture)
        val camera = Game.player.camera

        val backgroundTileWidth = region.regionWidth.toFloat()
        val backgroundTileHeight = region.regionHeight.toFloat()

        val numBackgroundTilesX = ceil(camera.rectangle.width / backgroundTileWidth).toInt() + 2
        var backgroundTileX = (camera.position.x / layer.factor) % camera.rectangle.width - camera.rectangle.width * 0.5f - backgroundTileWidth

        repeat(numBackgroundTilesX) {
            Game.graphics2d.submit<DirectRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_BACKGROUND
                it.texture = layer.texture
                it.color = layer.color
                it.x = backgroundTileX
                it.y = -backgroundTileHeight * 0.5f + index * WorldConstants.WORLD_CELL_SIZE
                it.width = backgroundTileWidth
                it.height = backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }
    }

    override fun render(delta: Float) {
        if (isFirstRender) {
            clouds.forEach {
                findCloudSpawnPosition(it, true)
            }
            isFirstRender = false
        }

        ScreenUtils.clear(Color.SKY)

        for (i in layers.indices.reversed()) {
            val layer = layers[i]
            drawParallaxLayer(layer, i)
        }
        drawGrid()
        drawClouds(delta)
    }
}