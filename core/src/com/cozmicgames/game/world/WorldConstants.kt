package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.physics
import com.cozmicgames.game.physics.CollisionListener
import com.cozmicgames.game.physics.CollisionPair
import com.cozmicgames.game.player
import com.cozmicgames.game.world.dataValues.PlatformData
import kotlin.math.max
import kotlin.math.min

object WorldConstants {
    const val WORLD_CELL_SIZE = 64.0f
    const val RESIZE_BORDER_SIZE = 8.0f
    const val CLOUD_SIZE = 256.0f
    val SHADOW_COLOR = Color(0.2f, 0.2f, 0.2f, 0.5f)
    val SHADOW_OFFSET = Vector2(6.0f, 8.0f)
    val PLAYER_COLOR = Color.WHITE
    val GOAL_COLOR = WorldUtils.getColor(42.0f)
    const val CLOUD_SPEED = 10.0f
    const val CLOUD_Y = 750.0f
    const val CLOUD_Y_SPREAD = 800.0f
    const val CLOUD_SPEED_SPREAD = 0.5f
    const val CLOUDS_COUNT = 100

    const val WORLD_MIN_X = -100 * WORLD_CELL_SIZE
    const val WORLD_MAX_X = 100 * WORLD_CELL_SIZE
    const val WORLD_MIN_Y = 0.0f
    const val WORLD_WATER_Y = -WORLD_CELL_SIZE * 13.0f
    const val PLATFORM_MOVE_SPEED = 45.0f

