package com.cozmicgames.common.utils.collections

import com.badlogic.gdx.utils.Disposable

open class ReclaimingPool<T : Any>(val pool: Pool<T>, var onObtain: (T) -> Unit = {}) : Disposable {
    constructor(size: Int = 10, supplier: () -> T, reset: (T) -> Unit = { if (it is Resettable) it.reset() }, onObtain: (T) -> Unit = {}) : this(
        Pool(size, supplier, reset), onObtain)

    private val pooled = arrayListOf<T>()

    fun obtain(): T {
        val obj = pool.obtain()
        pooled += obj
        onObtain(obj)
        return obj
    }

    fun freePooled() {
        pooled.forEach {
            pool.free(it)
        }
        pooled.clear()
    }

    override fun dispose() {
        pool.dispose()
    }
}

inline fun <T : Any, R> ReclaimingPool<T>.use(block: ReclaimingPool<T>.() -> R): R {
    try {
        return block(this)
    } finally {
        freePooled()
    }
}
