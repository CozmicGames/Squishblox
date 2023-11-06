package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.game.utils.extensions.clamp
import com.cozmicgames.game.graphics.gui.elements.Group
import com.cozmicgames.game.graphics.gui.elements.Label
import com.cozmicgames.game.graphics.gui.elements.ScrollPane
import com.cozmicgames.game.graphics.gui.elements.TextButton
import com.cozmicgames.game.graphics.gui.skin.ColorDrawableValue
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.guis
import com.cozmicgames.game.input
import kotlin.math.max

class GUIWindow(var title: String = "", var isResizable: Boolean = true, val hasTitleBar: Boolean = true, isScrollable: Boolean = true, windowLayer: Int) : Group() {
    companion object {
        private const val RESIZE_BORDER_SIZE = 5.0f

        private const val BACKGROUND_COLOR = 0x555555FF
        private const val BACKGROUND_COLOR_DISABLED = 0x55555599
        private const val BUTTON_COLOR_NORMAL = 0x565656FF
        private const val BUTTON_COLOR_HOVERED = 0x454545FF
        private const val BUTTON_COLOR_PRESSED = 0x676767FF
        private const val BUTTON_COLOR_DISABLED = 0x56565699
        private const val FONT_COLOR = 0xDDDDDDFF.toInt()
        private const val FONT_COLOR_DISABLED = 0xDDDDDD99.toInt()
        private const val SELECTION_COLOR = 0xDDDDDD66.toInt()

        private val skin = GUISkin()

        init {
            skin.addStyle(Label.LabelStyle::class, "window").also {
                it.background = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR) }
                it.backgroundDisabled = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR_DISABLED) }
                it.textColor.color.set(FONT_COLOR)
                it.textColorDisabled?.color?.set(FONT_COLOR_DISABLED)
                it.align.value = Align.left
                it.wrap.value = false
            }

            skin.addStyle(ScrollPane.ScrollPaneStyle::class, "window").also {
                it.background = NinepatchDrawableValue().also {
                    it.texture = "textures/scroll_background.png"
                    it.color.set(BACKGROUND_COLOR)
                    it.topSplitHeight = 1
                    it.bottomSplitHeight = 1
                    it.leftSplitWidth = 1
                    it.rightSplitWidth = 1
                }
                it.backgroundDisabled = NinepatchDrawableValue().also {
                    it.texture = "textures/scroll_background.png"
                    it.color.set(BACKGROUND_COLOR_DISABLED)
                    it.topSplitHeight = 1
                    it.bottomSplitHeight = 1
                    it.leftSplitWidth = 1
                    it.rightSplitWidth = 1
                }
                it.horizontalScrollbarNormal = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR) }
                it.horizontalScrollbarHovered = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR) }
                it.horizontalScrollbarDisabled = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR_DISABLED) }
                it.verticalScrollbarNormal = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR) }
                it.verticalScrollbarHovered = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR) }
                it.verticalScrollbarDisabled = ColorDrawableValue().also { it.color.set(BACKGROUND_COLOR_DISABLED) }
                it.horizontalHandleNormal = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_NORMAL) }
                it.horizontalHandleHovered = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_HOVERED) }
                it.horizontalHandlePressed = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_PRESSED) }
                it.horizontalHandleDisabled = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_DISABLED) }
                it.verticalHandleNormal = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_NORMAL) }
                it.verticalHandleHovered = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_HOVERED) }
                it.verticalHandlePressed = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_PRESSED) }
                it.verticalHandleDisabled = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_DISABLED) }
            }

            skin.addStyle(TextButton.TextButtonStyle::class, "window").also {
                it.textColorNormal.color.set(FONT_COLOR)
                it.textColorHovered.color.set(FONT_COLOR)
                it.textColorPressed.color.set(FONT_COLOR)
                it.textColorDisabled.color.set(FONT_COLOR_DISABLED)
                it.backgroundNormal = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_NORMAL) }
                it.backgroundHovered = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_HOVERED) }
                it.backgroundPressed = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_PRESSED) }
                it.backgroundDisabled = ColorDrawableValue().also { it.color.set(BUTTON_COLOR_DISABLED) }
            }
        }
    }

    override val additionalLayers = windowLayer

    var windowX = 0.0f
        set(value) {
            field = value.clamp(-RESIZE_BORDER_SIZE, Gdx.graphics.width - windowWidth + RESIZE_BORDER_SIZE)
        }

    var windowY = 0.0f
        set(value) {
            field = value.clamp(-RESIZE_BORDER_SIZE, Gdx.graphics.height - windowHeight + RESIZE_BORDER_SIZE)
        }

    var windowWidth = 200.0f
        set(value) {
            field = max(RESIZE_BORDER_SIZE, value)
        }

    var windowHeight = 200.0f
        set(value) {
            field = max(RESIZE_BORDER_SIZE, value)
        }

    val content = Group()

    var onClose: () -> Unit = {}

    init {
        constraints.x = absolute { windowX + if (isResizable) RESIZE_BORDER_SIZE else 0.0f }
        constraints.y = absolute { windowY + if (isResizable) RESIZE_BORDER_SIZE else 0.0f }
        constraints.width = absolute { windowWidth - if (isResizable) RESIZE_BORDER_SIZE * 2 else 0.0f }
        constraints.height = absolute { windowHeight - if (isResizable) RESIZE_BORDER_SIZE * 2 else 0.0f }

        val scrollPane = ScrollPane(skin, "window")

        val titleLabel = Label(skin, "window") { title }

        val closeButton = TextButton(skin, "window", "Close") {
            Game.guis.closeWindow(this)
        }

        closeButton.constraints.x = absolute { titleLabel.width }
        closeButton.constraints.y = absolute { 0.0f }
        closeButton.constraints.width = packed()
        closeButton.constraints.height = packed()

        titleLabel.constraints.x = absolute { 0.0f }
        titleLabel.constraints.y = absolute { 0.0f }
        titleLabel.constraints.width = same(this) - same(closeButton)
        titleLabel.constraints.height = packed()

        if (isScrollable) {
            scrollPane.constraints.x = absolute { 0.0f }
            scrollPane.constraints.y = absolute { if (hasTitleBar) titleLabel.height else 0.0f }
            scrollPane.constraints.width = same(this)
            scrollPane.constraints.height = same(this) - absolute { if (hasTitleBar) titleLabel.height else 0.0f }

            content.constraints.x = same(scrollPane)
            content.constraints.y = same(scrollPane)
            content.constraints.width = same(scrollPane)
            content.constraints.height = same(scrollPane)

            scrollPane.content.addElement(content)

            addElement(scrollPane)
        } else {
            content.constraints.x = absolute { 0.0f }
            content.constraints.y = absolute { if (hasTitleBar) titleLabel.height else 0.0f }
            content.constraints.width = same(this)
            content.constraints.height = same(this) - absolute { if (hasTitleBar) titleLabel.height else 0.0f }

            addElement(content)
        }


        if (hasTitleBar) {
            addElement(titleLabel)
            addElement(closeButton)
        }

        addListener(object : Listener {
            private var offsetX = 0.0f
            private var offsetY = 0.0f
            private var isDragged = false
            private var isResizingFlag = 0

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                val isTopLeftHovered = gui?.isHovered(windowX, windowY, RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, windowLayer, scissorRectangle) == true
                val isTopCenterHovered = gui?.isHovered(windowX + RESIZE_BORDER_SIZE, windowY, windowWidth - RESIZE_BORDER_SIZE * 2, RESIZE_BORDER_SIZE, windowLayer, scissorRectangle) == true
                val isTopRightHovered = gui?.isHovered(windowX + windowWidth - RESIZE_BORDER_SIZE, windowY, RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, windowLayer, scissorRectangle) == true

                val isCenterLeftHovered = gui?.isHovered(windowX, windowY + RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, windowHeight - RESIZE_BORDER_SIZE * 2, windowLayer, scissorRectangle) == true
                val isCenterRightHovered = gui?.isHovered(windowX + windowWidth - RESIZE_BORDER_SIZE, windowY + RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, windowHeight - RESIZE_BORDER_SIZE * 2, windowLayer, scissorRectangle) == true

                val isBottomLeftHovered = gui?.isHovered(windowX, windowY + windowHeight - RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, windowLayer, scissorRectangle) == true
                val isBottomCenterHovered = gui?.isHovered(windowX + RESIZE_BORDER_SIZE, windowY + windowHeight - RESIZE_BORDER_SIZE, windowWidth - RESIZE_BORDER_SIZE * 2, RESIZE_BORDER_SIZE, windowLayer, scissorRectangle) == true
                val isBottomRightHovered = gui?.isHovered(windowX + windowWidth - RESIZE_BORDER_SIZE, windowY + windowHeight - RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, RESIZE_BORDER_SIZE, windowLayer, scissorRectangle) == true

                if (isResizable && isResizingFlag == 0) {
                    if (isTopLeftHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.NWSEResize)
                    else if (isTopCenterHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.VerticalResize)
                    else if (isTopRightHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.NESWResize)
                    else if (isCenterLeftHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.HorizontalResize)
                    else if (isCenterRightHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.HorizontalResize)
                    else if (isBottomLeftHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.NESWResize)
                    else if (isBottomCenterHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.VerticalResize)
                    else if (isBottomRightHovered)
                        Gdx.graphics.setSystemCursor(SystemCursor.NWSEResize)
                    else
                        Gdx.graphics.setSystemCursor(SystemCursor.Arrow)
                }

                if (Game.input.justTouchedDown) {
                    if (isResizable)
                        isResizingFlag = if (isTopLeftHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.NWSEResize)
                            offsetX = Game.input.x - windowX
                            offsetY = Game.input.y - windowY
                            1 shl 0
                        } else if (isTopCenterHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.VerticalResize)
                            offsetX = Game.input.x - windowX
                            offsetY = Game.input.y - windowY
                            1 shl 1
                        } else if (isTopRightHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.NESWResize)
                            offsetX = Game.input.x - windowX - windowWidth
                            offsetY = Game.input.y - windowY
                            1 shl 2
                        } else if (isCenterLeftHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.HorizontalResize)
                            offsetX = Game.input.x - windowX
                            offsetY = Game.input.y - windowY
                            1 shl 3
                        } else if (isCenterRightHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.HorizontalResize)
                            offsetX = Game.input.x - windowX - windowWidth
                            offsetY = Game.input.y - windowY
                            1 shl 4
                        } else if (isBottomLeftHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.NESWResize)
                            offsetX = Game.input.x - windowX
                            offsetY = Game.input.y - windowY - windowHeight
                            1 shl 5
                        } else if (isBottomCenterHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.VerticalResize)
                            offsetX = Game.input.x - windowX
                            offsetY = Game.input.y - windowY - windowHeight
                            1 shl 6
                        } else if (isBottomRightHovered) {
                            Gdx.graphics.setSystemCursor(SystemCursor.NWSEResize)
                            offsetX = Game.input.x - windowX - windowWidth
                            offsetY = Game.input.y - windowY - windowHeight
                            1 shl 7
                        } else
                            0
                    else
                        isResizingFlag = 0

                    if (titleLabel.isHovered) {
                        isDragged = true
                        offsetX = Game.input.x - windowX
                        offsetY = Game.input.y - windowY
                    }
                }

                if (Game.input.justTouchedUp) {
                    isDragged = false
                    isResizingFlag = 0
                    Gdx.graphics.setSystemCursor(SystemCursor.Arrow)
                }

                if (isDragged) {
                    windowX = Game.input.x - offsetX
                    windowY = Game.input.y - offsetY
                }

                if (isResizingFlag and (1 shl 0) != 0) {
                    val newX = Game.input.x - offsetX
                    val newY = Game.input.y - offsetY
                    windowWidth -= newX - windowX
                    windowHeight -= newY - windowY
                    windowX = newX
                    windowY = newY
                }

                if (isResizingFlag and (1 shl 1) != 0) {
                    val newY = Game.input.y - offsetY
                    windowHeight -= newY - windowY
                    windowY = newY
                }

                if (isResizingFlag and (1 shl 2) != 0) {
                    val newX = Game.input.x - offsetX
                    val newY = Game.input.y - offsetY
                    windowWidth = newX - windowX
                    windowHeight -= newY - windowY
                    windowY = newY
                }

                if (isResizingFlag and (1 shl 3) != 0) {
                    val newX = Game.input.x - offsetX
                    windowWidth -= newX - windowX
                    windowX = newX
                }

                if (isResizingFlag and (1 shl 4) != 0) {
                    val newX = Game.input.x - offsetX
                    windowWidth = newX - windowX
                }

                if (isResizingFlag and (1 shl 5) != 0) {
                    val newX = Game.input.x - offsetX
                    val newY = Game.input.y - offsetY
                    windowWidth -= newX - windowX
                    windowHeight = newY - windowY
                    windowX = newX
                }

                if (isResizingFlag and (1 shl 6) != 0) {
                    val newY = Game.input.y - offsetY
                    windowHeight = newY - windowY
                }

                if (isResizingFlag and (1 shl 7) != 0) {
                    val newX = Game.input.x - offsetX
                    val newY = Game.input.y - offsetY
                    windowWidth = newX - windowX
                    windowHeight = newY - windowY
                }
            }
        })
    }
}