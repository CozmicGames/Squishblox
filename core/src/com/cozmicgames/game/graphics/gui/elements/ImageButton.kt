package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable
import com.cozmicgames.game.input

open class ImageButton(val style: ImageButtonStyle = ImageButtonStyle(), override var onClick: () -> Unit = {}) : ToggleButton, GUIElement() {
    constructor(skin: GUISkin, name: String = "default", onClick: () -> Unit = {}) : this(skin.getStyle(ImageButtonStyle::class, name)!!, onClick)

    class ImageButtonStyle : GUIElementStyle() {
        var backgroundNormal by optionalDrawable { TextureDrawableValue() }
        var backgroundHovered by optionalDrawable { TextureDrawableValue() }
        var backgroundPressed by optionalDrawable { TextureDrawableValue() }
        var backgroundDisabled by optionalDrawable { TextureDrawableValue() }
    }

    override var isToggled = false
        set(value) {
            field = value

            if (isEnabled) {
                drawable = if (value)
                    style.backgroundPressed
                else
                    style.backgroundNormal
            }
        }

    private var drawable = style.backgroundNormal

    init {
        addListener(object : Listener {
            override fun onEnter(element: GUIElement) {
                if (isEnabled)
                    drawable = style.backgroundHovered
            }

            override fun onExit(element: GUIElement) {
                if (isEnabled)
                    drawable = if (isToggled) style.backgroundPressed else style.backgroundNormal
            }

            override fun onEnable(element: GUIElement) {
                drawable = if (isHovered)
                    style.backgroundHovered
                else if (isToggled)
                    style.backgroundPressed
                else
                    style.backgroundNormal
            }

            override fun onDisable(element: GUIElement) {
                drawable = style.backgroundDisabled
            }

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (!isEnabled)
                    return

                if (isHovered)
                    if (Game.input.justTouchedDown) {
                        onClick()
                        drawable = style.backgroundPressed
                    } else if (Game.input.justTouchedUp)
                        drawable = style.backgroundHovered
            }
        })
    }

    override fun render() {
        drawable?.let {
            it.drawable.draw(layer, it.color, x, y, width, height)
        }
    }
}
