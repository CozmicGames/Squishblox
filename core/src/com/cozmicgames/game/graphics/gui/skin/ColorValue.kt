package com.cozmicgames.game.graphics.gui.skin

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.utils.Properties

class ColorValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.COLOR

    val color = Color(Color.WHITE)

    override fun read(properties: Properties) {
        color.r = properties.getFloat("r") ?: 1.0f
        color.g = properties.getFloat("g") ?: 1.0f
        color.b = properties.getFloat("b") ?: 1.0f
        color.a = properties.getFloat("a") ?: 1.0f
    }

    override fun write(properties: Properties) {
        properties.setFloat("r", color.r)
        properties.setFloat("g", color.g)
        properties.setFloat("b", color.b)
        properties.setFloat("a", color.a)
    }
}