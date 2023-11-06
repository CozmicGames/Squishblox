package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.game.graphics.gui.skin.GUISkin

open class TextField(text: String, style: TextAreaStyle = TextAreaStyle()) : TextArea(text, style) {
    constructor(skin: GUISkin, name: String = "default", text: String) : this(text, skin.getStyle(TextAreaStyle::class, name)!!)

    override val supportsMultiline = false
}