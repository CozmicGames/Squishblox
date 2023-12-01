package com.cozmicgames.game.states

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.ImageButton
import com.cozmicgames.game.graphics.gui.elements.Label
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.skin.*
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import kotlin.math.floor

class PlayLevelState(uuid: String, levelData: String) : WorldGameState() {
    init {
        Game.player.startLevel(uuid, levelData)
        Game.player.playState = PlayState.PLAY
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
        val labelWidth = 200.0f

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

        val timerLabel = Label({
            val time = Game.player.levelRunTime
            val minutes = floor(time / 60.0f).toInt()
            val seconds = floor(time % 60.0f).toInt()
            val milliseconds = floor((time * 1000.0f) % 1000.0f).toInt()

            val minutesString = minutes.toString()
            val secondsString = if (seconds < 10) "0$seconds" else seconds.toString()
            val millisecondsString = if (milliseconds < 10) "00$milliseconds" else if (milliseconds < 100) "0$milliseconds" else milliseconds.toString()

            "$minutesString:$secondsString:$millisecondsString"
        }, Label.LabelStyle().also {
            it.background = null
            it.align.value = Align.left
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.isFixedTextSize = BooleanValue().also {
                it.value = true
            }
            it.fixedTextSize = FloatValue().also {
                it.value = 50.0f
            }
            it.textColor = ColorValue().also {
                it.color.set(Color.WHITE)
            }
        })

        timerLabel.constraints.x = absolute(true) { buttonOffsetFromSide + buttonSpacing + resetButton.width + buttonSpacing + stopButton.width }
        timerLabel.constraints.y = same(resetButton)
        timerLabel.constraints.width = absolute(labelWidth)
        timerLabel.constraints.height = same(resetButton)
        buttonBackground.addElement(timerLabel)

        buttonBackground.constraints.x = absolute(0.0f, true)
        buttonBackground.constraints.y = absolute(0.0f)
        buttonBackground.constraints.width = absolute { buttonOffsetFromSide * 2.0f + timerLabel.width + buttonSpacing + resetButton.width + buttonSpacing + stopButton.width }
        buttonBackground.constraints.height = absolute(resetButton.y + buttonSize + buttonOffsetFromSide)

        gui.addElement(buttonBackground)
    }
}