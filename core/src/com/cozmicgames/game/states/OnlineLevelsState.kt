package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.common.networking.messages.LevelDataMessage
import com.cozmicgames.common.networking.messages.LevelsMessage
import com.cozmicgames.common.networking.messages.RequestLevelDataMessage
import com.cozmicgames.common.networking.messages.RequestLevelsMessage
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.engine.graphics2d.BasicRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.ImageButton
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.elements.ScrollPane
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.widgets.LeaderboardWidget
import com.cozmicgames.game.widgets.SettingsWidget

class OnlineLevelsState : InGameState() {
    private abstract inner class MenuElement : GUIElement()

    private inner class LoadingLevelElement : MenuElement() {
        override fun render() {
            val borderSize = 3.0f

            Game.graphics2d.submit<BasicRenderable2D> {
                it.layer = layer
                it.texture = "blank"
                it.color = Color.DARK_GRAY
                it.x = x - borderSize
                it.y = y - borderSize
                it.width = width + borderSize * 2
                it.height = height + borderSize * 2
            }

            Game.graphics2d.submit<BasicRenderable2D> {
                it.layer = layer + 1
                it.texture = "textures/loading_spinner.png"
                it.x = x + width * 0.5f - 64.0f * 0.5f
                it.y = y + height * 0.5f - 64.0f * 0.5f
                it.width = 64.0f
                it.height = 64.0f
                it.originX = it.width * 0.5f
                it.originY = it.height * 0.5f
                it.rotation = Game.time.sinceStart * 1000.0f * 0.5f
            }
        }
    }

    private inner class NotAvailableLevelElement : MenuElement() {
        override fun render() {
            val borderSize = 3.0f

            Game.graphics2d.submit<BasicRenderable2D> {
                it.layer = layer
                it.texture = "blank"
                it.color = Color.DARK_GRAY
                it.x = x - borderSize
                it.y = y - borderSize
                it.width = width + borderSize * 2
                it.height = height + borderSize * 2
            }

            Game.graphics2d.submit<BasicRenderable2D> {
                it.layer = layer
                it.texture = "textures/missing_level.png"
                it.x = x
                it.y = y
                it.width = width
                it.height = height
            }
        }
    }

