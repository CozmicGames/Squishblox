package com.cozmicgames.game.world.processors

import com.cozmicgames.game.Game
import com.cozmicgames.game.physics
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.common.utils.maths.length
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.PlatformData

class PlatformMoveProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState != PlayState.EDIT
    }

    private fun updatePlatform(delta: Float, block: WorldBlock, platformData: PlatformData) {
        val amountX = (block.minX - platformData.fromMinX) / (platformData.toMinX - platformData.fromMinX)
        val amountY = (block.minY - platformData.fromMinY) / (platformData.toMinY - platformData.fromMinY)

        if ((platformData.fromMinX == platformData.toMinX || amountX <= 0.0f) && (platformData.fromMinY == platformData.toMinY || amountY <= 0.0f))
            platformData.currentMoveDirection = 1.0f

        if ((platformData.fromMinX == platformData.toMinX || amountX >= 1.0f) && (platformData.fromMinY == platformData.toMinY || amountY >= 1.0f))
            platformData.currentMoveDirection = -1.0f

        var deltaX = (platformData.toMinX - platformData.fromMinX) * platformData.currentMoveDirection
        var deltaY = (platformData.toMinY - platformData.fromMinY) * platformData.currentMoveDirection

        val normalizationFactor = 1.0f / length(deltaX, deltaY)
        deltaX *= normalizationFactor
        deltaY *= normalizationFactor

        deltaX *= delta * WorldConstants.PLATFORM_MOVE_SPEED
        deltaY *= delta * WorldConstants.PLATFORM_MOVE_SPEED

        platformData.currentDeltaX = deltaX
        platformData.currentDeltaY = deltaY

        block.body.positionX += deltaX
        block.body.positionY += deltaY

        platformData.playerBlockId?.let { id ->
            val playerBlock = worldScene.getBlockFromId(id)!!
            playerBlock.body.positionX += deltaX
            playerBlock.body.positionY += deltaY
        }

        platformData.playerBlockId = null

        Game.physics.forEachOverlappingRectangle(block.minX, block.maxY, block.width, 1.0f) {
            if (it.userData is PlayerBlock)
                platformData.playerBlockId = it.userData.id
        }
    }

    override fun process(delta: Float) {
        worldScene.findGameObjectsWithComponent<WorldBlock> {
            it.getComponent<WorldBlock>()?.let { block ->
                block.getData<PlatformData>()?.let { platformData ->
                    updatePlatform(delta, block, platformData)
                }
            }
        }
    }
}