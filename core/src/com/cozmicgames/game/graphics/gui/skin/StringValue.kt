package com.cozmicgames.game.graphics.gui.skin

import com.cozmicgames.common.utils.Properties

class StringValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.BOOLEAN

    var value = ""

    override fun read(properties: Properties) {
        properties.getString("value")?.let { value = it }
    }

    override fun write(properties: Properties) {
        properties.setString("value", value)
    }
}