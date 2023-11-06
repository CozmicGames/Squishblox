package com.cozmicgames.game.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object Reflection {
    fun <T : Any> getClassName(cls: KClass<T>) = cls.java.name

    fun getClassByName(name: String): KClass<*>? {
        return try {
            Class.forName(name).kotlin
        } catch (e: Exception) {
            null
        }
    }

    fun <T : Any> createInstance(cls: KClass<T>): T? {
        return cls.constructors.find { it.parameters.isEmpty() }?.call()
    }

    //TODO: Find a better and more robust solution
    fun <T : Any> createInstance(cls: KClass<T>, vararg args: Any?): T? {
        return cls.primaryConstructor?.call(args) ?: cls.constructors.find { it.parameters.isEmpty() }?.call()
    }

    fun <T : Any> getSupplier(cls: KClass<T>): (() -> T)? {
        val ctor = cls.constructors.find { it.parameters.isEmpty() } ?: return null
        return { ctor.call() }
    }
}

inline fun <reified T : Any> Reflection.getClassName() = getClassName(T::class)

inline fun <reified T : Any> Reflection.createInstance() = createInstance(T::class)

inline fun <reified T : Any> Reflection.getSupplier() = getSupplier(T::class)