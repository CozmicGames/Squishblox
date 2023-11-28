package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.Game
import com.cozmicgames.game.physics
import com.cozmicgames.game.physics.AxisAlignedRectangleShape
import com.cozmicgames.game.physics.Body
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
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

    private var bodyInternal: Body? = null

    val body get() = bodyInternal!!

    override fun onAdded() {
        if (id >= 0)
            worldScene.world.removeBlock(id)

        val cellMinX = WorldUtils.toCellCoord(minX)
        val cellMinY = WorldUtils.toCellCoord(minY)
        val cellMaxX = WorldUtils.toCellCoord(maxX)
        val cellMaxY = WorldUtils.toCellCoord(maxY)

        id = worldScene.world.createBlock(cellMinX, cellMinY, cellMaxX, cellMaxY)

        var body = this.bodyInternal
        if (body != null)
            Game.physics.removeBody(body)

        body = Body(transformComponent.transform, this)
        body.dynamicFriction = 0.0f
        body.staticFriction = 0.0f
        body.setShape(AxisAlignedRectangleShape(), 100.0f)
        body.calculateMassAndInertia()

        Game.physics.addBody(body)
        this.bodyInternal = body
    }

    override fun onRemoved() {
        worldScene.world.removeBlock(id)
        bodyInternal?.let {
            Game.physics.removeBody(it)
        }
        id = -1
    }

    override fun write(properties: Properties) {
        properties.setFloatArray("color", arrayOf(color.r, color.g, color.b))
    }

    override fun read(properties: Properties) {
        properties.getFloatArray("color")?.let { color.set(it[0], it[1], it[2], 1.0f) }
    }
}

open class WorldBlock : BlockComponent() {
    private var dataValues: MutableMap<KClass<*>, Readable>? = null

    override fun onAdded() {
        super.onAdded()
        body.setStatic()
    }

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
        super.read(properties)
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
        super.write(properties)
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
    override fun onAdded() {
        super.onAdded()

        body.calculateMassAndInertia()
        body.restitution = 0.0f
    }

    fun calculateJumpForce(baseJumpForce: Float): Float {
        return baseJumpForce //TODO: Change regarding width to height ratio, but not too extremely
    }

    fun scale(amount: Float) {
        if ((maxX - minX) > (maxY - minY)) {
            val remainingAmount = scaleWidth(amount)
            if (remainingAmount > 0.0f)
                scaleHeight(remainingAmount)
        } else {
            val remainingAmount = scaleHeight(amount)
            if (remainingAmount > 0.0f)
                scaleWidth(remainingAmount)
        }
    }

    private fun scaleWidth(amount: Float): Float {
        val scaleFactor = width / height
        var adjustedAmount = amount * scaleFactor

        if (amount < 0.0f) {
            val minWidth = (WorldConstants.WORLD_CELL_SIZE * WorldConstants.WORLD_CELL_SIZE) / height
            adjustedAmount = max(adjustedAmount, width - minWidth)
            minX += adjustedAmount * 0.5f
            maxX -= adjustedAmount * 0.5f
            return 0.0f
        }

        val checkY = minY + WorldConstants.WORLD_CELL_SIZE * 0.5f
        val checkHeight = height - WorldConstants.WORLD_CELL_SIZE

        var amountLeft = adjustedAmount * 0.5f
        var amountRight = adjustedAmount * 0.5f

        val collisionsLeft = Game.physics.getAllOverlappingRectangle(minX - amountLeft, checkY, amountLeft, checkHeight, { it != body })
        val collisionsRight = Game.physics.getAllOverlappingRectangle(maxX, checkY, amountRight, checkHeight, { it != body })

        var maxAmountLeft = amountLeft
        var maxAmountRight = amountLeft

        collisionsLeft.forEach {
            (it.userData as? WorldBlock)?.let {
                maxAmountLeft = min(maxAmountLeft, minX - it.maxX)
            }
        }

        collisionsRight.forEach {
            (it.userData as? WorldBlock)?.let {
                maxAmountRight = min(maxAmountRight, it.minX - maxX)
            }
        }

        val additionalAmountRight = if (amountLeft > maxAmountLeft) amountLeft - maxAmountLeft else 0.0f
        val additionalAmountLeft = if (amountRight > maxAmountRight) amountRight - maxAmountRight else 0.0f

        amountLeft += additionalAmountLeft
        amountRight += additionalAmountRight

        amountLeft = min(amountLeft, maxAmountLeft)
        amountRight = min(amountRight, maxAmountRight)

        val minX = this.minX
        val maxX = this.maxX

        this.minX = minX - amountLeft
        this.maxX = maxX + amountRight

        return amount - (amountLeft + amountRight) / scaleFactor
    }

