package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue

object DefaultStyle {
    val normalTextColor = Color(0.2f, 0.2f, 0.2f, 1.0f)
    val hoveredTextColor = Color(0.3f, 0.3f, 0.3f, 1.0f)
    val pressedTextColor = Color(0.1f, 0.1f, 0.1f, 1.0f)
    val disabledTextColor = Color(0.25f, 0.25f, 0.25f, 0.75f)

    val normalColor = Color(0.8f, 0.8f, 0.8f, 1.0f)
    val hoveredColor = Color(0.9f, 0.9f, 0.9f, 1.0f)
    val pressedColor = Color(0.65f, 0.65f, 0.65f, 1.0f)
    val disabledColor = Color(0.75f, 0.75f, 0.75f, 0.75f)
    val highlightColor = Color.LIME
    val cursorColor = Color.WHITE
    val selectionColor = Color(0.3f, 0.3f, 0.3f, 0.2f)

    fun normalDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(normalColor)
        it.paddingLeft = 0.0f
        it.paddingRight = 0.0f
        it.paddingTop = 0.0f
        it.paddingBottom = 0.0f
    }

    fun hoveredDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(hoveredColor)
        it.paddingLeft = 0.0f
        it.paddingRight = 0.0f
        it.paddingTop = 0.0f
        it.paddingBottom = 0.0f
    }

    fun pressedDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(pressedColor)
        it.paddingLeft = 0.0f
        it.paddingRight = 0.0f
        it.paddingTop = 0.0f
        it.paddingBottom = 0.0f
    }

    fun disabledDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(disabledColor)
        it.paddingLeft = 0.0f
        it.paddingRight = 0.0f
        it.paddingTop = 0.0f
        it.paddingBottom = 0.0f
    }

    fun highlightDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(highlightColor)
        it.paddingLeft = 0.0f
        it.paddingRight = 0.0f
        it.paddingTop = 0.0f
        it.paddingBottom = 0.0f
    }

    fun textCursorDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(cursorColor)
    }

    fun textSelectionDrawable() = TextureDrawableValue().also {
        it.texture = "blank"
        it.color.set(selectionColor)
    }
}