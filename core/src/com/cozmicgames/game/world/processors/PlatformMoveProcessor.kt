package com.cozmicgames.game.world.processors

import com.cozmicgames.game.Game
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.utils.extensions.clamp
import com.cozmicgames.game.utils.maths.distance
import com.cozmicgames.game.utils.maths.lerp
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.PlatformData
import com.dongbat.jbump.Collisions

class PlatformMoveProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private val tempCollisions = Collisions()

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

        val deltaX = currentMinX - block.minX
        val deltaY = currentMinY - block.minY

        val result = worldScene.physicsWorld.move(block.id, deltaX, deltaY)
        if (result != null) {
            platformData.playerBlockId?.let {
                worldScene.getBlockFromId(it)?.let {
                    val playerBlockWidth = it.maxX - it.minX
                    val playerBlockHeight = it.maxY - it.minY

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