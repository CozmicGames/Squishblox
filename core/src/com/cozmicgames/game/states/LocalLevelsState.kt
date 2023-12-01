package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.common.networking.messages.ConfirmSubmitLevelMessage
import com.cozmicgames.common.networking.messages.SubmitLevelMessage
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.engine.graphics2d.BasicRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.*
import com.cozmicgames.game.graphics.gui.skin.*
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.widgets.InfoWidget
import com.cozmicgames.game.widgets.LeaderboardWidget
import com.cozmicgames.game.widgets.SettingsWidget
import com.cozmicgames.game.world.WorldScene

class LocalLevelsState : InGameState() {
    companion object {
        private fun loadLevelData(uuid: String, isLocal: Boolean): String {
            val file = Gdx.files.local("levels/${if (isLocal) "local" else "saved"}/$uuid/level.json")
            if (!file.exists()) {
                val properties = Properties()
                val scene = WorldScene()
                scene.initialize()
                scene.write(properties)
                return properties.write()
            }

            return file.readString()
        }

        private fun loadLevelPreviewImage(uuid: String, isLocal: Boolean): Pixmap {
            val file = Gdx.files.local("levels/${if (isLocal) "local" else "saved"}/$uuid/level.png")
            if (!file.exists())
                return Pixmap(Gdx.files.internal("textures/missing_level.png"))
            return Pixmap(file)
        }
    }

    private abstract class MenuElement : GUIElement()

    private abstract inner class LevelElement(val levelData: String, previewImage: Pixmap) : Disposable, MenuElement() {
        override val additionalLayers = 1

        private val texture: Texture

        init {
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
                onPlay()
            }
            playButton.constraints.x = center()
            playButton.constraints.y = center()
            playButton.constraints.width = aspect()
            playButton.constraints.height = relative(0.4f)
            playButton.isEnabled = false
            playButton.isSolid = false
            addElement(playButton)

            addListener(object : Listener {
                override fun onEnter(element: GUIElement) {
                    playButton.isEnabled = true
                }

                override fun onExit(element: GUIElement) {
                    playButton.isEnabled = false
                }
            })
        }

        protected abstract fun onPlay()

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

    private inner class TutorialLevelElement : LevelElement(Gdx.files.internal("tutorial/level.json").readString(), Pixmap(Gdx.files.internal("tutorial/level.png"))) {
        init {
            val contextInfoSize = 40.0f
            val contextInfoOffsetFromSide = 10.0f

            val contextInfoLabel = Label("Tutorial", Label.LabelStyle().also {
                it.background = null
                it.align.value = Align.center
                it.font = FontValue().also {
                    it.font = "fonts/VinaSans-Regular.fnt"
                }
                it.isFixedTextSize = BooleanValue().also {
                    it.value = true
                }
                it.fixedTextSize = FloatValue().also {
                    it.value = 32.0f
                }
                it.textColor = ColorValue().also {
                    it.color.set(Color.WHITE)
                }
            })
            contextInfoLabel.constraints.x = absolute(contextInfoOffsetFromSide, true)
            contextInfoLabel.constraints.y = absolute(contextInfoOffsetFromSide)
            contextInfoLabel.constraints.width = absolute(100.0f)
            contextInfoLabel.constraints.height = absolute(contextInfoSize)

            val contextButtonBackground = Panel(Panel.PanelStyle().also {
                it.background = NinepatchDrawableValue().also {
                    it.texture = "textures/level_context_button_background.png"
                    it.autoSetSplitSizes()
                    it.color.set(0xd3c781FF.toInt())
                }
            })
            contextButtonBackground.constraints.x = absolute(0.0f)
            contextButtonBackground.constraints.y = absolute(0.0f)
            contextButtonBackground.constraints.width = absolute { contextInfoLabel.width + contextInfoOffsetFromSide * 2 }
            contextButtonBackground.constraints.height = absolute(contextInfoSize + contextInfoOffsetFromSide * 2)

            contextButtonBackground.addElement(contextInfoLabel)
            addElement(contextButtonBackground)
        }

        override fun onPlay() {
            returnState = TransitionGameState(PlayLevelState("", levelData), CircleTransition())
        }
    }

