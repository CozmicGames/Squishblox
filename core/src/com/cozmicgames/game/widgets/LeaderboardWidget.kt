package com.cozmicgames.game.widgets

import com.badlogic.gdx.utils.Align
import com.cozmicgames.common.networking.messages.RequestScoreboardMessage
import com.cozmicgames.common.networking.messages.ScoreboardMessage
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.*
import com.cozmicgames.game.graphics.gui.elements.List
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.networking
import kotlin.math.floor

class LeaderboardWidget(uuid: String, callback: () -> Unit) : Panel(PanelStyle().also {
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

        fun formatTimeString(time: Long): String {
            val minutes = floor(time / 1000 / 60.0f).toInt()
            val seconds = floor(time / 1000 % 60.0f).toInt()
            val milliseconds = floor(time % 1000.0f).toInt()

            val minutesString = minutes.toString()
            val secondsString = if (seconds < 10) "0$seconds" else seconds.toString()
            val millisecondsString = if (milliseconds < 10) "00$milliseconds" else if (milliseconds < 100) "0$milliseconds" else milliseconds.toString()

            return "$minutesString:$secondsString:$millisecondsString"
        }

        val titleLabel = Label("Leaderboard", titleLabelStyle)
        titleLabel.constraints.x = center()
        titleLabel.constraints.y = absolute(0.0f)
        titleLabel.constraints.width = same(this)
        titleLabel.constraints.height = absolute(60.0f)

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
            callback()
        }
        closeButton.constraints.x = center()
        closeButton.constraints.y = absolute(0.0f, true)
        closeButton.constraints.width = absolute(60.0f)
        closeButton.constraints.height = aspect()

        val scoreboardList = VerticalList<GUIElement>(List.ListStyle().also {
            it.background = null
        })
        scoreboardList.constraints.x = center()
        scoreboardList.constraints.y = absolute(80.0f)
        scoreboardList.constraints.width = relative(0.85f)
        scoreboardList.constraints.height = absolute(300.0f)

        Game.networking.listenFor<ScoreboardMessage> {
            if (it.uuid == uuid) {
                it.scoreboard.forEachIndexed { index, entry ->
                    if (entry != null) {
                        scoreboardList.add(Label("${index + 1}:  ${entry.name} (${formatTimeString(entry.time)})", textLabelStyle).also {
                            it.constraints.width = relative(0.4f, this)
                            it.constraints.height = absolute(50.0f)
                        })
                    }
                }
                true
            } else
                false
        }
        Game.networking.send(RequestScoreboardMessage().also {
            it.uuid = uuid
        })

        addElement(titleLabel)
        addElement(scoreboardList)
        addElement(closeButton)
    }
}