package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.ImageButton
import com.cozmicgames.game.graphics.gui.elements.Label
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.skin.*
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState

class PlayTutorialLevelState : WorldGameState() {
    init {
        val data = Gdx.files.internal("tutorial/level.json").readString()
        Game.player.startLevel("tutorial", data)
        Game.player.playState = PlayState.TUTORIAL
        Game.player.isPaused = false

        val buttonBackground = Panel(Panel.PanelStyle().also {
            it.background = NinepatchDrawableValue().also {
                it.texture = "textures/edit_button_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xd3c781FF.toInt())
            }
        })

        val buttonSize = 75.0f
        val buttonSpacing = 10.0f
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
            returnState = TransitionGameState(LocalLevelsState(), CircleTransition())
        }
        stopButton.constraints.x = absolute(buttonOffsetFromSide, true)
        stopButton.constraints.y = absolute(buttonOffsetFromSide)
        stopButton.constraints.width = absolute(buttonSize)
        stopButton.constraints.height = aspect()
        buttonBackground.addElement(stopButton)

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
        resetButton.constraints.x = absolute(buttonOffsetFromSide + buttonSpacing + stopButton.width, true)
        resetButton.constraints.y = absolute(buttonOffsetFromSide)
        resetButton.constraints.width = absolute(buttonSize)
        resetButton.constraints.height = aspect()
        buttonBackground.addElement(resetButton)

        buttonBackground.constraints.x = absolute(0.0f, true)
        buttonBackground.constraints.y = absolute(0.0f)
        buttonBackground.constraints.width = absolute { buttonOffsetFromSide * 2.0f + resetButton.width + buttonSpacing + stopButton.width }
        buttonBackground.constraints.height = absolute(resetButton.y + buttonSize + buttonOffsetFromSide)

        val infoBackground = Panel(Panel.PanelStyle().also {
            it.background = ColorDrawableValue().also {
                it.color.set(0x88888844.toInt())
                it.setPadding(10.0f)
            }
        })

        val infoLabel = Label("""
            Move left with A
            Move right with D
            Jump with SPACE
            Blocks with + make you grow
            Blocks with - make you shrink
            Use platforms to deform yourself
            The goal is to reach your friend
        """.trimIndent(), Label.LabelStyle().also {
            it.background = null
            it.isFixedTextSize.value = true
            it.fixedTextSize = FloatValue().also {
                it.value = 30.0f
            }
            it.wrap.value = false
            it.align.value = Align.center
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        })
        infoLabel.constraints.x = absolute(0.0f)
        infoLabel.constraints.y = absolute(0.0f)
        infoLabel.constraints.width = packed()
        infoLabel.constraints.height = packed()

        infoBackground.addElement(infoLabel)

        infoBackground.constraints.x = absolute(20.0f)
        infoBackground.constraints.y = absolute(20.0f)
        infoBackground.constraints.width = packed()
        infoBackground.constraints.height = packed()

        gui.addElement(buttonBackground)
        gui.addElement(infoBackground)
    }
}