    private inner class LocalLevelElement(val uuid: String) : LevelElement(loadLevelData(uuid, true), loadLevelPreviewImage(uuid, true)) {
        init {
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
            contextButtonBackground.constraints.width = absolute(contextButtonSize * 3 + contextButtonSpacing * 2 + contextButtonOffsetFromSide * 2)
            contextButtonBackground.constraints.height = absolute(contextButtonSize + contextButtonOffsetFromSide * 2)

            val uploadButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/upload_icon.png"
                    it.flipY = true
                    it.color.set(0xDDDDDDFF.toInt())
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/upload_icon.png"
                    it.flipY = true
                    it.color.set(0x4A8CCEFF)
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/upload_icon.png"
                    it.flipY = true
                    it.color.set(0x2E91F4FF)
                }
            }) {
                Game.networking.listenFor<ConfirmSubmitLevelMessage> {
                    if (it.isConfirmed) {
                        val properties = Properties()
                        properties.read(levelData)

                        Game.player.deleteLocalLevel(uuid)
                        Game.player.registerSavedLevel(it.uuid, properties)

                        updateLevelElements()
                    } else {
                        this@LocalLevelsState.gui.isInteractionEnabled = false
                        val window = Game.guis.openWindow("", 400.0f, 460.0f, false, false, false)
                        val widget = InfoWidget("Level rejected", "You can only upload\nlevels once every 60 seconds.") {
                            Game.guis.closeWindow(window)
                            Game.tasks.submit({
                                this@LocalLevelsState.gui.isInteractionEnabled = true
                            })
                        }.also {
                            it.constraints.x = same()
                            it.constraints.y = same()
                            it.constraints.width = fill()
                            it.constraints.height = fill()
                        }
                        window.content.addElement(widget)
                    }
                    true
                }

                Game.networking.send(SubmitLevelMessage().also {
                    it.levelData = levelData
                })
            }
            uploadButton.constraints.x = absolute(contextButtonOffsetFromSide + contextButtonSize * 2 + contextButtonSpacing * 2, true)
            uploadButton.constraints.y = absolute(contextButtonOffsetFromSide)
            uploadButton.constraints.width = absolute(contextButtonSize)
            uploadButton.constraints.height = aspect()
            contextButtonBackground.addElement(uploadButton)

            val editButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/edit_level_icon.png"
                    it.flipY = true
                    it.color.set(0xDDDDDDFF.toInt())
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/edit_level_icon.png"
                    it.flipY = true
                    it.color.set(0x4A8CCEFF)
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/edit_level_icon.png"
                    it.flipY = true
                    it.color.set(0x2E91F4FF)
                }
            }) {
                val properties = Properties()
                properties.read(levelData)
                properties.getProperties("level")?.let {
                    returnState = TransitionGameState(EditState(it.write(), uuid), LinearTransition(LinearTransition.Direction.RIGHT))
                }
            }
            editButton.constraints.x = absolute(contextButtonOffsetFromSide + contextButtonSize + contextButtonSpacing, true)
            editButton.constraints.y = absolute(contextButtonOffsetFromSide)
            editButton.constraints.width = absolute(contextButtonSize)
            editButton.constraints.height = aspect()
            contextButtonBackground.addElement(editButton)

            val deleteButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/trashcan_icon.png"
                    it.flipY = true
                    it.color.set(0xBF000CAA.toInt())
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/trashcan_open_icon.png"
                    it.flipY = true
                    it.color.set(0xBF000CFF.toInt())
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/trashcan_icon.png"
                    it.flipY = true
                    it.color.set(0xA0040FFF.toInt())
                }
            }) {
                removeLocalLevel(uuid)
            }
            deleteButton.constraints.x = absolute(contextButtonOffsetFromSide, true)
            deleteButton.constraints.y = absolute(contextButtonOffsetFromSide)
            deleteButton.constraints.width = absolute(contextButtonSize)
            deleteButton.constraints.height = aspect()
            contextButtonBackground.addElement(deleteButton)

            addElement(contextButtonBackground)
        }

        override fun onPlay() {
            val properties = Properties()
            properties.read(levelData)
            properties.getProperties("level")?.let {
                returnState = TransitionGameState(PlayLevelState(uuid, it.write()), CircleTransition())
            }
        }
    }

    private inner class SavedLevelElement(val uuid: String) : LevelElement(loadLevelData(uuid, false), loadLevelPreviewImage(uuid, false)) {
        init {
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

            val deleteButton = ImageButton(ImageButton.ImageButtonStyle().also {
                it.backgroundNormal = TextureDrawableValue().also {
                    it.texture = "textures/trashcan_icon.png"
                    it.flipY = true
                    it.color.set(0xBF000CAA.toInt())
                }
                it.backgroundHovered = TextureDrawableValue().also {
                    it.texture = "textures/trashcan_open_icon.png"
                    it.flipY = true
                    it.color.set(0xBF000CFF.toInt())
                }
                it.backgroundPressed = TextureDrawableValue().also {
                    it.texture = "textures/trashcan_icon.png"
                    it.flipY = true
                    it.color.set(0xA0040FFF.toInt())
                }
            }) {
                removeSavedLevel(uuid)
            }
            deleteButton.constraints.x = absolute(contextButtonOffsetFromSide, true)
            deleteButton.constraints.y = absolute(contextButtonOffsetFromSide)
            deleteButton.constraints.width = absolute(contextButtonSize)
            deleteButton.constraints.height = aspect()
            contextButtonBackground.addElement(deleteButton)

            addElement(contextButtonBackground)
        }

        override fun onPlay() {
            val properties = Properties()
            properties.read(levelData)
            properties.getProperties("level")?.let {
                returnState = TransitionGameState(PlayLevelState(uuid, it.write()), CircleTransition())
            }
        }
    }

    override val presentSource = Renderer2D.MENU

    private val menuElements = arrayListOf<MenuElement>()
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
        buttonBackground.constraints.width = absolute(buttonSize * 5 + buttonSpacing * 4 + buttonOffsetFromSide * 2)
        buttonBackground.constraints.height = absolute(buttonSize + buttonOffsetFromSide * 2)

        val createLevelButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/edit_level_icon.png"
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/edit_level_icon.png"
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/edit_level_icon.png"
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }) {
            returnState = TransitionGameState(EditState(), LinearTransition(LinearTransition.Direction.RIGHT))
        }
        createLevelButton.constraints.x = absolute(buttonOffsetFromSide + buttonSize * 4 + buttonSpacing * 4, true)
        createLevelButton.constraints.y = absolute(buttonOffsetFromSide)
        createLevelButton.constraints.width = absolute(buttonSize)
        createLevelButton.constraints.height = aspect()
        buttonBackground.addElement(createLevelButton)

        val onlineButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/online_icon.png"
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/online_icon.png"
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/online_icon.png"
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }) {
            returnState = TransitionGameState(OnlineLevelsState(), LinearTransition(LinearTransition.Direction.DOWN))
        }
        onlineButton.constraints.x = absolute(buttonOffsetFromSide + buttonSize * 3 + buttonSpacing * 3, true)
        onlineButton.constraints.y = absolute(buttonOffsetFromSide)
        onlineButton.constraints.width = absolute(buttonSize)
        onlineButton.constraints.height = aspect()
        buttonBackground.addElement(onlineButton)

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

        updateLevelElements()
    }

    private fun updateLevelElements() {
        levelsScrollPane.content.children.clear()
        menuElements.forEach {
            if (it is Disposable)
                it.dispose()
        }
        menuElements.clear()

        menuElements += TutorialLevelElement()

        Gdx.files.local("levels/local/").list().forEach {
            val uuid = it.name()
            menuElements += LocalLevelElement(uuid)
        }

        Gdx.files.local("levels/saved/").list().forEach {
            val uuid = it.name()
            menuElements += SavedLevelElement(uuid)
        }

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
    }

    private fun removeSavedLevel(uuid: String) {
        Game.player.deleteSavedLevel(uuid)
        updateLevelElements()
    }

    private fun removeLocalLevel(uuid: String) {
        Game.player.deleteLocalLevel(uuid)
        updateLevelElements()
    }

    override fun end() {
        super.end()

        menuElements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}