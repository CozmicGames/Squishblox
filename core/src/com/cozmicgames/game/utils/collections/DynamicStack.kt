package com.cozmicgames.game.utils.collections


class DynamicStack<T> : Stack<T> {
    private val values = DynamicArray<T>()

    override var size = 0
        private set

    override val isEmpty get() = size == 0

    override val current get() = if (size == 0) null else values[size - 1]

    override fun push(value: T) {
        values[size] = value
        size++
    }

    override fun pop(): T {
        size--
        return requireNotNull(values[size])
    }

    override fun peek(): T {
        return requireNotNull(values[size - 1])
    }

    override fun iterator() = values.iterator()

    override fun forEach(descending: Boolean, block: (T) -> Unit) {
        if (descending) {
            var i = size
            while (i >= 0) {
                values[i]?.let { block(it) }
                i--
            }
        } else {
            var i = 0
            while (i < size) {
                values[i]?.let { block(it) }
                i++
            }
        }
    }

    override fun clear() {
        values.clear()
    }
}
