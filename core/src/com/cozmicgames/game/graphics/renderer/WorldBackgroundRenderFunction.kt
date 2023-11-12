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

class WorldBackgroundRenderFunction : RenderFunction() {
    private class Cloud(val texture: String, var x: Float, var y: Float, var speedFactor: Float)

    private val textures = arrayOf(
        "textures/cloud_0.png",
        "textures/cloud_1.png",
        "textures/cloud_2.png",
        "textures/cloud_3.png",
        "textures/cloud_4.png"
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

    override fun render(delta: Float) {
        if (isFirstRender) {
            clouds.forEach {
                findCloudSpawnPosition(it, true)
            }
            isFirstRender = false
        }

        ScreenUtils.clear(Color.SKY)

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
}