package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.graphics.engine.graphics2d.TextRenderable2D
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.*
import com.cozmicgames.game.localization
import kotlin.math.min

class Label(var textGetter: () -> String, val style: LabelStyle = LabelStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default", textGetter: () -> String) : this(textGetter, skin.getStyle(LabelStyle::class, name)!!)

    constructor(text: String, style: LabelStyle = LabelStyle()) : this({ text }, style)

    constructor(skin: GUISkin, name: String = "default", text: String) : this(skin, name, { text })

    class LabelStyle : GUIElementStyle() {
        var textColor by color { it.color.set(DefaultStyle.normalTextColor) }
        var textColorDisabled by optionalColor { it.color.set(DefaultStyle.normalTextColor) }
        var font by font {}
        var align by int { it.value = Align.center }
        var wrap by boolean { it.value = true }
        var isFixedTextSize by boolean { it.value = true }
        var fixedTextSize by optionalFloat()
        var background by optionalDrawable { DefaultStyle.normalDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.disabledDrawable() }
    }

    var text
        get() = textGetter()
        set(value) {
            textGetter = { value }
        }

    private var textColor: ColorValue? = style.textColor
    private var drawable = style.background

    override val usedLayers = 2

    init {
        addListener(object : Listener {
            override fun onEnable(element: GUIElement) {
                drawable = style.background
                textColor = style.textColor
            }

            override fun onDisable(element: GUIElement) {
                drawable = style.backgroundDisabled
                textColor = style.textColorDisabled
            }
        })
    }

    override fun render() {
        val paddingTop = drawable?.paddingTop ?: 0.0f
        val paddingLeft = drawable?.paddingLeft ?: 0.0f
        val paddingRight = drawable?.paddingRight ?: 0.0f
        val paddingBottom = drawable?.paddingBottom ?: 0.0f

        drawable?.let {
            it.drawable.draw(layer, it.color, x, y, width, height)
        }

        textColor?.let { color ->
            Game.graphics2d.submit<TextRenderable2D> {
                it.font = style.font.font
                it.style = style.font.fontStyle
                it.text = Game.localization[textGetter()]
                it.wrap = style.wrap.value
                it.size = style.fixedTextSize?.value ?: gui!!.textSize

                if (!style.isFixedTextSize.value && width > 0.0f && height > 0.0f) {
                    val occupiedWidth = it.layout.width
                    val occupiedHeight = it.layout.height

                    val availableWidth = width - paddingLeft - paddingRight
                    val availableHeight = height - paddingTop - paddingBottom

                    val widthFactor = availableWidth / occupiedWidth
                    val heightFactor = availableHeight / occupiedHeight

                    it.size *= min(widthFactor, heightFactor)
                }

                it.layer = layer + 1
                it.color = color.color
                if (style.wrap.value)
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
}