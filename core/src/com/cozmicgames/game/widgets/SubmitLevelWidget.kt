package com.cozmicgames.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.extensions.safeWidth
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.engine.graphics2d.BasicRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.gui.*
import com.cozmicgames.game.graphics.gui.elements.*
import com.cozmicgames.game.graphics.gui.elements.List
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue
import com.cozmicgames.game.graphics.gui.skin.NinepatchDrawableValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.player.PlayerCamera
import com.cozmicgames.game.world.WorldConstants
import java.util.UUID

class SubmitLevelWidget(levelData: String, uuid: String?, callback: (Boolean) -> Unit) : Disposable, Panel(PanelStyle().also {
    it.background = NinepatchDrawableValue().also {
        it.texture = "textures/widget_background.png"
        it.autoSetSplitSizes()
        it.color.set(0xd3c781FF.toInt())
    }
}) {
    private val camera = PlayerCamera()

    init {
        Game.previewRenderer.setLevelData(levelData)
        Game.previewRenderer.camera = camera
        camera.getViewportWidth = { Game.previewRenderer.width.toFloat() }
        camera.getViewportHeight = { Game.previewRenderer.height.toFloat() }
        camera.getMaxZoom = { 1.5f }
        camera.getMinZoom = { 0.5f }
        camera.getMinY = { WorldConstants.WORLD_MIN_Y }
        camera.getMinX = { WorldConstants.WORLD_MIN_X + Gdx.graphics.safeWidth * 0.5f }
        camera.getMaxX = { WorldConstants.WORLD_MAX_X - Gdx.graphics.safeWidth * 0.5f }

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
            it.align.value = Align.center
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.textColor = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
        }

        val titleLabel = Label("Submit level", titleLabelStyle)
        titleLabel.constraints.x = center()
        titleLabel.constraints.y = absolute(0.0f)
        titleLabel.constraints.width = same(this)
        titleLabel.constraints.height = absolute(60.0f)

        val infoLabel = Label("Adjust preview", textLabelStyle)
        infoLabel.constraints.x = center()
        infoLabel.constraints.y = absolute(70.0f)
        infoLabel.constraints.width = same(this)
        infoLabel.constraints.height = absolute(30.0f)

        val screenshotElement = object : GUIElement() {
            override fun render() {
                Game.graphics2d.submit<BasicRenderable2D> {
                    it.layer = layer
                    it.texture = "blank"
                    it.color = Color.DARK_GRAY
                    it.x = x
                    it.y = y
                    it.width = width
                    it.height = height
                }

                val borderSize = 3.0f

                Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = layer + 1
                    it.texture = Game.previewRenderer.texture?.let { TextureRegion(it) }
                    it.x = x + borderSize
                    it.y = y + borderSize
                    it.width = width - borderSize * 2
                    it.height = height - borderSize * 2
                }
            }
        }
        screenshotElement.constraints.x = center()
        screenshotElement.constraints.y = absolute(110.0f)
        screenshotElement.constraints.width = absolute(800.0f)
        screenshotElement.constraints.height = absolute(600.0f)

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

            val properties = Properties()
            properties.setProperties("level", Properties().also { it.read(levelData) })
            val cameraProperties = Properties()
            cameraProperties.setFloat("x", camera.position.x)
            cameraProperties.setFloat("y", camera.position.y)
            cameraProperties.setFloat("zoom", camera.zoom)
            properties.setProperties("camera", cameraProperties)

            Game.player.registerLocalLevel(uuid ?: UUID.randomUUID().toString(), properties)
        }
        confirmButton.constraints.width = absolute(60.0f)
        confirmButton.constraints.height = aspect()

        fun padding(getAmount: () -> Float) = Group().also {
            it.constraints.width = absolute { getAmount() }
            it.constraints.height = absolute { getAmount() }
        }

        addElement(titleLabel)
        addElement(infoLabel)
        addElement(screenshotElement)

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
            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (screenshotElement.isHovered) {
                    camera.zoom *= 1.0f + (Game.input.scrollY * 0.1f)

                    if (Game.input.isButtonDown(1) || Game.input.isButtonDown(2)) {
                        camera.position.x -= Game.input.deltaX * camera.zoom
                        camera.position.y += Game.input.deltaY * camera.zoom
                    }

                    camera.update()
                }
            }
        })
    }

    override fun dispose() {
        Game.previewRenderer.camera = null
    }
}