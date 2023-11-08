package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Component
import com.cozmicgames.game.scene.components.TransformComponent

sealed class BlockComponent : Component() {
    private val transformComponent by lazy { gameObject.getOrAddComponent<TransformComponent>() }

    val color = Color()

    lateinit var world: World

    var id = -1

    var minX
        get() = transformComponent.transform.x
        set(value) {
            transformComponent.transform.x = value
        }

    var minY
        get() = transformComponent.transform.y
        set(value) {
            transformComponent.transform.y = value
        }

    var maxX
        get() = transformComponent.transform.x + transformComponent.transform.scaleX
        set(value) {
            transformComponent.transform.scaleX = value - transformComponent.transform.x
        }

    var maxY
        get() = transformComponent.transform.y + transformComponent.transform.scaleY
        set(value) {
            transformComponent.transform.scaleY = value - transformComponent.transform.y
        }

    override fun onAdded() {
        if (id >= 0)
            world.removeBlock(id)

        val cellMinX = WorldUtils.toCellCoord(minX)
        val cellMinY = WorldUtils.toCellCoord(minY)
        val cellMaxX = WorldUtils.toCellCoord(maxX)
        val cellMaxY = WorldUtils.toCellCoord(maxY)

        id = world.createBlock(cellMinX, cellMinY, cellMaxX, cellMaxY)
    }

    override fun onRemoved() {
        world.removeBlock(id)
        id = -1
    }
}

class WorldBlockComponent : BlockComponent()

class PlayerBlockComponent : BlockComponent() {
    fun addSize(amount: Float) {
        if ((maxX - minX) > (maxY - minY))
            maxX += amount
        else
            maxY += amount
    }

    fun adjustWidth(newMinX: Float, newMaxX: Float) {
        val area = (maxX - minX) * (maxY - minY)
        val width = newMaxX - newMinX
        val height = area / width
        maxY = minY + height
        minX = newMinX
        maxX = newMaxX
    }

    fun adjustHeight(newMinY: Float, newMaxY: Float) {
        val area = (maxX - minX) * (maxY - minY)
        val height = newMaxY - newMinY
        val width = area / height
        maxX = minX + width
        minY = newMinY
        maxY = newMaxY
    }
}
