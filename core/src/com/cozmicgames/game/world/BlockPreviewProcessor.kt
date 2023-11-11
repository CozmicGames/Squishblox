package com.cozmicgames.game.world

import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectByComponent
import kotlin.math.max
import kotlin.math.min

class BlockPreviewProcessor(val world: WorldScene) : SceneProcessor() {
    private var x0 = 0
    private var y0 = 0
    private var x1 = 0
    private var y1 = 0

    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    override fun process(delta: Float) {
        val scene = this.scene ?: return
        val blockPreviewComponent = scene.findGameObjectByComponent<BlockPreviewComponent> { true }?.getComponent<BlockPreviewComponent>() ?: return

        if (Game.input.isTouched) {
            val cursorX = WorldUtils.toCellCoord(Game.player.inputX, WorldUtils.CoordRounding.FLOOR)
            val cursorY = WorldUtils.toCellCoord(Game.player.inputY, WorldUtils.CoordRounding.FLOOR)

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
            if (Game.input.justTouchedUp)
                if (blockPreviewComponent.isBuildable) {
                    val minX = min(if (x1 < x0) x0 + 1 else x0, x1)
                    val minY = min(if (y1 < y0) y0 + 1 else y0, y1)
                    val maxX = max(if (x1 < x0) x0 + 1 else x0, x1)
                    val maxY = max(if (y1 < y0) y0 + 1 else y0, y1)
                    world.addBlock(minX, minY, maxX, maxY, WorldUtils.getRandomBlockColor())
                }

            x0 = WorldUtils.toCellCoord(Game.player.inputX, WorldUtils.CoordRounding.FLOOR)
            y0 = WorldUtils.toCellCoord(Game.player.inputY, WorldUtils.CoordRounding.FLOOR)

            blockPreviewComponent.minX = WorldUtils.toWorldCoord(x0)
            blockPreviewComponent.minY = WorldUtils.toWorldCoord(y0)
            blockPreviewComponent.maxX = WorldUtils.toWorldCoord(x0 + 1)
            blockPreviewComponent.maxY = WorldUtils.toWorldCoord(y0 + 1)
        }
    }
}