    private fun scaleHeight(amount: Float): Float {
        val scaleFactor = height / width
        var adjustedAmount = amount * scaleFactor

        if (amount < 0.0f) {
            val minHeight = (WorldConstants.WORLD_CELL_SIZE * WorldConstants.WORLD_CELL_SIZE) / width
            adjustedAmount = max(adjustedAmount, height - minHeight)
            maxY -= adjustedAmount
            return 0.0f
        }


        val checkX = minX + WorldConstants.WORLD_CELL_SIZE * 0.5f
        val checkWidth = width - WorldConstants.WORLD_CELL_SIZE

        var heightAmount = adjustedAmount

        val collisions = Game.physics.getAllOverlappingRectangle(checkX, maxY + heightAmount, checkWidth, heightAmount, { it != body })

        var maxHeightAmount = heightAmount

        collisions.forEach {
            (it.userData as? WorldBlock)?.let {
                maxHeightAmount = min(maxHeightAmount, it.minY - maxY)
            }
        }

        heightAmount = min(heightAmount, maxHeightAmount)

        maxY += heightAmount

        return amount - heightAmount / scaleFactor
    }

    fun deformY(amount: Float): Boolean {
        val availableDeformY = height - WorldConstants.WORLD_CELL_SIZE
        val adjustedAmount = min(abs(amount), availableDeformY)

        val deformFactor = width / height

        var amountLeft = adjustedAmount * deformFactor
        var amountRight = adjustedAmount * deformFactor

        val checkY = minY + WorldConstants.WORLD_CELL_SIZE * 0.5f
        val checkHeight = height - WorldConstants.WORLD_CELL_SIZE

        val collisionsLeft = Game.physics.getAllOverlappingRectangle(minX - amountLeft, checkY, amountLeft, checkHeight, { it != body })
        val collisionsRight = Game.physics.getAllOverlappingRectangle(maxX, checkY, amountRight, checkHeight, { it != body })

        collisionsLeft.forEach {
            (it.userData as? WorldBlock)?.let {
                amountLeft = min(amountLeft, minX - it.maxX)
            }
        }

        collisionsRight.forEach {
            (it.userData as? WorldBlock)?.let {
                amountRight = min(amountRight, it.minX - maxX)
            }
        }

        val newMinX = minX - amountLeft
        val newMaxX = maxX + amountRight
        val area = (maxX - minX) * (maxY - minY)
        val newWidth = newMaxX - newMinX
        val newHeight = area / newWidth
        maxY = minY + newHeight
        minX = newMinX
        maxX = newMaxX

        return amountLeft == abs(amount) * deformFactor || amountRight == abs(amount) * deformFactor
    }

    fun deformX(amount: Float): Boolean {
        val availableDeformX = width - WorldConstants.WORLD_CELL_SIZE

        val adjustedAmount = min(abs(amount), availableDeformX)

        val heightDeformFactor = 1.05f * height / width
        var heightAmount = adjustedAmount * heightDeformFactor

        val checkX = minX + WorldConstants.WORLD_CELL_SIZE * 0.5f
        val checkWidth = width - WorldConstants.WORLD_CELL_SIZE

        val collisions = Game.physics.getAllOverlappingRectangle(checkX, maxY + heightAmount, checkWidth, heightAmount, { it != body })

        collisions.forEach {
            (it.userData as? WorldBlock)?.let {
                heightAmount = min(heightAmount, it.minY - maxY)
            }
        }

        val newMaxY = maxY + heightAmount
        val area = width * height
        val newHeight = newMaxY - minY
        val newWidth = area / newHeight

        if (amount < 0.0f)
            maxX = minX + newWidth
        else
            minX = maxX - newWidth

        maxY = newMaxY

        return heightAmount == abs(amount) * heightDeformFactor
    }
}

class GoalBlock : EntityBlock() {
    override fun onAdded() {
        super.onAdded()
        body.setStatic()
    }

    override fun update(delta: Float) {
        super.update(delta)

        val playerBlock = worldScene.findGameObjectByComponent<PlayerBlock> { true }
        isFacingRight = (playerBlock?.getComponent<PlayerBlock>()?.minX ?: Float.MAX_VALUE) >= minX
    }
}
