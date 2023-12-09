package com.cozmicgames.game.states

import com.badlogic.gdx.Input
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.ButtonGroup
import com.cozmicgames.game.graphics.gui.elements.ImageButton
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.widgets.ConfirmWidget
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.world.WorldScene

class EditState(levelData: String? = null, levelUuid: String? = null) : WorldGameState() {
    private val editButtons: Array<ImageButton>
    private val buttonGroup = ButtonGroup<ImageButton>()

    init {
        if (levelData != null)
            Game.player.scene.initialize(levelData)
        else
            Game.player.scene.initialize()

        Game.player.playState = PlayState.EDIT
        Game.player.isPaused = false

        val buttonBackground = Panel(Panel.PanelStyle().also {
            it.background = NinepatchDrawableValue().also {
                it.texture = "textures/edit_button_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xd3c781FF.toInt())
            }
        })

        val buttonSpacing = 10.0f
        val buttonSize = 75.0f
        val buttonOffsetFromSide = 15.0f

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
        }) {
            returnState = TestLevelState(Game.player.getCurrentLevelData(), levelUuid)
        }
        playButton.constraints.x = absolute(buttonOffsetFromSide, true)
        playButton.constraints.y = absolute(buttonOffsetFromSide)
        playButton.constraints.width = absolute(buttonSize)
        playButton.constraints.height = aspect()
        buttonBackground.addElement(playButton)

        fun getEditButtonStyle(texture: String) = ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = texture
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = texture
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = texture
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }

        editButtons = arrayOf(
            ImageButton(getEditButtonStyle("textures/create_block_icon.png")) {
                Game.player.scene.editState = WorldScene.EditState.CREATE
            },
            ImageButton(getEditButtonStyle("textures/edit_block_icon.png")) {
                Game.player.scene.editState = WorldScene.EditState.EDIT
            },
            ImageButton(getEditButtonStyle("textures/edit_platform_icon.png")) {
                Game.player.scene.editState = WorldScene.EditState.EDIT_PLATFORM
            },
            ImageButton(getEditButtonStyle("textures/edit_scale_up_icon.png")) {
                Game.player.scene.editState = WorldScene.EditState.EDIT_SCALE_UP
            },
            ImageButton(getEditButtonStyle("textures/edit_scale_down_icon.png")) {
                Game.player.scene.editState = WorldScene.EditState.EDIT_SCALE_DOWN
            },
            ImageButton(getEditButtonStyle("textures/delete_block_icon.png")) {
                Game.player.scene.editState = WorldScene.EditState.DELETE
            }
        )

        var buttonY = playButton.y
        editButtons.forEach {
            buttonY += buttonSpacing + buttonSize
            buttonGroup.add(it, it.onClick)
            buttonBackground.addElement(it)
            it.constraints.x = absolute(buttonOffsetFromSide, true)
            it.constraints.y = absolute(buttonY)
            it.constraints.width = absolute(buttonSize)
            it.constraints.height = aspect()
        }
        buttonGroup.selected = editButtons.first()
        buttonY += buttonSpacing + buttonSize

        val resetButton = ImageButton(ImageButton.ImageButtonStyle().also {
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
            gui.isInteractionEnabled = false
            val window = gui.openWindow("", 450.0f, 300.0f, false, false, false)
            val widget = ConfirmWidget("Reset level", "Do you want to reset the level?\nThis deletes all progress.") {
                if (it)
                    Game.player.scene.initialize()
                Game.tasks.submit({
                    window.close()
                    gui.isInteractionEnabled = true
                })
            }.also {
                it.constraints.x = same()
                it.constraints.y = same()
                it.constraints.width = fill()
                it.constraints.height = fill()
            }
            window.content.addElement(widget)
        }
        resetButton.constraints.x = absolute(buttonOffsetFromSide, true)
        resetButton.constraints.y = absolute(buttonY)
        resetButton.constraints.width = absolute(buttonSize)
        resetButton.constraints.height = aspect()
        buttonBackground.addElement(resetButton)

        buttonBackground.constraints.x = absolute(0.0f, true)
        buttonBackground.constraints.y = absolute(0.0f)
        buttonBackground.constraints.width = absolute(buttonSize + buttonOffsetFromSide * 2.0f)
        buttonBackground.constraints.height = absolute(buttonY + buttonSize + buttonOffsetFromSide)

        gui.addElement(buttonBackground)

        val backButtonBackground = Panel(Panel.PanelStyle().also {
            it.background = NinepatchDrawableValue().also {
                it.texture = "textures/level_context_button_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xd3c781FF.toInt())
            }
        })
        backButtonBackground.constraints.x = absolute(0.0f)
        backButtonBackground.constraints.y = absolute(0.0f)
        backButtonBackground.constraints.width = absolute(buttonSize + buttonOffsetFromSide * 2.0f)
        backButtonBackground.constraints.height = absolute(buttonSize + buttonOffsetFromSide * 2.0f)

        val backButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/back_icon.png"
                it.flipY = true
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/back_icon.png"
                it.flipY = true
                it.color.set(0x4A8CCEFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/back_icon.png"
                it.flipY = true
                it.color.set(0x2E91F4FF)
            }
        }) {
            gui.isInteractionEnabled = false
            val window = gui.openWindow("", 450.0f, 300.0f, false, false, false)
            val widget = ConfirmWidget("Close editor", "Do you want to close the editor?\nAll progress will be lost.") {
                if (it)
                    returnState = TransitionGameState(LocalLevelsState(), CircleTransition())
                Game.tasks.submit({
                    window.close()
                    gui.isInteractionEnabled = true
                })
            }.also {
                it.constraints.x = same()
                it.constraints.y = same()
                it.constraints.width = fill()
                it.constraints.height = fill()
            }
            window.content.addElement(widget)
        }
        backButton.constraints.x = absolute(buttonOffsetFromSide)
        backButton.constraints.y = absolute(buttonOffsetFromSide)
        backButton.constraints.width = absolute(buttonSize)
        backButton.constraints.height = aspect()
        backButtonBackground.addElement(backButton)

        gui.addElement(backButtonBackground)
    }

    override fun update(delta: Float) {
        super.update(delta)

        if (Game.input.isKeyJustDown(Input.Keys.NUM_1)) {
            buttonGroup.selected = editButtons[0]
            Game.player.scene.editState = WorldScene.EditState.CREATE
        }

        if (Game.input.isKeyJustDown(Input.Keys.NUM_2)) {
            buttonGroup.selected = editButtons[1]
            Game.player.scene.editState = WorldScene.EditState.EDIT
        }

        if (Game.input.isKeyJustDown(Input.Keys.NUM_3)) {
            buttonGroup.selected = editButtons[2]
            Game.player.scene.editState = WorldScene.EditState.EDIT_PLATFORM
        }

        if (Game.input.isKeyJustDown(Input.Keys.NUM_4)) {
            buttonGroup.selected = editButtons[3]
            Game.player.scene.editState = WorldScene.EditState.EDIT_SCALE_UP
        }

        if (Game.input.isKeyJustDown(Input.Keys.NUM_5)) {
            buttonGroup.selected = editButtons[4]
            Game.player.scene.editState = WorldScene.EditState.EDIT_SCALE_DOWN
        }

        if (Game.input.isKeyJustDown(Input.Keys.NUM_6)) {
            buttonGroup.selected = editButtons[5]
            Game.player.scene.editState = WorldScene.EditState.DELETE
        }
    }
}