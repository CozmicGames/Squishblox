package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.extensions.clamp
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.absolute
import com.cozmicgames.game.graphics.gui.packed
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.drawable
import com.cozmicgames.game.graphics.gui.skin.float
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable
import com.cozmicgames.game.input

class ScrollPane(private val style: ScrollPaneStyle = ScrollPaneStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(ScrollPaneStyle::class, name)!!)

    class ScrollPaneStyle : GUIElementStyle() {
        var background by optionalDrawable { DefaultStyle.highlightDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.normalDrawable() }
        var horizontalScrollbarNormal by drawable { DefaultStyle.normalDrawable() }
        var horizontalScrollbarHovered by drawable { DefaultStyle.normalDrawable() }
        var horizontalScrollbarDisabled by drawable { DefaultStyle.disabledDrawable() }
        var horizontalScrollbarHeight by float { it.value = 10.0f }
        var horizontalHandleNormal by drawable { DefaultStyle.normalDrawable() }
        var horizontalHandleHovered by drawable { DefaultStyle.hoveredDrawable() }
        var horizontalHandlePressed by drawable { DefaultStyle.pressedDrawable() }
        var horizontalHandleDisabled by drawable { DefaultStyle.disabledDrawable() }
        var verticalScrollbarNormal by drawable { DefaultStyle.normalDrawable() }
        var verticalScrollbarHovered by drawable { DefaultStyle.normalDrawable() }
        var verticalScrollbarDisabled by drawable { DefaultStyle.disabledDrawable() }
        var verticalScrollbarWidth by float { it.value = 10.0f }
        var verticalHandleNormal by drawable { DefaultStyle.normalDrawable() }
        var verticalHandleHovered by drawable { DefaultStyle.hoveredDrawable() }
        var verticalHandlePressed by drawable { DefaultStyle.pressedDrawable() }
        var verticalHandleDisabled by drawable { DefaultStyle.disabledDrawable() }
    }

    val content = Group()

    val scroll = Vector2()

    val contentWidth get() = if (needsVerticalScrollbar) availableContentWidth - style.verticalScrollbarWidth.value else availableContentWidth

    val contentHeight get() = if (needsHorizontalScrollbar) availableContentHeight - style.horizontalScrollbarHeight.value else availableContentHeight

    override val usedLayers = 3

    override val scissorRectangle = Rectangle()

    private var backgroundDrawable = style.background
    private var horizontalScrollbarDrawable = style.horizontalScrollbarNormal
    private var horizontalHandleDrawable = style.horizontalHandleNormal
    private var verticalScrollbarDrawable = style.verticalScrollbarNormal
    private var verticalHandleDrawable = style.verticalHandleNormal

    private val availableContentWidth get() = width - (backgroundDrawable?.let { it.paddingLeft + it.paddingRight } ?: 0.0f)
    private val availableContentHeight get() = height - (backgroundDrawable?.let { it.paddingTop + it.paddingBottom } ?: 0.0f)
    private val needsHorizontalScrollbar get() = content.width > availableContentWidth
    private val needsVerticalScrollbar get() = content.height > availableContentHeight
    private val horizontalScrollbarBounds = Rectangle()
    private val horizontalHandleBounds = Rectangle()
    private val verticalScrollbarBounds = Rectangle()
    private val verticalHandleBounds = Rectangle()

    init {
        content.constraints.x = absolute { (backgroundDrawable?.paddingLeft ?: 0.0f) - scroll.x }
        content.constraints.y = absolute { (backgroundDrawable?.paddingTop ?: 0.0f) - scroll.y }
        content.constraints.width = packed()
        content.constraints.height = packed()

        addElement(content)

        addListener(object : Listener {
            private var isDraggedHorizontal = false
            private var isDraggedVertical = false
            private var dragOffset = 0.0f

            private var isHorizontalScrollbarHovered = false
            private var isHorizontalHandleHovered = false
            private var isVerticalScrollbarHovered = false
            private var isVerticalHandleHovered = false

            //TODO: Implement flinging and scrolling

            override fun onEnable(element: GUIElement) {
                backgroundDrawable = style.background

                horizontalScrollbarDrawable = if (isHorizontalScrollbarHovered)
                    style.horizontalScrollbarHovered
                else
                    style.horizontalScrollbarNormal

                horizontalHandleDrawable = if (isHorizontalHandleHovered)
                    style.horizontalHandleHovered
                else
                    style.horizontalHandleNormal

                verticalScrollbarDrawable = if (isVerticalScrollbarHovered)
                    style.verticalScrollbarHovered
                else
                    style.verticalScrollbarNormal

                verticalHandleDrawable = if (isVerticalHandleHovered)
                    style.verticalHandleHovered
                else
                    style.verticalHandleNormal
            }

            override fun onDisable(element: GUIElement) {
                backgroundDrawable = style.backgroundDisabled
                horizontalScrollbarDrawable = style.horizontalScrollbarDisabled
                horizontalHandleDrawable = style.horizontalHandleDisabled
                verticalScrollbarDrawable = style.verticalScrollbarDisabled
                verticalHandleDrawable = style.verticalHandleDisabled
                isDraggedHorizontal = false
                isDraggedVertical = false
            }

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                val contentFactorX = content.width / contentWidth
                val contentFactorY = content.height / contentHeight

                horizontalScrollbarBounds.x = x + (backgroundDrawable?.paddingLeft ?: 0.0f)
                horizontalScrollbarBounds.y = y + height - (backgroundDrawable?.paddingBottom ?: 0.0f) - style.horizontalScrollbarHeight.value
                horizontalScrollbarBounds.width = contentWidth
                horizontalScrollbarBounds.height = style.horizontalScrollbarHeight.value

                horizontalHandleBounds.x = horizontalScrollbarBounds.x + scroll.x / contentFactorX
                horizontalHandleBounds.y = horizontalScrollbarBounds.y
                horizontalHandleBounds.width = horizontalScrollbarBounds.width / contentFactorX
                horizontalHandleBounds.height = horizontalScrollbarBounds.height

                verticalScrollbarBounds.x = x + width - (backgroundDrawable?.paddingRight ?: 0.0f) - style.verticalScrollbarWidth.value
                verticalScrollbarBounds.y = y + (backgroundDrawable?.paddingTop ?: 0.0f)
                verticalScrollbarBounds.width = style.verticalScrollbarWidth.value
                verticalScrollbarBounds.height = contentHeight

                verticalHandleBounds.x = verticalScrollbarBounds.x
                verticalHandleBounds.y = verticalScrollbarBounds.y + scroll.y / contentFactorY
                verticalHandleBounds.width = verticalScrollbarBounds.width
                verticalHandleBounds.height = verticalScrollbarBounds.height / contentFactorY

                val gui = gui ?: return

                isHorizontalScrollbarHovered = gui.isHovered(horizontalScrollbarBounds.x, horizontalScrollbarBounds.y, horizontalScrollbarBounds.width, horizontalScrollbarBounds.height, layer, scissorRectangle)
                isHorizontalHandleHovered = gui.isHovered(horizontalHandleBounds.x, horizontalHandleBounds.y, horizontalHandleBounds.width, horizontalHandleBounds.height, layer, scissorRectangle)
                isVerticalScrollbarHovered = gui.isHovered(verticalScrollbarBounds.x, verticalScrollbarBounds.y, verticalScrollbarBounds.width, verticalScrollbarBounds.height, layer, scissorRectangle)
                isVerticalHandleHovered = gui.isHovered(verticalHandleBounds.x, verticalHandleBounds.y, verticalHandleBounds.width, verticalHandleBounds.height, layer, scissorRectangle)

                val localInputX = gui.inputX - horizontalScrollbarBounds.x
                val localInputY = gui.inputY - verticalScrollbarBounds.y

                if (Game.input.justTouchedUp) {
                    if (isDraggedHorizontal) {
                        isDraggedHorizontal = false

                        horizontalScrollbarDrawable = if (isHorizontalScrollbarHovered)
                            style.horizontalScrollbarHovered
                        else
                            style.horizontalScrollbarNormal

                        horizontalHandleDrawable = if (isHorizontalHandleHovered)
                            style.horizontalHandleHovered
                        else
                            style.horizontalHandleNormal
                    }

                    if (isDraggedVertical) {
                        isDraggedVertical = false

                        verticalScrollbarDrawable = if (isVerticalScrollbarHovered)
                            style.verticalScrollbarHovered
                        else
                            style.verticalScrollbarNormal

                        verticalHandleDrawable = if (isVerticalHandleHovered)
                            style.verticalHandleHovered
                        else
                            style.verticalHandleNormal
                    }
                }

                if (needsHorizontalScrollbar && (isHorizontalScrollbarHovered || isHorizontalHandleHovered) && Game.input.justTouchedDown) {
                    isDraggedHorizontal = true

                    dragOffset = if (isHorizontalHandleHovered)
                        gui.inputX - horizontalHandleBounds.x
                    else {
                        if (localInputX - horizontalHandleBounds.width * 0.5f < horizontalScrollbarBounds.x)
                            localInputX - horizontalHandleBounds.width * 0.5f
                        else if (localInputX + horizontalHandleBounds.width * 0.5f > horizontalScrollbarBounds.x + horizontalScrollbarBounds.width)
                            localInputX + horizontalHandleBounds.width * 0.5f
                        else
                            0.0f
                    }

                    horizontalHandleDrawable = style.horizontalHandlePressed
                }

                if (needsVerticalScrollbar && (isVerticalScrollbarHovered || isVerticalHandleHovered) && Game.input.justTouchedDown) {
                    isDraggedVertical = true

                    dragOffset = if (isVerticalHandleHovered)
                        gui.inputY - verticalHandleBounds.y
                    else {
                        if (localInputY - verticalHandleBounds.height * 0.5f < verticalScrollbarBounds.y)
                            localInputY - verticalHandleBounds.height * 0.5f
                        else if (localInputY + verticalHandleBounds.height * 0.5f > verticalScrollbarBounds.y + verticalScrollbarBounds.height)
                            localInputY + verticalHandleBounds.height * 0.5f
                        else
                            0.0f
                    }

                    verticalHandleDrawable = style.verticalHandlePressed
                }

                if (isDraggedHorizontal)
                    scroll.x = (localInputX - dragOffset) * contentFactorX

                if (isDraggedVertical)
                    scroll.y = (localInputY - dragOffset) * contentFactorY

                if (isHovered) {
                    scroll.x += gui.scrollX
                    scroll.y -= gui.scrollY
                }

                scroll.x = scroll.x.clamp(0.0f, content.width - contentWidth)
                scroll.y = scroll.y.clamp(0.0f, content.height - contentHeight)
            }
        })
    }

    override fun submitElementsToUpdate() {
        fun submitElement(element: GUIElement) {
            gui?.submitElementToUpdate(element)

            element.children.forEach {
                submitElement(it)
            }
        }

        gui?.submitElementToUpdate(this)

        scissorRectangle.x = x
        scissorRectangle.y = y
        scissorRectangle.width = contentWidth
        scissorRectangle.height = contentHeight
        gui?.pushScissor(scissorRectangle)
        submitElement(content)
        gui?.popScissor()
    }

    override fun render() {
        backgroundDrawable?.let {
            it.drawable.draw(layer, it.color, x, y, width, height)
        }

        if (needsHorizontalScrollbar) {
            horizontalScrollbarDrawable.drawable.draw(layer + 1, horizontalScrollbarDrawable.color, horizontalScrollbarBounds.x, horizontalScrollbarBounds.y, horizontalScrollbarBounds.width, horizontalScrollbarBounds.height)
            horizontalHandleDrawable.drawable.draw(layer + 2, horizontalHandleDrawable.color, horizontalHandleBounds.x, horizontalHandleBounds.y, horizontalHandleBounds.width, horizontalHandleBounds.height)
        }

        if (needsVerticalScrollbar) {
            verticalScrollbarDrawable.drawable.draw(layer + 1, verticalScrollbarDrawable.color, verticalScrollbarBounds.x, verticalScrollbarBounds.y, verticalScrollbarBounds.width, verticalScrollbarBounds.height)
            verticalHandleDrawable.drawable.draw(layer + 2, verticalHandleDrawable.color, verticalHandleBounds.x, verticalHandleBounds.y, verticalHandleBounds.width, verticalHandleBounds.height)
        }

        scissorRectangle.x = x
        scissorRectangle.y = y
        scissorRectangle.width = contentWidth
        scissorRectangle.height = contentHeight
    }
}