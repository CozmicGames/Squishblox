package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.graphics2d.TextRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.boolean
import com.cozmicgames.game.graphics.gui.skin.color
import com.cozmicgames.game.graphics.gui.skin.font
import com.cozmicgames.game.graphics.gui.skin.int
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable
import com.cozmicgames.game.input
import com.cozmicgames.game.localization

open class TextButton(var textGetter: () -> String, private val style: TextButtonStyle = TextButtonStyle(), override var onClick: () -> Unit = {}) : ToggleButton, GUIElement() {
    constructor(skin: GUISkin, name: String = "default", textGetter: () -> String, onClick: () -> Unit = {}) : this(textGetter, skin.getStyle(TextButtonStyle::class, name)!!, onClick)
    constructor(text: String, style: TextButtonStyle = TextButtonStyle(), onClick: () -> Unit = {}) : this({ text }, style, onClick)
    constructor(skin: GUISkin, name: String = "default", text: String, onClick: () -> Unit = {}) : this(skin, name, { text }, onClick)


    class TextButtonStyle : GUIElementStyle() {
        var textColorNormal by color { it.color.set(DefaultStyle.normalTextColor) }
        var textColorHovered by color { it.color.set(DefaultStyle.hoveredTextColor) }
        var textColorPressed by color { it.color.set(DefaultStyle.pressedTextColor) }
        var textColorDisabled by color { it.color.set(DefaultStyle.disabledTextColor) }
        var font by font {}
        var align by int { it.value = Align.center }
        var isFixedTextSize by boolean { it.value = true }
        var backgroundNormal by optionalDrawable { DefaultStyle.normalDrawable() }
        var backgroundHovered by optionalDrawable { DefaultStyle.hoveredDrawable() }
        var backgroundPressed by optionalDrawable { DefaultStyle.pressedDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.disabledDrawable() }
    }

    override var isToggled = false
        set(value) {
            field = value

            if (isEnabled) {
                drawable = if (value)
                    style.backgroundPressed
                else
                    style.backgroundNormal

                textColor = if (value)
                    style.textColorPressed
                else
                    style.textColorNormal
            }
        }

    private var drawable = style.backgroundNormal
    private var textColor = style.textColorNormal

    override val usedLayers = 2

    init {
        addListener(object : Listener {
            override fun onEnter(element: GUIElement) {
                if (isEnabled) {
                    drawable = style.backgroundHovered
                    textColor = style.textColorHovered
                }
            }

            override fun onExit(element: GUIElement) {
                if (isEnabled) {
                    drawable = if (isToggled) style.backgroundPressed else style.backgroundNormal
                    textColor = if (isToggled) style.textColorPressed else style.textColorNormal
                }
            }

            override fun onEnable(element: GUIElement) {
                if (isHovered) {
                    drawable = style.backgroundHovered
                    textColor = style.textColorHovered
                } else {
                    drawable = if (isToggled) style.backgroundPressed else style.backgroundNormal
                    textColor = if (isToggled) style.textColorPressed else style.textColorNormal
                }
            }

            override fun onDisable(element: GUIElement) {
                drawable = style.backgroundDisabled
                textColor = style.textColorDisabled
            }

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (!isEnabled)
                    return

                if (isHovered)
                    if (Game.input.justTouchedDown) {
                        onClick()
                        drawable = style.backgroundPressed
                        textColor = style.textColorPressed
                    } else if (Game.input.justTouchedUp) {
                        drawable = style.backgroundHovered
                        textColor = style.textColorHovered
                    }
            }
        })
    }

    override fun render() {
        val paddingTop = drawable?.paddingTop ?: 0.0f
        val paddingLeft = drawable?.paddingLeft ?: 0.0f
        val paddingRight = drawable?.paddingRight ?: 0.0f
        val paddingBottom = drawable?.paddingBottom ?: 0.0f

        val textSize = if (style.isFixedTextSize.value)
            gui!!.textSize
        else
            height - paddingTop - paddingBottom

        drawable?.let {
            it.drawable.draw(layer, it.color, x, y, width, height)
        }

        Game.graphics2d.submit<TextRenderable2D> {
            it.layer = layer + 1
            it.font = style.font.font
            it.style = style.font.fontStyle
            it.color = textColor.color
            it.text = Game.localization[textGetter()]
            it.size = textSize
            it.wrap = false
            it.targetWidth = width - paddingLeft - paddingRight
            it.x = x + paddingLeft + when {
                Align.isCenterHorizontal(style.align.value) -> (width - paddingLeft - paddingRight - it.layout.width) * 0.5f
                Align.isRight(style.align.value) -> width - paddingLeft - paddingRight - it.layout.width
                else -> paddingLeft
            }
            it.y = y + paddingTop + when {
                Align.isCenterVertical(style.align.value) -> (height - paddingTop - paddingBottom - it.layout.height) * 0.5f
                Align.isBottom(style.align.value) -> height - paddingTop - paddingBottom - it.layout.height
                else -> paddingTop
            }
            it.layout.let { layout ->
                minContentWidth = layout.width + paddingLeft + paddingRight
                minContentHeight = layout.height + paddingTop + paddingBottom
            }
        }
    }
}