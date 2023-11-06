package com.cozmicgames.game.utils.collections

import com.badlogic.gdx.utils.Disposable

open class StaticPool<T : Any>(size: Int = 10, supplier: (Int) -> T, private val reset: (T) -> Unit = { if (it is Resettable) it.reset() }) : Disposable {
    private var elements = Array<Any?>(size) { supplier(it) }
    private var index = elements.size

    @Suppress("UNCHECKED_CAST")
    fun obtain(): T? {
        return if (elements.isNotEmpty()) {
            val element = elements[--index]
            elements[index] = null
            element as T?
        } else
            null
    }

    fun free(element: T) {
        reset(element)
        elements[index++] = element
    }

    override fun dispose() {
        elements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}