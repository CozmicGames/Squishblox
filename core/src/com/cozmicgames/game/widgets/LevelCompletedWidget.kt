package com.cozmicgames.game.widgets

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.cozmicgames.common.networking.messages.*
import com.cozmicgames.game.Game
import com.cozmicgames.game.gameSettings
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.*
import com.cozmicgames.game.graphics.gui.elements.List
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.networking
import kotlin.math.floor

class LevelCompletedWidget(uuid: String, time: Long, callback: (Boolean) -> Unit) : Panel(PanelStyle().also {
    it.background = NinepatchDrawableValue().also {
        it.texture = "textures/widget_background.png"
        it.autoSetSplitSizes()
        it.color.set(0xd3c781FF.toInt())
    }
}) {
    init {
        val titleLabelStyle = Label.LabelStyle().also {
            it.background = null
            it.isFixedTextSize.value = false
            it.wrap.value = false
            it.align.value = Align.center
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        val textLabelStyle = Label.LabelStyle().also {
            it.background = null
            it.isFixedTextSize.value = false
            it.wrap.value = false
            it.align.value = Align.topLeft
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        val textFieldStyle = TextArea.TextAreaStyle().also {
            it.background = NinepatchDrawableValue().also {
                it.texture = "textures/element_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xb15a29FF.toInt())
            }
            it.isFixedTextSize.value = false
            it.wrap.value = false
            it.align.value = Align.center
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        fun formatTimeString(time: Long): String {
            val minutes = floor(time / 1000 / 60.0f).toInt()
            val seconds = floor(time / 1000 % 60.0f).toInt()
            val milliseconds = floor(time % 1000.0f).toInt()

            val minutesString = minutes.toString()
            val secondsString = if (seconds < 10) "0$seconds" else seconds.toString()
            val millisecondsString = if (milliseconds < 10) "00$milliseconds" else if (milliseconds < 100) "0$milliseconds" else milliseconds.toString()

            return "$minutesString:$secondsString:$millisecondsString"
        }

        val titleLabel = Label("Level completed!", titleLabelStyle)
        titleLabel.constraints.x = center()
        titleLabel.constraints.y = absolute(0.0f)
        titleLabel.constraints.width = same(this)
        titleLabel.constraints.height = absolute(60.0f)

        val timeLabel = Label("Your time:\n${formatTimeString(time)}", textLabelStyle)
        timeLabel.constraints.x = relative(0.55f)
        timeLabel.constraints.y = relative(0.2f)
        timeLabel.constraints.width = relative(0.4f)
        timeLabel.constraints.height = relative(0.4f)

        val nameField = TextField(Game.gameSettings.name, textFieldStyle)
        nameField.constraints.x = relative(0.55f)
        nameField.constraints.y = relative(0.65f)
        nameField.constraints.width = relative(0.4f)
        nameField.constraints.height = absolute(50.0f)

        val closeButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/cancel_button.png"
                it.flipY = true
                it.color.set(0xBF000CAA.toInt())
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/cancel_button.png"
                it.flipY = true
                it.color.set(0xBF000CFF.toInt())
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/cancel_button.png"
                it.flipY = true
                it.color.set(0xA0040FFF.toInt())
            }
        }) {
            callback(false)
        }
        closeButton.constraints.width = absolute(60.0f)
        closeButton.constraints.height = aspect()

        val confirmButton = ImageButton(ImageButton.ImageButtonStyle().also {
            it.backgroundNormal = TextureDrawableValue().also {
                it.texture = "textures/confirm_button.png"
                it.flipY = true
                it.color.set(0x00BA2BFF)
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.texture = "textures/confirm_button.png"
                it.flipY = true
                it.color.set(0x00D62EFF)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.texture = "textures/confirm_button.png"
                it.flipY = true
                it.color.set(0x009E22FF)
            }
            it.backgroundDisabled = TextureDrawableValue().also {
                it.texture = "textures/confirm_button.png"
                it.flipY = true
                it.color.set(0x009E22AA)
            }
        }) {
            callback(true)

            Game.networking.send(SubmitScoreMessage().also {
                it.time = time
                it.name = nameField.text
                it.uuid = uuid
            })

            Game.gameSettings.name = nameField.text
        }
        confirmButton.constraints.width = absolute(60.0f)
        confirmButton.constraints.height = aspect()
        confirmButton.isEnabled = false

        fun padding(getAmount: () -> Float) = Group().also {
            it.constraints.width = absolute { getAmount() }
            it.constraints.height = absolute { getAmount() }
        }

        val scoreboardList = VerticalList<GUIElement>(List.ListStyle().also {
            it.background = null
        })
        scoreboardList.constraints.x = relative(0.05f)
        scoreboardList.constraints.y = relative(0.2f)
        scoreboardList.constraints.width = relative(0.4f)
        scoreboardList.constraints.height = relative(0.6f)

        scoreboardList.add(Label("Scoreboard", textLabelStyle).also {
            it.constraints.width = relative(0.4f, this)
            it.constraints.height = absolute(50.0f)
        })

        Game.networking.listenFor<ScoreboardMessage> {
            if (it.uuid == uuid) {
                it.scoreboard.forEachIndexed { index, entry ->
                    if (entry != null) {
                        scoreboardList.add(Label("$index:  ${entry.name} (${formatTimeString(entry.time)})", textLabelStyle).also {
                            it.constraints.width = relative(0.4f, this)
                            it.constraints.height = absolute(50.0f)
                        })
                    }
                }
            }
            true
        }
        Game.networking.send(RequestScoreboardMessage().also {
            it.uuid = uuid
        })

        addElement(titleLabel)
        addElement(scoreboardList)
        addElement(timeLabel)
        addElement(nameField)

        val buttonList = HorizontalList<GUIElement>(List.ListStyle().also {
            it.background = null
        })
        buttonList.constraints.x = absolute(0.0f)
        buttonList.constraints.y = absolute(20.0f, true)
        buttonList.constraints.width = same(this)
        buttonList.constraints.height = absolute(60.0f)
        buttonList.add(padding { (width - closeButton.width - confirmButton.width - 30.0f) * 0.5f })
        buttonList.add(closeButton)
        buttonList.add(padding { 30.0f })
        buttonList.add(confirmButton)
        addElement(buttonList)

        addListener(object : Listener {
            var previousName = nameField.text

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (nameField.text != previousName) {
                    confirmButton.isEnabled = false

                    Game.networking.listenFor<ConfirmNameMessage> {
                        if (it.name == nameField.text && it.isConfirmed)
                            confirmButton.isEnabled = true
                        true
                    }
                    Game.networking.send(CheckNameMessage().also {
                        it.name = nameField.text
                    })

                    previousName = nameField.text
                }
            }
        })
    }
}