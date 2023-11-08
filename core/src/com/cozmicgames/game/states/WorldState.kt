package com.cozmicgames.game.states

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics.gui.GUI
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.world.WorldConstants
import com.cozmicgames.game.world.WorldScene
import kotlin.math.ceil
import kotlin.math.floor

class WorldState : SuspendableGameState {
    internal var returnState: GameState = this

    val gui: GUI = Game.guis.create()
    val scene = WorldScene()

    init {
        gui.isInteractionEnabled = false
        gui.pauseRenderingOnDisabled = true
    }

    private fun drawBackground() {
        val backgroundTileWidth = 8 * WorldConstants.WORLD_CELL_SIZE
        val backgroundTileHeight = 8 * WorldConstants.WORLD_CELL_SIZE

        val numBackgroundTilesX = ceil(Game.player.camera.rectangle.width / backgroundTileWidth).toInt() + 1
        val numBackgroundTilesY = ceil(Game.player.camera.rectangle.height / backgroundTileHeight).toInt() + 1

        var backgroundTileX = floor((Game.player.camera.position.x - Game.player.camera.rectangle.width * 0.5f) / backgroundTileWidth) * backgroundTileWidth

        repeat(numBackgroundTilesX) {
            var backgroundTileY = floor((Game.player.camera.position.y - Game.player.camera.rectangle.height * 0.5f) / backgroundTileHeight) * backgroundTileHeight

            repeat(numBackgroundTilesY) {
                Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_BACKGROUND
                    it.texture = "textures/grid_background_8x8.png"
                    it.x = backgroundTileX
                    it.y = backgroundTileY
                    it.width = backgroundTileWidth
                    it.height = backgroundTileHeight
                    it.color = Color.LIGHT_GRAY
                }

                backgroundTileY += backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }
    }

    override fun render(delta: Float): () -> GameState {
        if (gui.isInteractionEnabled && Game.input.isKeyJustDown(Input.Keys.ESCAPE))
            returnState = InGameMenuState(this)

        drawBackground()
        scene.update(delta)
        Game.renderGraph.render(Game.time.delta)

        return { returnState }
    }

    override fun suspend() {
        gui.isInteractionEnabled = false
    }

    override fun resumeFromSuspension() {
        Game.renderer2d.setPresentSource(Renderer2D.WORLD)

        gui.isInteractionEnabled = true
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}