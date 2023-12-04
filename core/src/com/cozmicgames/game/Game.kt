package com.cozmicgames.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.cozmicgames.game.audio.AudioManager
import com.cozmicgames.game.localization.LocalizationManager
import com.cozmicgames.game.graphics.engine.graphics2d.Graphics2D
import com.cozmicgames.game.graphics.engine.graphics2d.fonts.FontManager
import com.cozmicgames.game.graphics.engine.rendergraph.RenderGraph
import com.cozmicgames.game.graphics.engine.rendergraph.functions.BlankRenderFunction
import com.cozmicgames.game.graphics.engine.shaders.ShaderManager
import com.cozmicgames.game.graphics.engine.textures.TextureManager
import com.cozmicgames.game.graphics.gui.GUIManager
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.input.GestureManager
import com.cozmicgames.game.input.InputManager
import com.cozmicgames.game.states.GameState
import com.cozmicgames.game.states.LoadingState
import com.cozmicgames.game.states.SuspendGameState
import com.cozmicgames.game.states.SuspendableGameState
import com.cozmicgames.game.states.TransitionGameState
import com.cozmicgames.common.utils.*
import com.cozmicgames.game.networking.NetworkManager
import com.cozmicgames.game.player.Player
import com.cozmicgames.game.physics.Physics
import com.cozmicgames.game.world.WorldPreviewImageRenderer
import com.cozmicgames.game.world.WorldPreviewRenderer
import kotlin.system.exitProcess

class Game(gameSettings: GameSettings) : ApplicationAdapter() {
    companion object {
        val context = Context()
    }

    private lateinit var currentGameState: GameState
    private var isFirstResize = true

    init {
        context.bind(gameSettings)
        Thread.currentThread().setUncaughtExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            exitProcess(1)
        }
    }

    override fun create() {
        currentGameState = LoadingState()
    }

    override fun resize(width: Int, height: Int) {
        if (isFirstResize)
            isFirstResize = false
        else {
            gameSettings.width = width
            gameSettings.height = height
        }

        if (!::currentGameState.isInitialized)
            return

        context.forEach {
            if (it is Resizable)
                it.resize(width, height)
        }

        currentGameState.resize(width, height)
    }

    override fun render() {
        context.forEach {
            if (it is Updatable)
                it.update(Gdx.graphics.deltaTime)
        }

        graphics2d.beginFrame()
        renderer2d.beginFrame()

        val newState = currentGameState.render(Game.time.delta)()
        renderGraph.render(Game.time.delta)

        renderer2d.endFrame()
        graphics2d.endFrame()

        if (newState != currentGameState) {
            if (newState !is SuspendGameState)
                currentGameState.end()
            else {
                if (currentGameState is SuspendableGameState)
                    (currentGameState as SuspendableGameState).suspend()
                else
                    currentGameState.end()
            }

            if (currentGameState is SuspendGameState && newState is SuspendableGameState)
                newState.resumeFromSuspension()

            currentGameState = newState
        }
    }

    override fun dispose() {
        currentGameState.end()
        context.dispose()
    }
}

val Game.Companion.time by Game.context.injector { Time() }
val Game.Companion.gameSettings by Game.context.injector<GameSettings>()
val Game.Companion.tasks by Game.context.injector { TaskManager() }
val Game.Companion.input by Game.context.injector { InputManager() }
val Game.Companion.gestures by Game.context.injector { GestureManager() }
val Game.Companion.localization by Game.context.lazyInjector { LocalizationManager() }
val Game.Companion.player by Game.context.injector { Player() }
val Game.Companion.shaders by Game.context.injector { ShaderManager() }
val Game.Companion.textures by Game.context.injector { TextureManager() }
val Game.Companion.fonts by Game.context.injector { FontManager() }
val Game.Companion.audio by Game.context.injector { AudioManager() }
val Game.Companion.graphics2d by Game.context.injector { Graphics2D() }
val Game.Companion.guis by Game.context.injector { GUIManager() }
val Game.Companion.previewRenderer by Game.context.injector { WorldPreviewRenderer(800, 600) }
val Game.Companion.previewImageRenderer by Game.context.injector { WorldPreviewImageRenderer(800, 600) }
val Game.Companion.renderGraph by Game.context.injector { RenderGraph(BlankRenderFunction()) }
val Game.Companion.renderer2d by Game.context.injector { Renderer2D() }
val Game.Companion.physics by Game.context.injector { Physics() }
val Game.Companion.networking by Game.context.injector { NetworkManager() }
