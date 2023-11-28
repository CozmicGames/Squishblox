package com.cozmicgames.common.utils.collections

class FixedSizeStack<T>(val capacity: Int) : Stack<T> {
    private var offset = 0
    private val values = arrayOfNulls<Any>(capacity)

    override var size = 0
        private set

    override val isEmpty get() = size == 0

    @Suppress("UNCHECKED_CAST")
    override val current get() = if (size == 0) null else requireNotNull(values[(offset + size - 1) % capacity] as? T?)

    val isFull get() = size == capacity

    override fun push(value: T) {
        values[(size + offset) % capacity] = value
        if (isFull)
            offset = (offset + 1) % capacity
        else
            size++
    }

    @Suppress("UNCHECKED_CAST")
    override fun pop(): T {
        size--
        return requireNotNull(values[(offset + size) % capacity] as? T?)
    }

    @Suppress("UNCHECKED_CAST")
    override fun peek(): T {
        values[(offset + size - 1) % capacity] as? T?
        return requireNotNull(values[size - 1] as? T?)
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            var index = 0

            override fun hasNext(): Boolean {
                return index < size
            }

            override fun next(): T {
                return values[(offset + index++) % capacity] as T
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun forEach(descending: Boolean, block: (T) -> Unit) {
        if (descending) {
            var i = size
            while (i >= 0) {
                block(requireNotNull(values[(i + offset) % capacity] as? T?))
                i--
            }
        } else {
            var i = 0
            while (i < size) {
                block(requireNotNull(values[(i + offset) % capacity] as? T?))
                i++
            }
        }
    }

    override fun clear() {
        offset = 0
        size = 0
    }
}
