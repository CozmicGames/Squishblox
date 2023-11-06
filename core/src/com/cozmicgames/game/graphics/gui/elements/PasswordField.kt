package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.game.graphics.gui.skin.GUISkin

class PasswordField(text: String, style: TextAreaStyle = TextAreaStyle()) : TextField(text, style) {
    constructor(skin: GUISkin, name: String = "default", text: String) : this(text, skin.getStyle(TextAreaStyle::class, name)!!)

    override var replacementChar: Char? = '*'
}