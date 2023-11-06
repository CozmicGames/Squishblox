package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable

open class Panel(val style: PanelStyle = PanelStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(PanelStyle::class, name)!!)

    class PanelStyle : GUIElementStyle() {
        var background by optionalDrawable { DefaultStyle.normalDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.disabledDrawable() }
    }

    override fun render() {
        val drawable = (if (isEnabled) style.background else style.backgroundDisabled)
        drawable?.drawable?.draw(layer, drawable.color, x, y, width, height)
    }
}