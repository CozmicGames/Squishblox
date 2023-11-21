package com.cozmicgames.game.world

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.IntMap
import com.cozmicgames.game.world.dataValues.PlatformData
import com.dongbat.jbump.*
import com.dongbat.jbump.World
import kotlin.math.abs

class PhysicsWorld(private val worldScene: WorldScene) : Iterable<Int> {
    private val platform = object : Response {
        private val tempCollisions = Collisions()

        private fun findNearestCollision(collisions: Collisions): Collision {
            var nearestCollisionIndex = 0
            repeat(collisions.size() - 1) {
                if (abs(collisions[it + 1].touch.y) < abs(collisions[nearestCollisionIndex].touch.y))
                    nearestCollisionIndex = it + 1
            }
            return collisions[nearestCollisionIndex]
        }

        override fun response(world: World<*>, collision: Collision, x: Float, y: Float, w: Float, h: Float, goalX: Float, goalY: Float, filter: CollisionFilter, result: Response.Result): Response.Result {
            val id = collision.item.userData as Int
            val block = worldScene.getBlockFromId(id)
            val otherId = collision.other.userData as Int
            val playerBlock = worldScene.getBlockFromId(otherId)

            if (playerBlock is PlayerBlock) {
                val moveAmountX = goalX - x
                val moveAmountY = goalY - y

                if (moveAmountX > 0.0f && playerBlock.minX < x || moveAmountX < 0.0f && playerBlock.minX > x || moveAmountY > 0.0f && playerBlock.minY < y || moveAmountY < 0.0f && playerBlock.minY > y) {
                    result.projectedCollisions.clear()
                    result.set(goalX, goalY)
                    return result
                }

                if (collision.normal.x == 0) {
                    project(playerBlock.id, 0.0f, moveAmountY, tempCollisions) {
                        it != collision.item.userData as Int
                    }

                    if (tempCollisions.size() == 0) {
                        move(otherId, 0.0f, moveAmountY)
                        result.projectedCollisions.clear()
                        result.set(goalX, goalY)
                        return result
                    } else {
                        val nearestCollision = findNearestCollision(tempCollisions)

                        var moveY = if (collision.normal.y > 0)
                            y - playerBlock.height - (nearestCollision.otherRect.y + nearestCollision.otherRect.h)
                        else
                            nearestCollision.otherRect.y - (collision.itemRect.y + collision.itemRect.h + playerBlock.height)

                        move(otherId, 0.0f, moveY)

                        moveY += playerBlock.deformY(moveAmountY - moveY)

                        if (!MathUtils.isEqual(y + moveY, goalY))
                            (block as? WorldBlock)?.let {
                                it.getData<PlatformData>()?.let {
                                    it.currentMoveDirection *= -1.0f
                                }
                            }

                        result.projectedCollisions.clear()
                        result.set(goalX, y + moveY)
                        return result
                    }
                } else {
                    project(playerBlock.id, moveAmountX, 0.0f, tempCollisions)

                    if (tempCollisions.size() == 0) {
                        move(otherId, moveAmountX, 0.0f)
                        result.projectedCollisions.clear()
                        result.set(goalX, goalY)
                        return result
                    } else {
                        val nearestCollision = findNearestCollision(tempCollisions)

                        var moveX = if (collision.normal.x > 0)
                            x - playerBlock.width - (nearestCollision.otherRect.x + nearestCollision.otherRect.w)
                        else
                            nearestCollision.otherRect.x - (collision.itemRect.x + collision.itemRect.w + playerBlock.width)

                        move(otherId, moveX, 0.0f)

                        moveX += playerBlock.deformX(moveAmountX - moveX)

                        if (!MathUtils.isEqual(x + moveX, goalX, 1.0f))
                            (block as? WorldBlock)?.let {
                                it.getData<PlatformData>()?.let {
                                    it.currentMoveDirection *= -1.0f
                                }
                            }

                        result.projectedCollisions.clear()
                        result.set(x + moveX, goalY)
                        return result
                    }
                }
            }

            Response.touch.response(world, collision, x, y, w, h, goalX, goalY, filter, result)

            if (!MathUtils.isEqual(goalX, result.goalX) || !MathUtils.isEqual(goalY, result.goalY))
                (block as? WorldBlock)?.getData<PlatformData>()?.let {
                    it.currentMoveDirection *= -1.0f
                }

            return result
        }
    }

    private val world = World<Int>(WorldConstants.WORLD_CELL_SIZE)
    private val items = IntMap<Item<Int>>()
    private val moveCollisionFilter = CollisionFilter { item, other ->
        var result = Response.slide
        if (item.userData is Int && other.userData is Int) {
            val block = worldScene.getBlockFromId(item.userData as Int)

            if (block is WorldBlock && block.getData<PlatformData>() != null)
                result = platform
        }

        result
    }

    override fun iterator(): Iterator<Int> {
        return items.values().map { it.userData }.iterator()
    }

    fun createBlock(id: Int, minX: Float, minY: Float, maxX: Float, maxY: Float) {
        val item = Item(id)
        world.add(item, minX, minY, maxX - minX, maxY - minY)
        items.put(id, item)
    }

    fun updateBlock(id: Int, minX: Float, minY: Float, maxX: Float, maxY: Float) {
        val item = items.get(id) ?: return
        world.update(item, minX, minY, maxX - minX, maxY - minY)
    }

    fun removeBlock(id: Int) {
        val item = items.remove(id) ?: return
        world.remove(item)
    }

    fun move(id: Int, amountX: Float, amountY: Float): Response.Result? {
        val item = items.get(id) ?: return null
        val rect = world.getRect(item)
        return world.move(item, rect.x + amountX, rect.y + amountY, moveCollisionFilter)
    }

    fun project(id: Int, amountX: Float, amountY: Float, collisions: Collisions, filter: (Int) -> Boolean = { true }) {
        val item = items.get(id) ?: return
        val rect = world.getRect(item)
        world.project(item, rect.x, rect.y, rect.w, rect.h, rect.x + amountX, rect.y + amountY, { _, other ->
            if (!filter(other.userData as Int) || other == item)
                null
            else
                Response.slide
        }, collisions)
    }

    fun getRect(id: Int): Rect? {
        val item = items.get(id) ?: return null
        return world.getRect(item)
    }
}