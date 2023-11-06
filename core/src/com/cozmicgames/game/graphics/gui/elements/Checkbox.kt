package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.drawable
import com.cozmicgames.game.input

class Checkbox(val style: CheckboxStyle = CheckboxStyle(), internal val onClick: (Boolean) -> Unit = {}) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default", onClick: (Boolean) -> Unit) : this(skin.getStyle(CheckboxStyle::class, name)!!, onClick)

    class CheckboxStyle : GUIElementStyle() {
        var unchecked by drawable { DefaultStyle.normalDrawable() }
        var uncheckedHovered by drawable { DefaultStyle.hoveredDrawable() }
        var uncheckedPressed by drawable { DefaultStyle.highlightDrawable() }
        var checked by drawable { DefaultStyle.highlightDrawable() }
        var checkedHovered by drawable { DefaultStyle.highlightDrawable() }
        var checkedPressed by drawable { DefaultStyle.highlightDrawable() }
        var uncheckedDisabled by drawable { DefaultStyle.disabledDrawable() }
        var checkedDisabled by drawable { DefaultStyle.disabledDrawable() }
    }

    private var drawable = style.unchecked

    var isChecked = false

    init {
        addListener(object : Listener {
            override fun onEnter(element: GUIElement) {
                if (isEnabled)
                    drawable = if (isChecked) style.checkedHovered else style.uncheckedHovered
            }

            override fun onExit(element: GUIElement) {
                if (isEnabled)
                    drawable = if (isChecked) style.checked else style.unchecked
            }

            override fun onEnable(element: GUIElement) {
                drawable = if (isHovered)
                    if (isChecked) style.checkedHovered else style.uncheckedHovered
                else
                    if (isChecked) style.checked else style.unchecked
            }

            override fun onDisable(element: GUIElement) {
                drawable = if (isChecked) style.checkedDisabled else style.uncheckedDisabled
            }

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (!isEnabled)
                    return

                if (isHovered)
                    if (Game.input.justTouchedDown) {
                        isChecked = !isChecked
                        onClick(isChecked)
                        drawable = if (isChecked) style.checkedPressed else style.uncheckedPressed
                    } else if (Game.input.justTouchedUp)
                        drawable = if (isChecked) style.checkedHovered else style.uncheckedHovered
            }
        })
    }

    override fun render() {
        drawable.drawable.draw(layer, drawable.color, x, y, width, height)
    }
}
