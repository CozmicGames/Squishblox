package com.cozmicgames.game.graphics.gui.skin

import com.cozmicgames.game.utils.Properties

class IntValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.INT

    var value = 0

    override fun read(properties: Properties) {
        properties.getInt("value")?.let { value = it }
    }

    override fun write(properties: Properties) {
        properties.setInt("value", value)
    }
}