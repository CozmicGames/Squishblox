package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.extensions.clamp
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.drawable
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable
import com.cozmicgames.game.input
import kotlin.math.abs

class Slider(val style: SliderStyle = SliderStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(SliderStyle::class, name)!!)

    class SliderStyle : GUIElementStyle() {
        var backgroundNormal by drawable { DefaultStyle.normalDrawable() }
        var backgroundHovered by drawable { DefaultStyle.normalDrawable() }
        var backgroundPressed by drawable { DefaultStyle.normalDrawable() }
        var backgroundDisabled by drawable { DefaultStyle.disabledDrawable() }
        var beforeHandleNormal by optionalDrawable()
        var beforeHandleHovered by optionalDrawable()
        var beforeHandlePressed by optionalDrawable()
        var beforeHandleDisabled by optionalDrawable()
        var afterHandleNormal by optionalDrawable()
        var afterHandleHovered by optionalDrawable()
        var afterHandlePressed by optionalDrawable()
        var afterHandleDisabled by optionalDrawable()
        var handleNormal by drawable { DefaultStyle.normalDrawable() }
        var handleHovered by drawable { DefaultStyle.hoveredDrawable() }
        var handlePressed by drawable { DefaultStyle.pressedDrawable() }
        var handleDisabled by drawable { DefaultStyle.disabledDrawable() }
    }

    override val usedLayers = 3

    var snapValues: FloatArray? = null
    var value = 0.0f
    var isVertical = false

    private var backgroundDrawable = style.backgroundNormal
    private var handleDrawable = style.handleNormal
    private var beforeHandleDrawable = style.beforeHandleNormal
    private var afterHandleDrawable = style.afterHandleNormal

    init {
        addListener(object : Listener {
            private var isDragged = false

            override fun onEnter(element: GUIElement) {
                if (isEnabled) {
                    backgroundDrawable = style.backgroundHovered
                    handleDrawable = style.handleHovered
                    beforeHandleDrawable = style.beforeHandleHovered
                    afterHandleDrawable = style.afterHandleHovered
                }
            }

            override fun onExit(element: GUIElement) {
                if (isEnabled) {
                    backgroundDrawable = style.backgroundNormal
                    handleDrawable = style.handleNormal
                    beforeHandleDrawable = style.beforeHandleNormal
                    afterHandleDrawable = style.afterHandleNormal
                }
            }

            override fun onEnable(element: GUIElement) {
                if (isHovered) {
                    backgroundDrawable = style.backgroundHovered
                    handleDrawable = style.handleHovered
                    beforeHandleDrawable = style.beforeHandleHovered
                    afterHandleDrawable = style.afterHandleHovered
                } else {
                    backgroundDrawable = style.backgroundNormal
                    handleDrawable = style.handleNormal
                    beforeHandleDrawable = style.beforeHandleNormal
                    afterHandleDrawable = style.afterHandleNormal
                }
            }

            override fun onDisable(element: GUIElement) {
                backgroundDrawable = style.backgroundDisabled
                handleDrawable = style.handleDisabled
                beforeHandleDrawable = style.beforeHandleDisabled
                afterHandleDrawable = style.afterHandleDisabled
                isDragged = false
            }

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (!isEnabled)
                    return

                if (Game.input.justTouchedUp) {
                    isDragged = false
                    if (isHovered) {
                        backgroundDrawable = style.backgroundHovered
                        handleDrawable = style.handleHovered
                        beforeHandleDrawable = style.beforeHandleHovered
                        afterHandleDrawable = style.afterHandleHovered
                    } else {
                        backgroundDrawable = style.backgroundNormal
                        handleDrawable = style.handleNormal
                        beforeHandleDrawable = style.beforeHandleNormal
                        afterHandleDrawable = style.afterHandleNormal
                    }
                }

                if (isHovered && Game.input.justTouchedDown) {
                    isDragged = true
                    backgroundDrawable = style.backgroundPressed
                    handleDrawable = style.handlePressed
                    beforeHandleDrawable = style.beforeHandlePressed
                    afterHandleDrawable = style.afterHandlePressed
                }

                if (isDragged) {
                    val value = if (isVertical)
                        ((Game.input.y - (y + backgroundDrawable.paddingTop) - width * 0.5f) / (height - backgroundDrawable.paddingTop - backgroundDrawable.paddingBottom - width)).clamp(0.0f, 1.0f)
                    else
                        ((Game.input.x - (x + backgroundDrawable.paddingLeft) - height * 0.5f) / (width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight - height)).clamp(0.0f, 1.0f)

                    val snapValues = this@Slider.snapValues
                    if (snapValues != null && snapValues.isNotEmpty()) {
                        var bestValue = snapValues.first()
                        var bestDifference = abs(bestValue - value)

                        snapValues.forEach {
                            val difference = abs(bestValue - value)
                            if (difference < bestDifference) {
                                bestValue = it
                                bestDifference = difference
                            }
                        }

                        this@Slider.value = bestValue
                    } else
                        this@Slider.value = value
                }
            }
        })
    }

    override fun render() {
        backgroundDrawable.drawable.draw(layer, backgroundDrawable.color, x, y, width, height)

        beforeHandleDrawable?.let {
            val beforeHandleX = x + backgroundDrawable.paddingLeft
            val beforeHandleY = y + backgroundDrawable.paddingTop
            val beforeHandleWidth = width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight
            val beforeHandleHeight = height - backgroundDrawable.paddingTop - backgroundDrawable.paddingBottom

            if (isVertical)
                it.drawable.draw(layer + 1, it.color, beforeHandleX, beforeHandleY, beforeHandleWidth, beforeHandleHeight * value.clamp(0.0f, 1.0f))
            else
                it.drawable.draw(layer + 1, it.color, beforeHandleX, beforeHandleY, beforeHandleWidth * value.clamp(0.0f, 1.0f), beforeHandleHeight)
        }

        afterHandleDrawable?.let {
            val afterHandleX: Float
            val afterHandleY: Float
            val afterHandleWidth: Float
            val afterHandleHeight: Float

            if (isVertical) {
                afterHandleX = x + backgroundDrawable.paddingLeft
                afterHandleY = y + backgroundDrawable.paddingTop + (height - backgroundDrawable.paddingTop - backgroundDrawable.paddingBottom) * value.clamp(0.0f, 1.0f)
                afterHandleWidth = width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight
                afterHandleHeight = (height - backgroundDrawable.paddingBottom - backgroundDrawable.paddingBottom) * (1.0f - value).clamp(0.0f, 1.0f)
            } else {
                afterHandleX = x + backgroundDrawable.paddingLeft + (width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight) * value.clamp(0.0f, 1.0f)
                afterHandleY = y + backgroundDrawable.paddingTop
                afterHandleWidth = (width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight) * (1.0f - value).clamp(0.0f, 1.0f)
                afterHandleHeight = height - backgroundDrawable.paddingBottom - backgroundDrawable.paddingBottom
            }

            it.drawable.draw(layer + 1, it.color, afterHandleX, afterHandleY, afterHandleWidth, afterHandleHeight)
        }

        val handleX: Float
        val handleY: Float
        val handleSize: Float

        if (isVertical) {
            handleX = x + backgroundDrawable.paddingLeft
            handleY = y + backgroundDrawable.paddingTop + (height - backgroundDrawable.paddingTop - backgroundDrawable.paddingBottom - width) * value.clamp(0.0f, 1.0f)
            handleSize = width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight
        } else {
            handleX = x + backgroundDrawable.paddingLeft + (width - backgroundDrawable.paddingLeft - backgroundDrawable.paddingRight - height) * value.clamp(0.0f, 1.0f)
            handleY = y + backgroundDrawable.paddingTop
            handleSize = height - backgroundDrawable.paddingTop - backgroundDrawable.paddingBottom
        }

        handleDrawable.drawable.draw(layer + 2, handleDrawable.color, handleX, handleY, handleSize, handleSize)
    }
}