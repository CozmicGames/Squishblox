package com.cozmicgames.game.world

import com.cozmicgames.game.scene.Component
import com.cozmicgames.game.scene.components.TransformComponent

class BlockPreviewComponent : Component() {
    private val transformComponent by lazy { gameObject.getOrAddComponent<TransformComponent>() }

    lateinit var world: World

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

    val isBuildable: Boolean
        get() {
            var result = true
            world.forEachBlock(WorldUtils.toCellCoord(minX), WorldUtils.toCellCoord(minY), WorldUtils.toCellCoord(maxX), WorldUtils.toCellCoord(maxY)) {
                result = false
            }
            return result
        }
}
