package com.cozmicgames.game.graphics.gui.skin

import com.cozmicgames.game.utils.Properties

class FloatValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.FLOAT

    var value = 0.0f

    override fun read(properties: Properties) {
        properties.getFloat("value")?.let { value = it }
    }

    override fun write(properties: Properties) {
        properties.setFloat("value", value)
    }
}