package com.cozmicgames.game.graphics.gui.skin

import com.cozmicgames.game.utils.Properties

sealed interface GUIStyleValue {
    enum class Type {
        DRAWABLE,
        FONT,
        COLOR,
        INT,
        FLOAT,
        BOOLEAN,
        STRING
    }

    val type: Type

    fun read(properties: Properties)
    fun write(properties: Properties)
}