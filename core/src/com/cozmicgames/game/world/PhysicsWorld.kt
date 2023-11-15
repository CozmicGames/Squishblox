package com.cozmicgames.game.world

import com.badlogic.gdx.utils.IntMap
import com.dongbat.jbump.CollisionFilter
import com.dongbat.jbump.Collisions
import com.dongbat.jbump.Item
import com.dongbat.jbump.Rect
import com.dongbat.jbump.Response
import com.dongbat.jbump.World

class PhysicsWorld : Iterable<Int> {
    private val world = World<Int>(WorldConstants.WORLD_CELL_SIZE)
    private val items = IntMap<Item<Int>>()
    private val collisionFilter = CollisionFilter { item, other ->
        Response.slide
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