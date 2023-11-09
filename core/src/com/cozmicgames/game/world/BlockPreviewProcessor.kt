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

        if (Game.input.justTouchedUp) {
            x1 = WorldUtils.toCellCoord(Game.player.inputX)
            y1 = WorldUtils.toCellCoord(Game.player.inputY)

            val minX = min(x0, x1)
            val minY = min(y0, y1)
            val maxX = max(x0, x1)
            val maxY = max(y0, y1)

            if (blockPreviewComponent.isBuildable)
                world.addBlock(minX, minY, maxX, maxY, WorldUtils.getRandomBlockColor())
        }

        if (Game.input.isTouched) {
            x1 = WorldUtils.toCellCoord(Game.player.inputX)
            y1 = WorldUtils.toCellCoord(Game.player.inputY)

            val minX = min(x0, x1)
            val minY = min(y0, y1)
            val maxX = max(x0, x1)
            val maxY = max(y0, y1)

            blockPreviewComponent.minX = WorldUtils.toWorldCoord(minX)
            blockPreviewComponent.minY = WorldUtils.toWorldCoord(minY)
            blockPreviewComponent.maxX = WorldUtils.toWorldCoord(maxX)
            blockPreviewComponent.maxY = WorldUtils.toWorldCoord(maxY)
        } else {
            if (Game.input.justTouchedDown) {
                x0 = WorldUtils.toCellCoord(Game.player.inputX)
                y0 = WorldUtils.toCellCoord(Game.player.inputY)
                x1 = x0 + 1
                y1 = y0 + 1
            }

            blockPreviewComponent.minX = WorldUtils.roundWorldToCellCoord(Game.player.inputX)
            blockPreviewComponent.minY = WorldUtils.roundWorldToCellCoord(Game.player.inputY)
            blockPreviewComponent.maxX = blockPreviewComponent.minX + WorldConstants.WORLD_CELL_SIZE
            blockPreviewComponent.maxY = blockPreviewComponent.minY + WorldConstants.WORLD_CELL_SIZE
        }
    }
}