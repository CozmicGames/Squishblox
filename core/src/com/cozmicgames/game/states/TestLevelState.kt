package com.cozmicgames.game.states

import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.absolute
import com.cozmicgames.game.graphics.gui.aspect
import com.cozmicgames.game.graphics.gui.elements.ImageButton
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState

class TestLevelState(levelData: String, uuid: String?) : WorldGameState() {
    init {
        Game.player.startLevel(uuid, levelData)
        Game.player.playState = PlayState.TEST
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

        val stopButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/stop_icon.png"
                it.flipY = true
                it.color.set(0xBF000CAA.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/stop_icon.png"
                it.flipY = true
                it.color.set(0xBF000CFF.toInt())
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/stop_icon.png"
                it.flipY = true
                it.color.set(0xA0040FFF.toInt())
            }
        }) {
            returnState = EditState(Game.player.getCurrentLevelData())
        }
        stopButton.constraints.x = absolute(buttonOffsetFromSide, true)
        stopButton.constraints.y = absolute(buttonOffsetFromSide)
        stopButton.constraints.width = absolute(buttonSize)
        stopButton.constraints.height = aspect()
        buttonBackground.addElement(stopButton)

        var buttonY = stopButton.y
        //repeat(6) {
        //    buttonY += buttonSpacing + buttonSize
        //} //TODO: Maybe transition the ui by scaling it..
        buttonY += buttonSpacing + buttonSize

        val resetButton = ImageButton(ImageButton.ImageButtonStyle().also {
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
            Game.player.resetLevel()
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
    }
}