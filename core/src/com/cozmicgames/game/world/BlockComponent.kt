package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Component
import com.cozmicgames.game.scene.components.TransformComponent
import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.Reflection
import com.cozmicgames.game.utils.serialization.Readable
import kotlin.reflect.KClass

sealed class BlockComponent : Component() {
    private val transformComponent by lazy { gameObject.getOrAddComponent<TransformComponent>() }

    val color = Color()
    var id = -1

    protected val worldScene get() = gameObject.scene as WorldScene

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
            worldScene.world.removeBlock(id)

        val cellMinX = WorldUtils.toCellCoord(minX)
        val cellMinY = WorldUtils.toCellCoord(minY)
        val cellMaxX = WorldUtils.toCellCoord(maxX)
        val cellMaxY = WorldUtils.toCellCoord(maxY)

        id = worldScene.world.createBlock(cellMinX, cellMinY, cellMaxX, cellMaxY)
        worldScene.physicsWorld.createBlock(id, minX, minY, maxX, maxY)
    }

    override fun onRemoved() {
        worldScene.world.removeBlock(id)
        worldScene.physicsWorld.updateBlock(id, minX, minY, maxX, maxY)
        id = -1
    }

    fun updatePhysicsBlock() {
        worldScene.physicsWorld.updateBlock(id, minX, minY, maxX, maxY)
    }
}

open class WorldBlock : BlockComponent() {
    private var dataValues: MutableMap<KClass<*>, Readable>? = null

    inline fun <reified T : Readable> getData() = getData(T::class)

    fun <T : Readable> getData(type: KClass<T>): T? {
        return dataValues?.get(type) as? T
    }

    inline fun <reified T : Readable> setData(value: T) = setData(value, T::class)

    fun <T : Readable> setData(value: T, type: KClass<T>) {
        if (dataValues == null)
            dataValues = hashMapOf()
        dataValues!![type] = value
    }

    inline fun <reified T : Readable> removeData() = removeData(T::class)

    fun <T : Readable> removeData(type: KClass<T>) {
        dataValues?.remove(type)
    }

    override fun read(properties: Properties) {
        properties.getPropertiesArray("dataValues")?.let {
            for (dataValueProperties in it) {
                val typeName = dataValueProperties.getString("typeName") ?: continue
                val type = Reflection.getClassByName(typeName) as? KClass<Readable> ?: continue
                val dataValue = Reflection.createInstance(type) ?: continue
                dataValue.read(dataValueProperties)
                setData(dataValue, type)
            }
        }
    }

    override fun write(properties: Properties) {
        dataValues?.let {
            val dataValuesProperties = arrayListOf<Properties>()
            it.forEach { (_, value) ->
                val dataValueProperties = Properties()
                dataValueProperties.setString("typeName", Reflection.getClassName(value::class))
                value.write(dataValueProperties)
                dataValuesProperties += dataValueProperties
            }
            properties.setPropertiesArray("dataValues", dataValuesProperties.toTypedArray())
        }
    }
}

class PlayerBlock : BlockComponent() {
    var deltaX = 0.0f
    var deltaY = 0.0f
    var gravityX = 0.0f
    var gravityY = -WorldConstants.GRAVITY

    fun addSize(amount: Float) {
        if ((maxX - minX) > (maxY - minY)) {
            var remainingAmount = addWidth(amount)
            if (remainingAmount > 0.0f) {
                remainingAmount = addHeight(remainingAmount)
                if (remainingAmount > 0.0f) {
                    //TODO: Blow up player
                }
            }
        } else {
            var remainingAmount = addHeight(amount)
            if (remainingAmount > 0.0f) {
                remainingAmount = addWidth(remainingAmount)
                if (remainingAmount > 0.0f) {
                    //TODO: Blow up player
                }
            }
        }
    }

    private fun addWidth(amount: Float): Float {
        val widthToAdd = amount * ((maxX - minX) / (maxY - minY))

        var newMaxX = maxX + widthToAdd
        val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(maxX), WorldUtils.toCellCoord(minY), WorldUtils.toCellCoord(newMaxX), WorldUtils.toCellCoord(maxY)) { it != id }
        if (collidingBlocks.isNotEmpty())
            newMaxX = collidingBlocks.minOf { (gameObject.scene as? WorldScene)?.getBlockFromId(it)?.minX ?: Float.MAX_VALUE }

        val remainingAmount = (maxX + widthToAdd - newMaxX) * ((maxY - minY) / (maxX - minX))
        maxX = newMaxX

        return remainingAmount
    }

    private fun addHeight(amount: Float): Float {
        val heightToAdd = amount * ((maxY - minY) / (maxX - minX))

        var newMaxY = maxY + heightToAdd
        val collidingBlocks = worldScene.world.getBlocks(WorldUtils.toCellCoord(minX), WorldUtils.toCellCoord(maxY), WorldUtils.toCellCoord(maxX), WorldUtils.toCellCoord(newMaxY)) { it != id }
        if (collidingBlocks.isNotEmpty())
            newMaxY = collidingBlocks.minOf { (gameObject.scene as? WorldScene)?.getBlockFromId(it)?.minY ?: Float.MAX_VALUE }

        val remainingAmount = (maxY + heightToAdd - newMaxY) * ((maxX - minX) / (maxY - minY))
        maxY = newMaxY

        return remainingAmount
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
