package com.cozmicgames.game.utils

import com.badlogic.gdx.utils.Disposable
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Context : Disposable {
    abstract class BaseInjector<T : Any>(protected val context: com.cozmicgames.game.utils.Context, protected val type: KClass<T>) {
        protected open var cachedValue = context.inject(type)

        internal fun updateValue() {
            cachedValue = context.inject(type)
        }
    }

    class NullableInjector<T : Any>(context: com.cozmicgames.game.utils.Context, cls: KClass<T>) : com.cozmicgames.game.utils.Context.BaseInjector<T>(context, cls) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): T? {
            return cachedValue
        }
    }

    open class Injector<T : Any>(context: com.cozmicgames.game.utils.Context, cls: KClass<T>) : com.cozmicgames.game.utils.Context.BaseInjector<T>(context, cls) {
        open operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            return requireNotNull(cachedValue)
        }
    }

    class LazyInjector<T : Any>(context: com.cozmicgames.game.utils.Context, cls: KClass<T>, private val default: () -> T) : com.cozmicgames.game.utils.Context.Injector<T>(context, cls) {
        override var cachedValue: T? = null

        override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            if (cachedValue == null) {
                cachedValue = context.inject(type)
                if (cachedValue == null) {
                    val value = default()
                    context.bind(type, value)
                    cachedValue = value
                }
            }

            return requireNotNull(cachedValue)
        }
    }

    private val values = ConcurrentHashMap<KClass<*>, Any>()
    private val injectors = ConcurrentHashMap<KClass<*>, ArrayList<BaseInjector<*>>>()

    inline fun <reified T : Any> injector() = injector(T::class)

    fun <T : Any> injector(type: KClass<T>): Injector<T> {
        val injector = Injector(this, type)
        injectors.getOrPut(type) { arrayListOf() } += injector
        return injector
    }

    inline fun <reified T : Any> nullableInjector() = nullableInjector(T::class)

    fun <T : Any> nullableInjector(type: KClass<T>): NullableInjector<T> {
        val injector = NullableInjector(this, type)
        injectors.getOrPut(type) { arrayListOf() } += injector
        return injector
    }

    inline fun <reified T : Any> lazyInjector(noinline default: () -> T) = lazyInjector(T::class, default)

    fun <T : Any> lazyInjector(type: KClass<T>, default: () -> T): LazyInjector<T> {
        val injector = LazyInjector(this, type, default)
        injectors.getOrPut(type) { arrayListOf() } += injector
        return injector
    }

    inline fun <reified T : Any> bind(value: T, allowReplace: Boolean = true) = bind(T::class, value, allowReplace)

    fun <T : Any> bind(type: KClass<T>, value: T, allowReplace: Boolean = true) {
        if (!allowReplace && hasProvider(type))
            throw RuntimeException("Context already has a value of type $type")

        values.put(type, value)?.let {
            if (it is Disposable)
                it.dispose()
        }

        injectors[type]?.forEach {
            it.updateValue()
        }
    }

    inline fun <reified T : Any> remove(dispose: Boolean = true) = remove(T::class, dispose)

    fun <T : Any> remove(type: KClass<T>, dispose: Boolean = true) = values.remove(type)?.let {
        if (dispose && it is Disposable)
            it.dispose()

        injectors[type]?.forEach {
            it.updateValue()
        }
    }

    inline fun <reified T : Any> hasProvider(): Boolean = hasProvider(T::class)

    fun <T : Any> hasProvider(type: KClass<T>): Boolean = values.containsKey(type)

    inline fun <reified T : Any> inject() = inject(T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> inject(type: KClass<T>): T? {
        var obj: T? = null
        values[type]?.let {
            obj = it as? T
        }
        return obj
    }

    fun forEach(block: (Any) -> Unit) {
        values.forEach { (_, value) ->
            block(value)
        }
    }

    fun clear(dispose: Boolean = true) {
        for ((_, provider) in values)
            if (dispose && provider is Disposable)
                provider.dispose()

        values.clear()
        injectors.clear()
    }

    override fun dispose() = clear(true)
}

inline fun <reified T : Any> Context.injector(lazy: Boolean = false, noinline default: () -> T): Context.Injector<T> {
    return if (lazy)
        lazyInjector(default)
    else {
        if (!hasProvider<T>())
            bind(default())
        injector()
    }
}
