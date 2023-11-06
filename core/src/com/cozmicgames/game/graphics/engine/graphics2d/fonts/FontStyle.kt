package com.cozmicgames.game.graphics.engine.graphics2d.fonts

import com.badlogic.gdx.graphics.Color

class FontStyle {
    var fontWeight = 0.05f
    var isShadowClipped = false
    var shadowColor = Color.CLEAR
    var shadowOffset = 0.0f
    var shadowSmoothing = 0.0f
    var innerShadowColor = Color.CLEAR
    var innerShadowRange = 0.0f
}

val DefaultFontStyle = FontStyle()
