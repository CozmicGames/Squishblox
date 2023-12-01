package com.cozmicgames.game.widgets

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.cozmicgames.common.networking.messages.RequestScoreboardMessage
import com.cozmicgames.common.networking.messages.ScoreboardMessage
import com.cozmicgames.game.Game
import com.cozmicgames.game.audio
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.*
import com.cozmicgames.game.graphics.gui.elements.List
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.networking
import kotlin.math.floor

class SettingsWidget(callback: () -> Unit) : Panel(PanelStyle().also {
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
            it.align.value = Align.left
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        val sliderStyle = Slider.SliderStyle().also {
            it.backgroundNormal = NinepatchDrawableValue().also {
                it.texture = "textures/slider_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xe47f3cFF.toInt())
            }
            it.backgroundHovered = NinepatchDrawableValue().also {
                it.texture = "textures/slider_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xe47f3cFF.toInt())
            }
            it.backgroundPressed = NinepatchDrawableValue().also {
                it.texture = "textures/slider_background.png"
                it.autoSetSplitSizes()
                it.color.set(0xe47f3cFF.toInt())
            }
            it.handleNormal = TextureDrawableValue().also {
                it.texture = "textures/slider_foreground.png"
                it.color.set(0xf2b877FF.toInt())
            }
            it.handleHovered = TextureDrawableValue().also {
                it.texture = "textures/slider_foreground.png"
                it.color.set(0xFFCA91FF.toInt())
            }
            it.handlePressed = TextureDrawableValue().also {
                it.texture = "textures/slider_foreground.png"
                it.color.set(0xDB9B57FF.toInt())
            }
        }

        val titleLabel = Label("Settings", titleLabelStyle)
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

        val musicVolumeLabel = Label("Music Volume", textLabelStyle)
        musicVolumeLabel.constraints.x = relative(0.1f)
        musicVolumeLabel.constraints.y = absolute(80.0f)
        musicVolumeLabel.constraints.width = relative(0.3f)
        musicVolumeLabel.constraints.height = absolute(50.0f)

        val musicVolumeSlider = Slider(sliderStyle)
        musicVolumeSlider.constraints.x = relative(0.6f)
        musicVolumeSlider.constraints.y = absolute(88.0f)
        musicVolumeSlider.constraints.width = relative(0.3f)
        musicVolumeSlider.constraints.height = absolute(34.0f)
        musicVolumeSlider.value = Game.audio.musicVolume

        val soundVolumeLabel = Label("Sound Volume", textLabelStyle)
        soundVolumeLabel.constraints.x = relative(0.1f)
        soundVolumeLabel.constraints.y = absolute(170.0f)
        soundVolumeLabel.constraints.width = relative(0.3f)
        soundVolumeLabel.constraints.height = absolute(50.0f)

        val soundVolumeSlider = Slider(sliderStyle)
        soundVolumeSlider.constraints.x = relative(0.6f)
        soundVolumeSlider.constraints.y = absolute(178.0f)
        soundVolumeSlider.constraints.width = relative(0.3f)
        soundVolumeSlider.constraints.height = absolute(34.0f)
        soundVolumeSlider.value = Game.audio.soundVolume

        addElement(titleLabel)
        addElement(musicVolumeLabel)
        addElement(musicVolumeSlider)
        addElement(soundVolumeLabel)
        addElement(soundVolumeSlider)
        addElement(closeButton)

        addListener(object : Listener {
            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                Game.audio.musicVolume = musicVolumeSlider.value
                Game.audio.soundVolume = soundVolumeSlider.value
            }
        })
    }
}