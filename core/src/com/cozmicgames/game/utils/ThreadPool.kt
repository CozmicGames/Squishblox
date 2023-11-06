package com.cozmicgames.game.utils

import com.badlogic.gdx.utils.Disposable
import java.util.concurrent.Executors

class ThreadPool : Disposable {
    private val executor = Executors.newCachedThreadPool()

    fun execute(block: () -> Unit) {
        return executor.execute(block)
    }

    override fun dispose() {
        executor.shutdown()
    }
}