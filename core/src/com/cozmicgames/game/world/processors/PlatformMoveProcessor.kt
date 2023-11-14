package com.cozmicgames.game.world.processors

import com.cozmicgames.game.Game
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.utils.extensions.clamp
import com.cozmicgames.game.utils.maths.distance
import com.cozmicgames.game.utils.maths.lerp
import com.cozmicgames.game.world.PlayState
import com.cozmicgames.game.world.WorldBlock
import com.cozmicgames.game.world.WorldConstants
import com.cozmicgames.game.world.WorldScene
import com.cozmicgames.game.world.dataValues.PlatformData

class PlatformMoveProcessor : SceneProcessor() {
    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.PLAY
    }

    private fun updatePlatform(delta: Float, block: WorldBlock, platformData: PlatformData) {
        platformData.currentPosition += platformData.currentMoveDirection * delta * WorldConstants.PLATFORM_MOVE_SPEED / distance(platformData.fromMinX, platformData.fromMinY, platformData.toMinX, platformData.toMinY)

        if (platformData.currentPosition >= 1.0f)
            platformData.currentMoveDirection = -1.0f

        if (platformData.currentPosition <= 0.0f)
            platformData.currentMoveDirection = 1.0f

        platformData.currentPosition = platformData.currentPosition.clamp(0.0f, 1.0f)

        val currentMinX = lerp(platformData.fromMinX, platformData.toMinX, platformData.currentPosition)
        val currentMinY = lerp(platformData.fromMinY, platformData.toMinY, platformData.currentPosition)

        val blockWidth = block.maxX - block.minX
        val blockHeight = block.maxY - block.minY

        block.minX = currentMinX
        block.minY = currentMinY
        block.maxX = currentMinX + blockWidth
        block.maxY = currentMinY + blockHeight
    }

    override fun process(delta: Float) {
        val scene = this.scene as? WorldScene ?: return

        scene.findGameObjectsWithComponent<WorldBlock> {
            it.getComponent<WorldBlock>()?.let { block ->
                block.getData<PlatformData>()?.let { platformData ->
                    updatePlatform(delta, block, platformData)
                }
            }
        }
    }
}