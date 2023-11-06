package com.cozmicgames.game.graphics.gui.skin

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.graphics.engine.graphics2d.fonts.FontStyle

class FontValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.FONT

    var font = ""
    var fontWeight = 0.0f
    var isShadowClipped = false
    val shadowColor = Color(Color.CLEAR)
    var shadowOffset = 0.0f
    var shadowSmoothing = 0.0f
    val innerShadowColor = Color(Color.CLEAR)
    var innerShadowRange = 0.0f

    val fontStyle = FontStyle()
        get() {
            field.fontWeight = fontWeight
            field.isShadowClipped = isShadowClipped
            field.shadowColor = shadowColor
            field.shadowOffset = shadowOffset
            field.shadowSmoothing = shadowSmoothing
            field.innerShadowColor = innerShadowColor
            field.innerShadowRange = innerShadowRange
            return field
        }

    override fun read(properties: Properties) {
        font = properties.getString("name") ?: ""
        fontWeight = properties.getFloat("fontWeight") ?: 0.0f
        isShadowClipped = properties.getBoolean("isShadowClipped") ?: false
        properties.getProperties("shadowColor")?.let {
            shadowColor.r = it.getFloat("r") ?: 1.0f
            shadowColor.g = it.getFloat("g") ?: 1.0f
            shadowColor.b = it.getFloat("b") ?: 1.0f
            shadowColor.a = it.getFloat("a") ?: 1.0f
        } ?: shadowColor.set(Color.CLEAR)
        shadowOffset = properties.getFloat("shadowOffset") ?: 0.0f
        shadowSmoothing = properties.getFloat("shadowSmoothing") ?: 0.0f
        properties.getProperties("innerShadowColor")?.let {
            innerShadowColor.r = it.getFloat("r") ?: 1.0f
            innerShadowColor.g = it.getFloat("g") ?: 1.0f
            innerShadowColor.b = it.getFloat("b") ?: 1.0f
            innerShadowColor.a = it.getFloat("a") ?: 1.0f
        } ?: innerShadowColor.set(Color.CLEAR)
        innerShadowRange = properties.getFloat("innerShadowRange") ?: 0.0f
    }

    override fun write(properties: Properties) {
        properties.setString("name", font)
        properties.setFloat("fontWeight", fontWeight)
        properties.setBoolean("isShadowClipped", isShadowClipped)
        properties.setProperties("shadowColor", Properties().also {
            it.setFloat("r", shadowColor.r)
            it.setFloat("g", shadowColor.g)
            it.setFloat("b", shadowColor.b)
            it.setFloat("a", shadowColor.a)
        })
        properties.setFloat("shadowOffset", shadowOffset)
        properties.setFloat("shadowSmoothing", shadowSmoothing)
        properties.setProperties("innerShadowColor", Properties().also {
            it.setFloat("r", innerShadowColor.r)
            it.setFloat("g", innerShadowColor.g)
            it.setFloat("b", innerShadowColor.b)
            it.setFloat("a", innerShadowColor.a)
        })
        properties.setFloat("innerShadowRange", innerShadowRange)
    }
}