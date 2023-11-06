package com.cozmicgames.game.utils.collections

interface Stack<T> : Iterable<T> {
    val size: Int
    val current: T?
    val isEmpty: Boolean

    fun push(value: T)
    fun pop(): T
    fun peek(): T
    fun forEach(descending: Boolean = false, block: (T) -> Unit)
    fun clear()
}

fun <T> Stack<T>.drain(block: (T) -> Unit) {
    while (!isEmpty) {
        block(pop())
    }
}
