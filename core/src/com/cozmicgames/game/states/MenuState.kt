package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Version
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.absolute
import com.cozmicgames.game.graphics.gui.aspect
import com.cozmicgames.game.graphics.gui.center
import com.cozmicgames.game.graphics.gui.distribute
import com.cozmicgames.game.graphics.gui.elements.Group
import com.cozmicgames.game.graphics.gui.elements.Image
import com.cozmicgames.game.graphics.gui.elements.Label
import com.cozmicgames.game.graphics.gui.elements.TextButton
import com.cozmicgames.game.graphics.gui.packed
import com.cozmicgames.game.graphics.gui.relative
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue
import com.cozmicgames.game.graphics.gui.skin.TextureDrawableValue
import com.cozmicgames.game.graphics.gui.transitionBack
import com.cozmicgames.game.graphics.gui.transitionTo
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.guis
import com.cozmicgames.game.renderGraph
import com.cozmicgames.game.renderer2d
import com.cozmicgames.game.tasks
import com.cozmicgames.game.time

class MenuState : GameState {
    private var returnState: GameState = this
    private val gui = Game.guis.create()
    private var isConnectionReady = false
    private var backgroundOffsetStart = 0.0f
    private var isAnimating = false

    private val bannerImage: Image
    private val versionLabel: Label
    private val buttonsGroup: Group
    private val menuButtons: Array<TextButton>

    init {
        Game.renderer2d.setPresentSource(Renderer2D.MENU)

        bannerImage = Image("branding/banner.png", flipY = true)
        bannerImage.constraints.x = center()
        bannerImage.constraints.y = absolute(25.0f)
        bannerImage.constraints.width = relative(0.6f)
        bannerImage.constraints.height = aspect(1200.0f / 500.0f)

        versionLabel = Label({ Version.versionString }, Label.LabelStyle().also {
            it.wrap.value = false
            it.background = null
            it.isFixedTextSize.value = false
            it.textColor.color.set(0x9A9A9AFF.toInt())
        })
        versionLabel.constraints.x = absolute(5.0f, true)
        versionLabel.constraints.y = absolute(5.0f, true)
        versionLabel.constraints.width = packed()
        versionLabel.constraints.height = absolute(15.0f)

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
            TextButton("Singleplayer", buttonStyle) {
                returnState = TransitionGameState(WorldState(), LinearTransition(LinearTransition.Direction.DOWN))
            },
            TextButton("Settings", buttonStyle) {
                //TODO: Show settings
                println("Settings")
            },
            TextButton("Credits", buttonStyle) {
                //TODO: Show credits
                println("Credits")
            },
            TextButton("Exit", buttonStyle) {
                Gdx.app.exit()
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

        gui.addElement(bannerImage)
        gui.addElement(versionLabel)
        gui.addElement(buttonsGroup)

        Game.tasks.schedule(1.0f) {
            isAnimating = true

            fun animateButton(index: Int) {
                if (index >= menuButtons.size)
                    return

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

    override fun render(delta: Float): () -> GameState {
        Game.renderGraph.render(Game.time.delta)

        if (isConnectionReady)
            returnState = TransitionGameState(WorldState(), GlitchTransition())

        return { returnState }
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}