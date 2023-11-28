package com.cozmicgames.game.scene.components

import com.cozmicgames.game.scene.Component
import com.cozmicgames.common.utils.maths.Transform
import com.cozmicgames.common.utils.Properties

class TransformComponent : Component() {
    val transform = Transform()

    override fun onAdded() = updateParent()

    override fun onParentChanged() = updateParent()

    private fun updateParent() {
        val parentTransform = gameObject.parent?.getComponent<TransformComponent>()?.transform
        transform.parent = parentTransform
    }

    override fun read(properties: Properties) {
        transform.read(properties)
    }

    override fun write(properties: Properties) {
        transform.write(properties)
    }
}
