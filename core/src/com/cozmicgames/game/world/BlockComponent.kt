package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.Game
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.Component
import com.cozmicgames.game.scene.components.TransformComponent
import com.cozmicgames.game.scene.findGameObjectByComponent
import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.Reflection
import com.cozmicgames.game.utils.Updatable
import com.cozmicgames.game.utils.maths.randomFloat
import com.cozmicgames.game.utils.serialization.Readable
import com.dongbat.jbump.Collisions
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlin.reflect.KClass

sealed class BlockComponent : Component() {
    protected val transformComponent by lazy { gameObject.getOrAddComponent<TransformComponent>() }

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

    val width get() = maxX - minX

    val height get() = maxY - minY

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

abstract class EntityBlock : Updatable, BlockComponent() {
    var isFacingRight = true

    var isBlinking = false
        private set

    private var blinkTimer = 5.0f

    override fun update(delta: Float) {
        if (Game.player.playState != PlayState.EDIT)
            blinkTimer -= delta

        if (blinkTimer <= 0.15f)
            isBlinking = true

        if (blinkTimer <= 0.0f) {
            isBlinking = false
            blinkTimer = 0.5f + randomFloat() * 5.0f
        }
    }
}

class PlayerBlock : EntityBlock() {
    var deltaX = 0.0f
    var deltaY = 0.0f

    private val tempCollisionsA = Collisions()
    private val tempCollisionsB = Collisions()

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

        updatePhysicsBlock()

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

        updatePhysicsBlock()

        return remainingAmount
    }

    fun adjustWidth(newMinX: Float, newMaxX: Float) {
        val area = (maxX - minX) * (maxY - minY)
        val width = newMaxX - newMinX
        val height = area / width
        maxY = minY + height
        minX = newMinX
        maxX = newMaxX
        updatePhysicsBlock()
    }

    fun adjustHeight(newMinY: Float, newMaxY: Float) {
        val area = (maxX - minX) * (maxY - minY)
        val height = newMaxY - newMinY
        val width = area / height
        maxX = minX + width
        minY = newMinY
        maxY = newMaxY
        updatePhysicsBlock()
    }

    fun deformY(amount: Float): Float {
        val availableDeformY = height - WorldConstants.WORLD_CELL_SIZE
        val adjustedAmount = min(abs(amount), availableDeformY)

        val widthDeformFactor = width / height

        val amountLeft = -adjustedAmount * widthDeformFactor
        val amountRight = adjustedAmount * widthDeformFactor

        worldScene.physicsWorld.project(id, amountLeft, 0.0f, tempCollisionsA)
        worldScene.physicsWorld.project(id, amountRight, 0.0f, tempCollisionsB)

        if (tempCollisionsA.size() == 0 && tempCollisionsB.size() == 0) {
            adjustWidth(minX + amountLeft, maxX + amountRight)
            return adjustedAmount * sign(amount)
        } else {

        }

        return 0.0f
    }

    fun deformX(amount: Float): Float {
        val availableDeformX = width - WorldConstants.WORLD_CELL_SIZE
        val adjustedAmount = min(abs(amount), availableDeformX)

        val heightDeformFactor = height / width
        val heightAmount = adjustedAmount * heightDeformFactor

        worldScene.physicsWorld.project(id, 0.0f, heightAmount, tempCollisionsA)

        if (tempCollisionsA.size() == 0) {
            adjustHeight(minY, maxY + heightAmount)
            return adjustedAmount * sign(amount)
        } else {

        }

        return 0.0f
    }
}

class GoalBlock : EntityBlock() {
    override fun update(delta: Float) {
        super.update(delta)

        val playerBlock = worldScene.findGameObjectByComponent<PlayerBlock> { true }
        isFacingRight = (playerBlock?.getComponent<PlayerBlock>()?.minX ?: Float.MAX_VALUE) >= minX
    }
}
