package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable

class StyledImage(val style: StyledImageStyle = StyledImageStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(StyledImageStyle::class, name)!!)

    class StyledImageStyle : GUIElementStyle() {
        var background by optionalDrawable { DefaultStyle.normalDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.disabledDrawable() }
    }

    private var drawable = style.background

    init {
        addListener(object : Listener {
            override fun onEnable(element: GUIElement) {
                drawable = style.background
            }

            override fun onDisable(element: GUIElement) {
                drawable = style.backgroundDisabled
            }
        })
    }

    override fun render() {
        drawable?.let { it.drawable.draw(layer, it.color, x, y, width, height) }
    }
}