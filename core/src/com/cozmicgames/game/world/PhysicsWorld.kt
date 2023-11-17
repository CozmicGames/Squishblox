package com.cozmicgames.game.world

import com.badlogic.gdx.utils.IntMap
import com.cozmicgames.game.world.dataValues.PlatformData
import com.dongbat.jbump.*
import com.dongbat.jbump.World

class PhysicsWorld(private val worldScene: WorldScene) : Iterable<Int> {
    private val deform = object : Response {
        override fun response(world: World<*>, collision: Collision, x: Float, y: Float, w: Float, h: Float, goalX: Float, goalY: Float, filter: CollisionFilter, result: Response.Result): Response.Result {
            return Response.touch.response(world, collision, x, y, w, h, goalX, goalY, filter, result)



            return result
        }
    }

    private val world = World<Int>(WorldConstants.WORLD_CELL_SIZE)
    private val items = IntMap<Item<Int>>()
    private val collisionFilter = CollisionFilter { item, other ->
        var result = Response.slide
        if (other.userData is Int)
            worldScene.getBlockFromId(other.userData as Int)?.let { otherBlock ->
                if (otherBlock is PlayerBlock) {
                    worldScene.getBlockFromId(item.userData as Int)?.let { block ->
                        if (block is WorldBlock)
                            block.getData<PlatformData>()?.let { platformData ->
                                result = if (platformData.playerBlockId != otherBlock.id) {
                                    deform
                                } else
                                    null
                            }
                    }
                }
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
        return world.move(item, rect.x + amountX, rect.y + amountY, collisionFilter)
    }

    fun project(id: Int, amountX: Float, amountY: Float, collisions: Collisions) {
        val item = items.get(id) ?: return
        val rect = world.getRect(item)
        world.project(item, rect.x, rect.y, rect.w, rect.h, if (amountX <= 0.0f) rect.x + amountX else rect.x + rect.w + amountX, if (amountY <= 0.0f) rect.y + amountY else rect.y + rect.w + amountY, collisionFilter, collisions)
    }

    fun getRect(id: Int): Rect? {
        val item = items.get(id) ?: return null
        return world.getRect(item)
    }
}