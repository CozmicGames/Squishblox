package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.common.utils.extensions.clamp
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.drawable

class Progressbar(val style: ProgressbarStyle=ProgressbarStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(ProgressbarStyle::class, name)!!)

    class ProgressbarStyle : GUIElementStyle() {
        var background by drawable { DefaultStyle.normalDrawable() }
        var foreground by drawable { DefaultStyle.highlightDrawable() }
        var backgroundDisabled by drawable { DefaultStyle.disabledDrawable() }
        var foregroundDisabled by drawable { DefaultStyle.disabledDrawable() }
    }

    var progress = 0.0f
    var isVertical = false

    override val usedLayers = 2

    override fun render() {
        val backgroundDrawable = (if (isEnabled) style.background else style.backgroundDisabled)
        val foregroundDrawable = (if (isEnabled) style.foreground else style.foregroundDisabled)

        backgroundDrawable.drawable.draw(layer, backgroundDrawable.color, x, y, width, height)

        val foregroundX = x + backgroundDrawable.paddingLeft
        val foregroundY = y + backgroundDrawable.paddingTop
        val foregroundWidth = width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight
        val foregroundHeight = height - backgroundDrawable.paddingTop - backgroundDrawable.paddingBottom

        if (isVertical)
            foregroundDrawable.drawable.draw(layer, foregroundDrawable.color, foregroundX, foregroundY, foregroundWidth, foregroundHeight * progress.clamp(0.0f, 1.0f))
        else
            foregroundDrawable.drawable.draw(layer, foregroundDrawable.color, foregroundX, foregroundY, foregroundWidth * progress.clamp(0.0f, 1.0f), foregroundHeight)
    }
}