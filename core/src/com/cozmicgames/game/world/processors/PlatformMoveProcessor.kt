package com.cozmicgames.game.world.processors

import com.cozmicgames.game.Game
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.utils.maths.length
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.PlatformData
import com.dongbat.jbump.Collisions
import kotlin.math.abs

class PlatformMoveProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private val tempCollisions = Collisions()

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.PLAY
    }

    private fun updatePlatform(delta: Float, block: WorldBlock, platformData: PlatformData) {
        val amountX = (block.minX - platformData.fromMinX) / (platformData.toMinX - platformData.fromMinX)
        val amountY = (block.minY - platformData.fromMinY) / (platformData.toMinY - platformData.fromMinY)

        if ((platformData.fromMinX == platformData.toMinX || amountX <= 0.0f) && (platformData.fromMinY == platformData.toMinY || amountY <= 0.0f))
            platformData.currentMoveDirection = 1.0f

        if ((platformData.fromMinX == platformData.toMinX || amountX >= 1.0f) && (platformData.fromMinY == platformData.toMinY || amountY >= 1.0f))
            platformData.currentMoveDirection = -1.0f

        val blockWidth = block.width
        val blockHeight = block.height

        var deltaX = (platformData.toMinX - platformData.fromMinX) * platformData.currentMoveDirection
        var deltaY = (platformData.toMinY - platformData.fromMinY) * platformData.currentMoveDirection

        val normalizationFactor = 1.0f / length(deltaX, deltaY)
        deltaX *= normalizationFactor
        deltaY *= normalizationFactor

        deltaX *= delta * WorldConstants.PLATFORM_MOVE_SPEED
        deltaY *= delta * WorldConstants.PLATFORM_MOVE_SPEED

        val result = worldScene.physicsWorld.move(block.id, deltaX, deltaY)
        if (result != null) {
            platformData.playerBlockId?.let {
                worldScene.getBlockFromId(it)?.let {
                    val playerBlockWidth = it.width
                    val playerBlockHeight = it.height

                    it.minX += result.goalX - block.minX
                    it.minY += result.goalY - block.minY
                    it.maxX = it.minX + playerBlockWidth
                    it.maxY = it.minY + playerBlockHeight

                    it.updatePhysicsBlock()
                }
            }
        }

        val blockRect = worldScene.physicsWorld.getRect(block.id)!!
        block.minX = blockRect.x
        block.minY = blockRect.y
        block.maxX = block.minX + blockWidth
        block.maxY = block.minY + blockHeight

        platformData.playerBlockId = null
        worldScene.physicsWorld.project(block.id, 0.0f, 0.1f, tempCollisions)
        if (tempCollisions.size() > 0)
            repeat(tempCollisions.size()) {
                val collision = tempCollisions[it]
                val id = collision.other.userData as Int
                val collidingBlock = worldScene.getBlockFromId(id)
                if (collidingBlock is PlayerBlock)
                    platformData.playerBlockId = collidingBlock.id
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