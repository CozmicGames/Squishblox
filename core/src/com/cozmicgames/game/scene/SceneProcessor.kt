package com.cozmicgames.game.scene

abstract class SceneProcessor {
    var scene: Scene? = null
        internal set

    abstract fun shouldProcess(delta: Float): Boolean

    abstract fun process(delta: Float)
}