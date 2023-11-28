package com.cozmicgames.game.states

import com.badlogic.gdx.Input
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.ButtonGroup
import com.cozmicgames.game.graphics.gui.elements.ImageButton
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.player.ConfirmWidget
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.world.WorldScene

class EditState(levelData: String? = null) : InGameState() {
    private val editButtons: Array<ImageButton>
    private val buttonGroup = ButtonGroup<ImageButton>()

    init {
        if (levelData != null)
            Game.player.scene.initialize(levelData)
        else
            Game.player.scene.initialize()

        Game.player.playState = PlayState.EDIT
        Game.player.isPaused = false

        val editButtonBackground = Panel(Panel.PanelStyle().also {
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
            returnState = TestState(Game.player.getCurrentLevelData())
        }
        playButton.constraints.x = absolute(buttonOffsetFromSide, true)
        playButton.constraints.y = absolute(buttonOffsetFromSide)
        playButton.constraints.width = absolute(buttonSize)
        playButton.constraints.height = aspect()
        editButtonBackground.addElement(playButton)

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
            editButtonBackground.addElement(it)
            it.constraints.x = absolute(buttonOffsetFromSide, true)
            it.constraints.y = absolute(buttonY)
            it.constraints.width = absolute(buttonSize)
            it.constraints.height = aspect()
        }
        buttonGroup.selected = editButtons.first()
        buttonY += buttonSpacing + buttonSize

        val resetButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/edit_reset_icon.png"
                it.flipY = true
                it.color.set(0xBF000CAA.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/edit_reset_icon_open.png"
                it.flipY = true
                it.color.set(0xBF000CFF.toInt())
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/edit_reset_icon.png"
                it.flipY = true
                it.color.set(0xA0040FFF.toInt())
            }
        }) {
            gui.isInteractionEnabled = false
            val window = Game.guis.openWindow("", 400.0f, 300.0f, false, false, false)
            val confirmWidget: ConfirmWidget = ConfirmWidget("Reset level", "Do you want to reset the level?\nThis deletes all progress.") {
                if (it)
                    Game.player.scene.initialize()
                Game.tasks.submit({
                    Game.guis.closeWindow(window)
                    gui.isInteractionEnabled = true
                })
            }.also {
                it.constraints.x = same()
                it.constraints.y = same()
                it.constraints.width = fill()
                it.constraints.height = fill()
            }
            window.content.addElement(confirmWidget)
        }
        resetButton.constraints.x = absolute(buttonOffsetFromSide, true)
        resetButton.constraints.y = absolute(buttonY)
        resetButton.constraints.width = absolute(buttonSize)
        resetButton.constraints.height = aspect()
        editButtonBackground.addElement(resetButton)

        editButtonBackground.constraints.x = absolute(0.0f, true)
        editButtonBackground.constraints.y = absolute(0.0f)
        editButtonBackground.constraints.width = absolute(buttonSize + buttonOffsetFromSide * 2.0f)
        editButtonBackground.constraints.height = absolute(buttonY + buttonSize + buttonOffsetFromSide)

        gui.addElement(editButtonBackground)
    }

    override fun update(delta: Float) {
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