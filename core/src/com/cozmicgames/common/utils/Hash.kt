package com.cozmicgames.common.utils

fun hashCodeOf(vararg values: Any): Int {
    if (values.isEmpty())
        return 0

    var hash = 1
    values.forEach {
        hash = 31 * hash + it.hashCode()
    }

    return hash
}

fun <T : Any> Iterable<T>.contentHashCode(): Int {
    var hash = 1
    var hasElements = false

    forEach {
        hash = 31 * hash + it.hashCode()
        hasElements = true
    }

    return if (hasElements) hash else 0
}
