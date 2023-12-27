package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.functions.BlankRenderFunction
import com.cozmicgames.game.graphics.engine.rendergraph.functions.present.TonemapPresentFunction
import com.cozmicgames.game.graphics.engine.rendergraph.onRender
import com.cozmicgames.game.graphics.engine.rendergraph.passes.ColorRenderPass
import com.cozmicgames.game.renderGraph

class Renderer2D {
    companion object {
        const val LOADING = "loading"

        const val MENU = "menu"

        const val WORLD = "world"

        const val TRANSITION_FROM = "transitionFrom"
        const val TRANSITION_TO = "transitionTo"
        const val TRANSITION = "transition"
    }

    var currentPresentSource = ""
        private set

    private var takeScreenshotCallbacks = arrayListOf<(Pixmap) -> Unit>()

    init {
        /**
         * MENU
         */
        Game.renderGraph.onRender(LOADING, ColorRenderPass(), LoadingRenderFunction())

        /**
         * MENU
         */
        Game.renderGraph.onRender(MENU, ColorRenderPass(), MenuRenderFunction())

        /**
         * WORLD
         */
        Game.renderGraph.onRender(WORLD, ColorRenderPass(), WorldRenderFunction())

        /**
         * TRANSITION
         */
        Game.renderGraph.onRender(TRANSITION_FROM, ColorRenderPass(), BlankRenderFunction())
        Game.renderGraph.onRender(TRANSITION_TO, ColorRenderPass(), BlankRenderFunction())
        Game.renderGraph.onRender(TRANSITION, ColorRenderPass(), BlankRenderFunction())
    }

    fun setPresentSource(pass: String) {
        if (pass == currentPresentSource)
            return

        Game.renderGraph.presentRenderFunction = TonemapPresentFunction(TonemapPresentFunction.Type.NONE, pass, 0)
        currentPresentSource = pass
    }

    fun takeScreenshot(callback: (Pixmap) -> Unit) {
        takeScreenshotCallbacks += callback
    }

    fun beginFrame() {

    }

    fun endFrame() {
        if (takeScreenshotCallbacks.isNotEmpty()) {
            val pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.width, Gdx.graphics.height)

            takeScreenshotCallbacks.forEach {
                it(pixmap)
            }

            pixmap.dispose()
            takeScreenshotCallbacks.clear()
        }
    }
}