package com.cozmicgames.game.widgets

import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.*
import com.cozmicgames.game.graphics.gui.elements.List
import com.cozmicgames.game.graphics.gui.skin.*

class InfoWidget(title: String, text: String, callback: () -> Unit) : Group() {
    init {
        val titleLabelStyle = Label.LabelStyle().also {
            it.background = null
            it.isFixedTextSize.value = false
            it.wrap.value = false
            it.align.value = Align.center
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        val textLabelStyle = Label.LabelStyle().also {
            it.background = null
            it.isFixedTextSize.value = false
            it.wrap.value = false
            it.align.value = Align.center
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        val list = VerticalList<GUIElement>(List.ListStyle().also {
            it.background = NinepatchDrawableValue().also {
                it.texture = "textures/widget_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xd3c781FF.toInt())
            }
        })
        list.constraints.x = same(this)
        list.constraints.y = same(this)
        list.constraints.width = same(this)
        list.constraints.height = same(this)

        val titleLabel = Label({ title }, titleLabelStyle)
        titleLabel.constraints.width = same(this)
        titleLabel.constraints.height = absolute(60.0f)

        val textLabel = Label({ text }, textLabelStyle)
        textLabel.constraints.width = same(this)
        textLabel.constraints.height = absolute(75.0f)

        val closeButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/cancel_button.png"
                it.flipY = true
                it.color.set(0xBF000CAA.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/cancel_button.png"
                it.flipY = true
                it.color.set(0xBF000CFF.toInt())
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/cancel_button.png"
                it.flipY = true
                it.color.set(0xA0040FFF.toInt())
            }
        }) {
            callback()
        }
        closeButton.constraints.x = center()
        closeButton.constraints.y = absolute(0.0f, true)
        closeButton.constraints.width = absolute(60.0f)
        closeButton.constraints.height = aspect()

        fun padding(getAmount: () -> Float) = Group().also {
            it.constraints.width = absolute { getAmount() }
            it.constraints.height = absolute { getAmount() }
        }

        list.add(titleLabel)
        list.add(padding { 30.0f })
        list.add(textLabel)
        list.add(padding { 10.0f })
        addElement(list)

        addElement(closeButton)
    }
}