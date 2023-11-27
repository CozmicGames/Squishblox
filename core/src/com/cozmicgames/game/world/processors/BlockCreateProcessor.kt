package com.cozmicgames.game.world.processors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.world.WorldConstants
import com.cozmicgames.game.world.WorldScene
import com.cozmicgames.game.world.WorldUtils
import kotlin.math.max
import kotlin.math.min

class BlockCreateProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private var x0 = 0
    private var y0 = 0
    private var x1 = 0
    private var y1 = 0

    private val blockPreviewNinePatch: NinePatch

    init {
        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.EDIT && worldScene.editState == WorldScene.EditState.CREATE
    }

    private fun isBuildable(minX: Int, minY: Int, maxX: Int, maxY: Int): Boolean {
        var result = true
        worldScene.world.forEachBlock(minX, minY, maxX, maxY) {
            result = false
        }
        return result
    }

    override fun process(delta: Float) {
        if (Game.input.isButtonDown(0)) {
            val cursorX = WorldUtils.toCellCoord(Game.player.inputX, if (Game.player.inputX >= 0.0f) WorldUtils.CoordRounding.FLOOR else WorldUtils.CoordRounding.CEIL)
            val cursorY = WorldUtils.toCellCoord(Game.player.inputY, if (Game.player.inputY >= 0.0f) WorldUtils.CoordRounding.FLOOR else WorldUtils.CoordRounding.CEIL)

            y1 = if (cursorY < y0)
                cursorY
            else
                cursorY + 1

            x1 = if (cursorX < x0)
                cursorX
            else
                cursorX + 1

            if (x1 == x0)
                x1 = x0 + 1

            if (y1 == y0)
                y1 = y0 + 1

            val minX = min(if (x1 < x0) x0 + 1 else x0, x1)
            val minY = min(if (y1 < y0) y0 + 1 else y0, y1)
            val maxX = max(if (x1 < x0) x0 + 1 else x0, x1)
            val maxY = max(if (y1 < y0) y0 + 1 else y0, y1)

            Game.graphics2d.submit<NinepatchRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                it.ninePatch = blockPreviewNinePatch
                it.color = if (isBuildable(minX, minY, maxX, maxY)) Color.WHITE else Color.RED
                it.x = WorldUtils.toWorldCoord(minX)
                it.y = WorldUtils.toWorldCoord(minY)
                it.width = WorldUtils.toWorldCoord(maxX) - WorldUtils.toWorldCoord(minX)
                it.height = WorldUtils.toWorldCoord(maxY) - WorldUtils.toWorldCoord(minY)
            }
        } else {
            if (Game.input.isButtonJustUp(0)) {
                val minX = min(if (x1 < x0) x0 + 1 else x0, x1)
                val minY = min(if (y1 < y0) y0 + 1 else y0, y1)
                val maxX = max(if (x1 < x0) x0 + 1 else x0, x1)
                val maxY = max(if (y1 < y0) y0 + 1 else y0, y1)

                if (isBuildable(minX, minY, maxX, maxY))
                    worldScene.addBlock(minX, minY, maxX, maxY, WorldUtils.getRandomBlockColor())
            }

            x0 = WorldUtils.toCellCoord(Game.player.inputX, if (Game.player.inputX >= 0.0f) WorldUtils.CoordRounding.FLOOR else WorldUtils.CoordRounding.CEIL)
            y0 = WorldUtils.toCellCoord(Game.player.inputY, if (Game.player.inputY >= 0.0f) WorldUtils.CoordRounding.FLOOR else WorldUtils.CoordRounding.CEIL)
            x1 = x0 + 1
            y1 = y0 + 1

            Game.graphics2d.submit<NinepatchRenderable2D> {
                it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                it.ninePatch = blockPreviewNinePatch
                it.color = if (isBuildable(x0, y0, x0 + 1, y0 + 1)) Color.WHITE else Color.RED
                it.x = WorldUtils.toWorldCoord(x0)
                it.y = WorldUtils.toWorldCoord(y0)
                it.width = WorldConstants.WORLD_CELL_SIZE
                it.height = WorldConstants.WORLD_CELL_SIZE
            }
        }
    }
}