package com.cozmicgames.game.scene

import com.cozmicgames.common.utils.Properties

abstract class Component {
    lateinit var gameObject: GameObject
        internal set

    open fun onAdded() {}
    open fun onParentChanged() {}
    open fun onActiveChanged() {}
    open fun onRemoved() {}

    open fun read(properties: Properties) {}
    open fun write(properties: Properties) {}
}
