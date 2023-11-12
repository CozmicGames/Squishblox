package com.cozmicgames.game.graphics.renderer

import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.functions.BlankRenderFunction
import com.cozmicgames.game.graphics.engine.rendergraph.functions.BlurHorizontalRenderFunction
import com.cozmicgames.game.graphics.engine.rendergraph.functions.present.TonemapPresentFunction
import com.cozmicgames.game.graphics.engine.rendergraph.onRender
import com.cozmicgames.game.graphics.engine.rendergraph.passes.ColorRenderPass
import com.cozmicgames.game.renderGraph

class Renderer2D {
    companion object {
        const val MENU = "menu"

        const val WORLD_BACKGROUND = "worldBackground"
        const val WORLD = "world"

        const val INGAME_MENU_BLUR_HORIZONTAL = "inGameMenuBlurHorizontal"
        const val INGAME_MENU_BLUR_VERTICAL = "inGameMenuBlurVertical"
        const val INGAME_MENU = "inGameMenuFinal"

        const val TRANSITION_FROM = "transitionFrom"
        const val TRANSITION_TO = "transitionTo"
        const val TRANSITION = "transition"
    }

    var currentPresentSource = WORLD
        private set

    init {
        /**
         * MENU
         */
        Game.renderGraph.onRender(MENU, ColorRenderPass(), MenuRenderFunction())

        /**
         * WORLD
         */
        Game.renderGraph.onRender(WORLD_BACKGROUND, ColorRenderPass(), WorldBackgroundRenderFunction())
        Game.renderGraph.onRender(WORLD, ColorRenderPass(), WorldRenderFunction(WORLD_BACKGROUND, 0))

        /**
         * INGAME MENU
         */
        Game.renderGraph.onRender(INGAME_MENU_BLUR_HORIZONTAL, ColorRenderPass(), BlurHorizontalRenderFunction(WORLD, 0))
        Game.renderGraph.onRender(INGAME_MENU_BLUR_VERTICAL, ColorRenderPass(), BlurHorizontalRenderFunction(INGAME_MENU_BLUR_HORIZONTAL, 0))
        Game.renderGraph.onRender(INGAME_MENU, ColorRenderPass(), InGameMenuRenderFunction())

        /**
         * TRANSITION
         */
        Game.renderGraph.onRender(TRANSITION_FROM, ColorRenderPass(), BlankRenderFunction())
        Game.renderGraph.onRender(TRANSITION_TO, ColorRenderPass(), BlankRenderFunction())
        Game.renderGraph.onRender(TRANSITION, ColorRenderPass(), BlankRenderFunction())

        setPresentSource(MENU)
    }

    fun setPresentSource(pass: String) {
        if (pass == currentPresentSource)
            return

        Game.renderGraph.presentRenderFunction = TonemapPresentFunction(TonemapPresentFunction.Type.NONE, pass, 0)
        currentPresentSource = pass
    }

    fun beginFrame() {

    }

    fun endFrame() {

    }
}