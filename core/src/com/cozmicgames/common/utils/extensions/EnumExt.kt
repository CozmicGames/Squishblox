package com.cozmicgames.common.utils.extensions

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
    return try {
        enumValueOf<T>(name)
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T : Enum<T>> enumValueOfOrDefault(name: String, noinline default: () -> T): T {
    return try {
        enumValueOf(name)
    } catch (e: Exception) {
        default()
    }
}