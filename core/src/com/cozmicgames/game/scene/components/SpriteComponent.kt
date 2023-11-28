package com.cozmicgames.game.scene.components

import com.cozmicgames.common.utils.Properties
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.AnimatedSpriteMaterial
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.SpriteMaterial
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.StaticSpriteMaterial
import com.cozmicgames.game.scene.Component

class SpriteComponent : Component() {
    var layer = 0
    var originX = 0.5f
    var originY = 0.5f
    var material: SpriteMaterial = StaticSpriteMaterial()

    override fun read(properties: Properties) {
        properties.getInt("layer")?.let { layer = it }
        val isAnimated = properties.getBoolean("isAnimated")

        material = if (isAnimated == true)
            AnimatedSpriteMaterial()
        else
            StaticSpriteMaterial()

        properties.getProperties("material")?.let {
            material.set(it)
        }

        properties.getFloat("originX")?.let { originX = it }
        properties.getFloat("originY")?.let { originY = it }
    }

    override fun write(properties: Properties) {
        properties.setInt("layer", layer)
        properties.setBoolean("isAnimated", material is AnimatedSpriteMaterial)
        properties.setProperties("material", material)
        properties.setFloat("originX", originX)
        properties.setFloat("originY", originY)
    }
}