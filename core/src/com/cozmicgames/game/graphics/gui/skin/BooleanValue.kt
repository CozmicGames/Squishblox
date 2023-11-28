package com.cozmicgames.game.graphics.gui.skin

import com.cozmicgames.common.utils.Properties

class BooleanValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.BOOLEAN

    var value = false

    override fun read(properties: Properties) {
        properties.getBoolean("value")?.let { value = it }
    }

    override fun write(properties: Properties) {
        properties.setBoolean("value", value)
    }
}