package com.cozmicgames.game.states

import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.absolute
import com.cozmicgames.game.graphics.gui.distribute
import com.cozmicgames.game.graphics.gui.elements.Group
import com.cozmicgames.game.graphics.gui.elements.Panel
import com.cozmicgames.game.graphics.gui.elements.TextButton
import com.cozmicgames.game.graphics.gui.fill
import com.cozmicgames.game.graphics.gui.packed
import com.cozmicgames.game.graphics.gui.relative
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.graphics.gui.slideOutLeft
import com.cozmicgames.game.graphics.gui.transitionBack
import com.cozmicgames.game.graphics.gui.transitionTo
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.guis
import com.cozmicgames.game.input
import com.cozmicgames.game.renderer2d
import com.cozmicgames.game.tasks

class InGameMenuState(private val previousState: WorldState) : SuspendGameState {
    private var returnState: GameState = this
    private val gui = Game.guis.create()
    private val buttonsGroup: Group
    private val menuButtons: Array<TextButton>
    private val blockingPanel: Panel
    private var isReady = false
    private val previousGui = previousState.gui

    init {
        Game.guis.remove(previousGui)

        Game.renderer2d.setPresentSource(Renderer2D.INGAME_MENU)

        blockingPanel = Panel(Panel.PanelStyle().also {
            it.background = null
        })
        blockingPanel.constraints.x = absolute(0.0f)
        blockingPanel.constraints.y = absolute(0.0f)
        blockingPanel.constraints.width = fill()
        blockingPanel.constraints.height = fill()

        buttonsGroup = Group()
        buttonsGroup.constraints.x = absolute(0.0f)
        buttonsGroup.constraints.y = absolute(25.0f, true)
        buttonsGroup.constraints.width = packed()
        buttonsGroup.constraints.height = relative(0.4f)

        val buttonStyle = TextButton.TextButtonStyle().also {
            it.font = FontValue().also {
                it.font = "fonts/VinaSans-Regular.fnt"
            }
            it.isFixedTextSize.value = false
            it.align.value = Align.center
            it.textColorNormal = ColorValue().also {
                it.color.set(0xBBBBBBBFF.toInt())
            }
            it.textColorHovered = ColorValue().also {
                it.color.set(0xDDDDDDFF.toInt())
            }
            it.textColorPressed = ColorValue().also {
                it.color.set(0xBBBBBBBFF.toInt())
            }
            it.textColorDisabled = ColorValue().also {
                it.color.set(0xBBBBBBB99.toInt())
            }
            it.backgroundNormal = TextureDrawableValue().also {
                it.flipY = true
                it.paddingTop = 6.0f
                it.paddingLeft = 6.0f
                it.paddingBottom = 6.0f
                it.texture = "textures/menu_button.png"
                it.color.set(0x58585866)
            }
            it.backgroundHovered = TextureDrawableValue().also {
                it.flipY = true
                it.paddingTop = 6.0f
                it.paddingLeft = 6.0f
                it.paddingBottom = 6.0f
                it.texture = "textures/menu_button.png"
                it.color.set(0x6A6A6A66)
            }
            it.backgroundPressed = TextureDrawableValue().also {
                it.flipY = true
                it.paddingTop = 6.0f
                it.paddingLeft = 6.0f
                it.paddingBottom = 6.0f
                it.texture = "textures/menu_button.png"
                it.color.set(0x87878766.toInt())
            }
            it.backgroundDisabled = TextureDrawableValue().also {
                it.flipY = true
                it.paddingTop = 6.0f
                it.paddingLeft = 6.0f
                it.paddingBottom = 6.0f
                it.texture = "textures/menu_button.png"
                it.color.set(0x58585833)
            }
        }

        menuButtons = arrayOf(
            TextButton("Resume", buttonStyle) {
                resume()
            },
            TextButton("Save", buttonStyle) {

            },
            TextButton("Load", buttonStyle) {

            },
            TextButton("Settings", buttonStyle) {
                //TODO: Show settings
                println("Settings")
            },
            TextButton("Menu", buttonStyle) {
                buttonsGroup.slideOutLeft {
                    returnState = MenuState()
                    previousState.end()
                }
            }
        )

        class ButtonHoverListener : GUIElement.Listener {
            override fun onEnter(element: GUIElement) {
                element.transitionTo({
                    constraints.width = relative(0.275f, gui.root)
                })
            }

            override fun onExit(element: GUIElement) {
                element.transitionBack()
            }
        }

        menuButtons.forEachIndexed { index, button ->
            button.constraints.x = relative(-0.25f, gui.root)
            button.constraints.y = distribute(index, menuButtons)
            button.constraints.width = relative(0.25f, gui.root)
            button.constraints.height = absolute { buttonsGroup.height / (menuButtons.size + 1) }
            button.addListener(ButtonHoverListener())
            buttonsGroup.addElement(button)
        }

        gui.addElement(blockingPanel)
        gui.addElement(buttonsGroup)

        Game.tasks.schedule(0.25f) {
            fun animateButton(index: Int) {
                if (index >= menuButtons.size) {
                    isReady = true
                    return
                }

                menuButtons[index].transitionTo({
                    constraints.x = absolute(0.0f)
                }) {
                    menuButtons[index].transform.applyCurrentStateToElement()
                    animateButton(index + 1)
                }
            }

            animateButton(0)
        }
    }

    private fun resume() {
        buttonsGroup.slideOutLeft {
            previousState.returnState = previousState
            returnState = previousState
            Game.guis.add(previousGui)
        }
    }

    override fun render(delta: Float): () -> GameState {
        previousState.render(delta)

        if (isReady && Game.input.isKeyJustDown(Input.Keys.ESCAPE))
            resume()

        return { returnState }
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}