    init {
        Game.physics.collisionListener = object : CollisionListener {
            override fun onCollision(collisionPair: CollisionPair) {
                if (collisionPair.a.userData is PlayerBlock && collisionPair.b.userData is GoalBlock || collisionPair.a.userData is GoalBlock && collisionPair.b.userData is GoalBlock) {
                    Game.player.onCompleteLevel()
                    return
                }

                lateinit var playerBlock: PlayerBlock
                lateinit var platformBlock: WorldBlock

                if (collisionPair.a.userData is WorldBlock) {
                    platformBlock = collisionPair.a.userData
                    if (collisionPair.b.userData is PlayerBlock)
                        playerBlock = collisionPair.b.userData
                    else {
                        collisionPair.a.positionX -= collisionPair.manifold.normal.x * collisionPair.manifold.penetration
                        collisionPair.a.positionY -= collisionPair.manifold.normal.y * collisionPair.manifold.penetration
                        platformBlock.getData<PlatformData>()?.let {
                            it.currentMoveDirection *= -1.0f
                        }
                        return
                    }
                } else if (collisionPair.b.userData is WorldBlock) {
                    platformBlock = collisionPair.b.userData
                    if (collisionPair.a.userData is PlayerBlock)
                        playerBlock = collisionPair.a.userData
                    else {
                        collisionPair.b.positionX += collisionPair.manifold.normal.x * collisionPair.manifold.penetration
                        collisionPair.b.positionY += collisionPair.manifold.normal.y * collisionPair.manifold.penetration
                        platformBlock.getData<PlatformData>()?.let {
                            it.currentMoveDirection *= -1.0f
                        }
                        return
                    }
                }

                val platformData = platformBlock.getData<PlatformData>() ?: return
                val playerCenterX = playerBlock.minX + playerBlock.width * 0.5f
                val playerCenterY = playerBlock.minY + playerBlock.height * 0.5f

                fun debugDraw(blockComponent: BlockComponent) = Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = RenderLayers.WORLD_LAYER_END
                    it.texture = "blank"
                    it.color = Color.RED
                    it.x = blockComponent.minX
                    it.y = blockComponent.minY
                    it.width = blockComponent.width
                    it.height = blockComponent.height
                }

                when {
                    playerCenterY > platformBlock.maxY && platformData.currentDeltaY > 0.0f -> {
                        // Platform beneath player, potentially squishing it into another block

                        val checkX = playerBlock.minX + WORLD_CELL_SIZE * 0.25f
                        val checkY = playerBlock.maxY
                        val checkWidth = playerBlock.width - WORLD_CELL_SIZE * 0.5f
                        val checkHeight = platformData.currentDeltaY

                        val collisions = Game.physics.getAllOverlappingRectangle(checkX, checkY, checkWidth, checkHeight, { it != playerBlock.body })

                        var deformAmount = 0.0f

                        collisions.forEach {
                            (it.userData as? WorldBlock)?.let { block ->
                                deformAmount = max(deformAmount, block.minY - playerBlock.maxY)
                            }
                        }

                        if (deformAmount != 0.0f && !playerBlock.deformY(deformAmount))
                            platformData.currentMoveDirection *= -1.0f
                    }

                    playerCenterY < platformBlock.minY && platformData.currentDeltaY < 0.0f -> {
                        // Platform above player, potentially squishing it down

                        val checkX = playerBlock.minX + WORLD_CELL_SIZE * 0.25f
                        val checkY = playerBlock.minY - platformData.currentDeltaY
                        val checkWidth = playerBlock.width - WORLD_CELL_SIZE * 0.5f
                        val checkHeight = platformData.currentDeltaY

                        val collisions = Game.physics.getAllOverlappingRectangle(checkX, checkY, checkWidth, checkHeight, { it != playerBlock.body })

                        var deformAmount = 0.0f

                        collisions.forEach {
                            (it.userData as? WorldBlock)?.let { block ->
                                deformAmount = min(deformAmount, playerBlock.minY - block.maxY)
                            }
                        }

                        if (deformAmount != 0.0f && !playerBlock.deformY(deformAmount))
                            platformData.currentMoveDirection *= -1.0f
                    }

                    playerCenterX > platformBlock.maxX && platformData.currentDeltaX > 0.0f -> {
                        // Platform to the left

                        val checkX = playerBlock.maxX
                        val checkY = playerBlock.minY + WORLD_CELL_SIZE * 0.25f
                        val checkWidth = platformData.currentDeltaX
                        val checkHeight = playerBlock.height - WORLD_CELL_SIZE * 0.5f

                        val collisions = Game.physics.getAllOverlappingRectangle(checkX, checkY, checkWidth, checkHeight, { it != playerBlock.body })

                        var deformAmount = 0.0f

                        collisions.forEach {
                            (it.userData as? WorldBlock)?.let { block ->
                                deformAmount = min(deformAmount, playerBlock.maxX - block.minX)
                            }
                        }

                        if (deformAmount != 0.0f && !playerBlock.deformX(deformAmount))
                            platformData.currentMoveDirection *= -1.0f
                    }

                    playerCenterX < platformBlock.minX && platformData.currentDeltaX < 0.0f -> {
                        // Platform to the right

                        val checkX = playerBlock.minX - platformData.currentDeltaX
                        val checkY = playerBlock.minY + WORLD_CELL_SIZE * 0.25f
                        val checkWidth = platformData.currentDeltaX
                        val checkHeight = playerBlock.height - WORLD_CELL_SIZE * 0.5f

                        val collisions = Game.physics.getAllOverlappingRectangle(checkX, checkY, checkWidth, checkHeight, { it != playerBlock.body })

                        var deformAmount = 0.0f

                        collisions.forEach {
                            (it.userData as? WorldBlock)?.let { block ->
                                deformAmount = min(deformAmount, playerBlock.minX - block.maxX)
                            }
                        }

                        if (deformAmount != 0.0f && !playerBlock.deformX(deformAmount))
                            platformData.currentMoveDirection *= -1.0f
                    }
                }

                val manifold = Game.physics.getCollisionManifold(collisionPair.a, collisionPair.b)
                if (manifold != null) {
                    collisionPair.manifold = manifold
                    collisionPair.shouldCollide = true
                } else
                    collisionPair.shouldCollide = false
            }
        }
    }
}