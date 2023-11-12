package com.cozmicgames.game.world

import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectByComponent
import kotlin.math.max
import kotlin.math.min

class BlockCreateProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private var x0 = 0
    private var y0 = 0
    private var x1 = 0
    private var y1 = 0

    override fun shouldProcess(delta: Float): Boolean {
        return worldScene.editState == WorldScene.EditState.CREATE
    }

    override fun process(delta: Float) {
        val scene = this.scene ?: return
        val blockPreviewComponent = scene.findGameObjectByComponent<BlockPreviewComponent> { true }?.getComponent<BlockPreviewComponent>() ?: return

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

            blockPreviewComponent.minX = WorldUtils.toWorldCoord(minX)
            blockPreviewComponent.minY = WorldUtils.toWorldCoord(minY)
            blockPreviewComponent.maxX = WorldUtils.toWorldCoord(maxX)
            blockPreviewComponent.maxY = WorldUtils.toWorldCoord(maxY)
        } else {
            if (Game.input.isButtonJustUp(0))
                if (blockPreviewComponent.isBuildable) {
                    val minX = min(if (x1 < x0) x0 + 1 else x0, x1)
                    val minY = min(if (y1 < y0) y0 + 1 else y0, y1)
                    val maxX = max(if (x1 < x0) x0 + 1 else x0, x1)
                    val maxY = max(if (y1 < y0) y0 + 1 else y0, y1)
                    worldScene.addBlock(minX, minY, maxX, maxY, WorldUtils.getRandomBlockColor())
                }

            x0 = WorldUtils.toCellCoord(Game.player.inputX, if (Game.player.inputX >= 0.0f) WorldUtils.CoordRounding.FLOOR else WorldUtils.CoordRounding.CEIL)
            y0 = WorldUtils.toCellCoord(Game.player.inputY, if (Game.player.inputY >= 0.0f) WorldUtils.CoordRounding.FLOOR else WorldUtils.CoordRounding.CEIL)

            blockPreviewComponent.minX = WorldUtils.toWorldCoord(x0)
            blockPreviewComponent.minY = WorldUtils.toWorldCoord(y0)
            blockPreviewComponent.maxX = WorldUtils.toWorldCoord(x0 + 1)
            blockPreviewComponent.maxY = WorldUtils.toWorldCoord(y0 + 1)
        }
    }
}