    private inner class OnlineLevelElement(val uuid: String, val levelData: String) : MenuElement(), Disposable {
        override val additionalLayers = 1

        private val texture: Texture

        init {
            val levelProperties = Properties()
            levelProperties.read(levelData)

            val previewImage = Game.player.createPreviewImage(levelProperties)
            texture = Texture(previewImage)
            previewImage.dispose()

            val playButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/play_icon.png"
                    it.flipY = true
                    it.color.set(0x00BA2BFF)
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/play_icon.png"
                    it.flipY = true
                    it.color.set(0x00D62EFF)
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/play_icon.png"
                    it.flipY = true
                    it.color.set(0x009E22FF)
                }
                it.backgroundDisabled = null
            }) {
                val properties = Properties()
                properties.read(levelData)
                properties.getProperties("level")?.let {
                    returnState = TransitionGameState(PlayLevelState(uuid, it.write()), CircleTransition())
                }
            }
            playButton.constraints.x = center()
            playButton.constraints.y = center()
            playButton.constraints.width = aspect()
            playButton.constraints.height = relative(0.4f)
            playButton.isEnabled = false
            playButton.isSolid = false
            addElement(playButton)

            val contextButtonSpacing = 8.0f
            val contextButtonSize = 40.0f
            val contextButtonOffsetFromSide = 10.0f

            val contextButtonBackground = Panel(Panel.PanelStyle().also {
                it.background = NinepatchDrawableValue().also {
                    it.texture = "textures/level_context_button_background.png"
                    it.autoSetSplitSizes()
                    it.color.set(0xd3c781FF.toInt())
                }
            })
            contextButtonBackground.constraints.x = absolute(0.0f)
            contextButtonBackground.constraints.y = absolute(0.0f)
            contextButtonBackground.constraints.width = absolute(contextButtonSize * 2 + contextButtonSpacing + contextButtonOffsetFromSide * 2)
            contextButtonBackground.constraints.height = absolute(contextButtonSize + contextButtonOffsetFromSide * 2)

            val leaderboardButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/leaderboard_icon.png"
                    it.flipY = true
                    it.color.set(0xDDDDDDFF.toInt())
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/leaderboard_icon.png"
                    it.flipY = true
                    it.color.set(0x4A8CCEFF)
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/leaderboard_icon.png"
                    it.flipY = true
                    it.color.set(0x2E91F4FF)
                }
            }) {
                val window = Game.guis.openWindow("", 400.0f, 460.0f, false, false, false)
                val widget = LeaderboardWidget(uuid) {
                    Game.guis.closeWindow(window)
                }.also {
                    it.constraints.x = same()
                    it.constraints.y = same()
                    it.constraints.width = fill()
                    it.constraints.height = fill()
                }
                window.content.addElement(widget)
            }
            leaderboardButton.constraints.x = absolute(contextButtonOffsetFromSide + contextButtonSize + contextButtonSpacing, true)
            leaderboardButton.constraints.y = absolute(contextButtonOffsetFromSide)
            leaderboardButton.constraints.width = absolute(contextButtonSize)
            leaderboardButton.constraints.height = aspect()
            contextButtonBackground.addElement(leaderboardButton)

            val saveButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/save_icon.png"
                    it.flipY = true
                    it.color.set(0xDDDDDDFF.toInt())
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/save_icon.png"
                    it.flipY = true
                    it.color.set(0x4A8CCEFF)
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/save_icon.png"
                    it.flipY = true
                    it.color.set(0x2E91F4FF)
                }
            }) {
                Game.player.registerSavedLevel(uuid, levelProperties)
            }
            saveButton.constraints.x = absolute(contextButtonOffsetFromSide, true)
            saveButton.constraints.y = absolute(contextButtonOffsetFromSide)
            saveButton.constraints.width = absolute(contextButtonSize)
            saveButton.constraints.height = aspect()
            contextButtonBackground.addElement(saveButton)

            addElement(contextButtonBackground)

            addListener(object : Listener {
                override fun onEnter(element: GUIElement) {
                    playButton.isEnabled = true
                }

                override fun onExit(element: GUIElement) {
                    playButton.isEnabled = false
                }
            })
        }

        override fun render() {
            val borderSize = 3.0f

            Game.graphics2d.submit<BasicRenderable2D> {
                it.layer = layer
                it.texture = "blank"
                it.color = Color.DARK_GRAY
                it.x = x - borderSize
                it.y = y - borderSize
                it.width = width + borderSize * 2
                it.height = height + borderSize * 2
            }

            Game.graphics2d.submit<DirectRenderable2D> {
                it.layer = layer
                it.texture = TextureRegion(texture)
                it.x = x
                it.y = y
                it.width = width
                it.height = height
            }
        }

        override fun dispose() {
            texture.dispose()
        }
    }

    override val presentSource = Renderer2D.MENU

    private val menuElements = Array<MenuElement>(6) {
        LoadingLevelElement()
    }
    private val levelsScrollPane: ScrollPane

    init {
        Game.player.playState = PlayState.MENU

        val buttonSpacing = 10.0f
        val buttonSize = 75.0f
        val buttonOffsetFromSide = 15.0f

        val buttonBackground = object : Panel(PanelStyle().also {
            it.background = NinepatchDrawableValue().also {
                it.texture = "textures/edit_button_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xd3c781FF.toInt())
            }
        }) {
            override val additionalLayers = 10
        }
        buttonBackground.constraints.x = absolute(0.0f, true)
        buttonBackground.constraints.y = absolute(0.0f)
        buttonBackground.constraints.width = absolute(buttonSize * 4 + buttonSpacing * 3 + buttonOffsetFromSide * 2)
        buttonBackground.constraints.height = absolute(buttonSize + buttonOffsetFromSide * 2)

        val localButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/local_icon.png"
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/local_icon.png"
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/local_icon.png"
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }) {
            returnState = TransitionGameState(LocalLevelsState(), CircleTransition())
        }
        localButton.constraints.x = absolute(buttonOffsetFromSide + buttonSize * 3 + buttonSpacing * 3, true)
        localButton.constraints.y = absolute(buttonOffsetFromSide)
        localButton.constraints.width = absolute(buttonSize)
        localButton.constraints.height = aspect()
        buttonBackground.addElement(localButton)

        val reloadLevelsButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/reset_icon.png"
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/reset_icon.png"
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/reset_icon.png"
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }) {
            updateLevelElements()
        }
        reloadLevelsButton.constraints.x = absolute(buttonOffsetFromSide + buttonSize * 2 + buttonSpacing * 2, true)
        reloadLevelsButton.constraints.y = absolute(buttonOffsetFromSide)
        reloadLevelsButton.constraints.width = absolute(buttonSize)
        reloadLevelsButton.constraints.height = aspect()
        buttonBackground.addElement(reloadLevelsButton)

        val settingsButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/settings_icon.png"
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/settings_icon.png"
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/settings_icon.png"
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }) {
            val window = Game.guis.openWindow("", 400.0f, 300.0f, false, false, false)
            val widget = SettingsWidget() {
                Game.guis.closeWindow(window)
            }.also {
                it.constraints.x = same()
                it.constraints.y = same()
                it.constraints.width = fill()
                it.constraints.height = fill()
            }
            window.content.addElement(widget)
        }
        settingsButton.constraints.x = absolute(buttonOffsetFromSide + buttonSize + buttonSpacing, true)
        settingsButton.constraints.y = absolute(buttonOffsetFromSide)
        settingsButton.constraints.width = absolute(buttonSize)
        settingsButton.constraints.height = aspect()
        buttonBackground.addElement(settingsButton)

        val exitButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/exit_icon.png"
                it.flipY = true
                it.color.set(0xBF000CAA.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/exit_icon.png"
                it.flipY = true
                it.color.set(0xBF000CFF.toInt())
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/exit_icon.png"
                it.flipY = true
                it.color.set(0xA0040FFF.toInt())
            }
        }) {
            Gdx.app.exit()
        }
        exitButton.constraints.x = absolute(buttonOffsetFromSide, true)
        exitButton.constraints.y = absolute(buttonOffsetFromSide)
        exitButton.constraints.width = absolute(buttonSize)
        exitButton.constraints.height = aspect()
        buttonBackground.addElement(exitButton)

        gui.addElement(buttonBackground)

        levelsScrollPane = ScrollPane(ScrollPane.ScrollPaneStyle().also {
            it.background = null
            it.horizontalHandleNormal = null
            it.horizontalHandleHovered = null
            it.horizontalHandlePressed = null
            it.horizontalHandleDisabled = null
            it.horizontalScrollbarNormal = null
            it.horizontalScrollbarHovered = null
            it.horizontalScrollbarDisabled = null
            it.verticalScrollbarNormal = null
            it.verticalScrollbarHovered = null
            it.verticalScrollbarDisabled = null
            it.verticalHandleNormal = NinepatchDrawableValue().also {
                it.texture = "textures/scrollbar_handle.png"
                it.autoSetSplitSizes()
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.verticalHandleHovered = NinepatchDrawableValue().also {
                it.texture = "textures/scrollbar_handle.png"
                it.autoSetSplitSizes()
                it.color.set(0x4A8CCEFF)
            }
            it.verticalHandlePressed = NinepatchDrawableValue().also {
                it.texture = "textures/scrollbar_handle.png"
                it.autoSetSplitSizes()
                it.color.set(0x2E91F4FF)
            }
            it.verticalScrollbarWidth.value = 16.0f
        })
        levelsScrollPane.constraints.x = absolute(0.0f)
        levelsScrollPane.constraints.y = absolute(0.0f)
        levelsScrollPane.constraints.width = fill()
        levelsScrollPane.constraints.height = fill()
        gui.addElement(levelsScrollPane)

        val elementsPerLine = 3
        val elementsAspect = 4.0f / 3.0f
        val elementsSpacing = 25.0f

        fun getElementWidth() = (Gdx.graphics.width.toFloat() - elementsSpacing * (elementsPerLine + 1)) / elementsPerLine
        fun getElementHeight() = getElementWidth() / elementsAspect

        menuElements.forEachIndexed { index, element ->
            val x = index % elementsPerLine
            val y = index / elementsPerLine

            element.constraints.x = absolute { elementsSpacing + x * getElementWidth() + x * elementsSpacing }
            element.constraints.y = absolute { elementsSpacing + y * getElementHeight() + y * elementsSpacing }
            element.constraints.width = absolute { getElementWidth() }
            element.constraints.height = absolute { getElementHeight() }
        }

        menuElements.forEach {
            levelsScrollPane.content.addElement(it)
        }

        updateLevelElements()
    }

    private fun updateLevelElements() {
        fun changeElement(index: Int, newElement: MenuElement) {
            newElement.constraints.x = menuElements[index].constraints.x
            newElement.constraints.y = menuElements[index].constraints.y
            newElement.constraints.width = menuElements[index].constraints.width
            newElement.constraints.height = menuElements[index].constraints.height
            levelsScrollPane.content.removeElement(menuElements[index])
            levelsScrollPane.content.addElement(newElement)
            menuElements[index] = newElement
        }

        val message = RequestLevelsMessage()

        var filterIndex = 0
        menuElements.forEach {
            if (it is OnlineLevelElement) {
                message.filterLevelUuids[filterIndex] = it.uuid
                filterIndex++
            }
            if (it is Disposable)
                it.dispose()
        }

        repeat(menuElements.size) {
            changeElement(it, LoadingLevelElement())
        }

        Game.networking.listenFor<LevelsMessage>(onTimeout = {
            repeat(menuElements.size) {
                changeElement(it, NotAvailableLevelElement())
            }
        }) {
            repeat(menuElements.size) { index ->
                val uuid = it.levels[index]
                if (uuid != null) {
                    Game.networking.listenFor<LevelDataMessage>(onTimeout = {
                        changeElement(index, NotAvailableLevelElement())
                    }) {
                        if (it.uuid == uuid) {
                            changeElement(index, OnlineLevelElement(uuid, it.levelData))
                            true
                        } else
                            false

                    }
                    Game.networking.send(RequestLevelDataMessage().also {
                        it.uuid = uuid
                    })
                } else
                    changeElement(index, NotAvailableLevelElement())
            }
            true
        }
        Game.networking.send(message)
    }

    override fun end() {
        super.end()

        menuElements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}