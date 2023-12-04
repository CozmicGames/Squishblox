package com.cozmicgames.common.utils

import com.badlogic.gdx.Gdx
import com.cozmicgames.common.utils.extensions.enumValueOfOrNull
import com.cozmicgames.common.utils.extensions.stringOrNull
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

open class Properties {
    private companion object {
        val prettyPrintJson = Json {
            prettyPrint = true
            allowSpecialFloatingPointValues = true
        }

        val normalPrintJson = Json {
            prettyPrint = false
            allowSpecialFloatingPointValues = true
        }
    }

    abstract class Delegate<T>(val defaultValue: () -> T) {
        private var isDefaultSet = false

        abstract fun get(properties: Properties, name: String): T

        abstract fun set(properties: Properties, name: String, value: T)

        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            val properties = (thisRef as Properties)

            if (property.name !in properties && !isDefaultSet) {
                set(properties, property.name, defaultValue())
                isDefaultSet = true
            }

            return get(properties, property.name)
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            set(thisRef as Properties, property.name, value)
        }
    }

    abstract class OptionalDelegate<T>(val defaultValue: (() -> T)?) {
        private var isDefaultSet = false

        abstract fun get(properties: Properties, name: String): T?

        abstract fun set(properties: Properties, name: String, value: T)

        operator fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val properties = (thisRef as Properties)

            if (property.name !in properties && !isDefaultSet) {
                defaultValue?.let {
                    set(properties, property.name, it())
                }
                isDefaultSet = true
            }

            return get(properties, property.name)
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            if (value == null)
                (thisRef as Properties).remove(property.name)
            else
                set(thisRef as Properties, property.name, value)
        }
    }

    enum class Type {
        INT,
        FLOAT,
        BOOLEAN,
        STRING,
        PROPERTIES
    }

    data class Value(val name: String, val type: Type, val value: Any, val isArray: Boolean)

    private val valuesInternal = hashMapOf<String, Value>()

    val values get() = valuesInternal.values.toList()

    private fun setArrayValue(name: String, type: Type, value: Any) {
        valuesInternal[name] = Value(name, type, value, true)
    }

    private fun setSingleValue(name: String, type: Type, value: Any) {
        valuesInternal[name] = Value(name, type, value, false)
    }

    private fun getValue(name: String) = valuesInternal[name]

    operator fun contains(name: String) = getValue(name) != null

    fun remove(name: String): Boolean {
        return valuesInternal.remove(name) != null
    }

    fun getType(name: String) = getValue(name)?.type

    fun isArray(name: String) = getValue(name)?.isArray == true

    fun setInt(name: String, value: Int) = setSingleValue(name, Type.INT, value)

    fun setFloat(name: String, value: Float) = setSingleValue(name, Type.FLOAT, value)

    fun setBoolean(name: String, value: Boolean) = setSingleValue(name, Type.BOOLEAN, value)

    fun setString(name: String, value: String) = setSingleValue(name, Type.STRING, value)

    fun setProperties(name: String, value: Properties) = setSingleValue(name, Type.PROPERTIES, value)

    fun setIntArray(name: String, value: Array<Int>) = setArrayValue(name, Type.INT, value)

    fun setFloatArray(name: String, value: Array<Float>) = setArrayValue(name, Type.FLOAT, value)

    fun setBooleanArray(name: String, value: Array<Boolean>) = setArrayValue(name, Type.BOOLEAN, value)

    fun setStringArray(name: String, value: Array<String>) = setArrayValue(name, Type.STRING, value)

    fun setPropertiesArray(name: String, value: Array<Properties>) = setArrayValue(name, Type.PROPERTIES, value)

    fun getInt(name: String): Int? {
        val value = getValue(name) ?: return null
        return value.value as? Int
    }

    fun getFloat(name: String): Float? {
        val value = getValue(name) ?: return null
        return value.value as? Float
    }

    fun getBoolean(name: String): Boolean? {
        val value = getValue(name) ?: return null
        return value.value as? Boolean
    }

    fun getString(name: String): String? {
        val value = getValue(name) ?: return null
        return value.value as? String
    }

    fun getProperties(name: String): Properties? {
        val value = getValue(name) ?: return null
        return value.value as? Properties
    }

    @Suppress("UNCHECKED_CAST")
    fun getIntArray(name: String): Array<Int>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getFloatArray(name: String): Array<Float>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Float>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBooleanArray(name: String): Array<Boolean>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Boolean>
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringArray(name: String): Array<String>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<String>
    }

    @Suppress("UNCHECKED_CAST")
    fun getPropertiesArray(name: String): Array<Properties>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Properties>
    }

    @Suppress("UNCHECKED_CAST")
    fun write(prettyPrint: Boolean = true): String {
        fun writeProperties(properties: Properties, builder: JsonObjectBuilder) {
            properties.valuesInternal.forEach { (_, value) ->
                builder.putJsonObject(value.name) {
                    put("type", value.type.name)

                    if (value.isArray) {
                        when (value.type) {
                            Type.INT -> {
                                putJsonArray("array") {
                                    (value.value as Array<Int>).forEach {
                                        add(it)
                                    }
                                }
                            }

                            Type.FLOAT -> {
                                putJsonArray("array") {
                                    (value.value as Array<Float>).forEach {
                                        add(it)
                                    }
                                }
                            }

                            Type.BOOLEAN -> {
                                putJsonArray("array") {
                                    (value.value as Array<Boolean>).forEach {
                                        add(it)
                                    }
                                }
                            }

                            Type.STRING -> {
                                putJsonArray("array") {
                                    (value.value as Array<String>).forEach {
                                        add(it)
                                    }
                                }
                            }

                            Type.PROPERTIES -> {
                                putJsonArray("array") {
                                    (value.value as Array<Properties>).forEach {
                                        addJsonObject {
                                            writeProperties(it, this)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        when (value.type) {
                            Type.INT -> put("value", value.value as Int)
                            Type.FLOAT -> put("value", value.value as Float)
                            Type.BOOLEAN -> put("value", value.value as Boolean)
                            Type.STRING -> put("value", value.value as String)
                            Type.PROPERTIES -> putJsonObject("value") {
                                writeProperties(value.value as Properties, this)
                            }
                        }
                    }
                }
            }
        }

        val obj = buildJsonObject {
            writeProperties(this@Properties, this)
        }

        return if (prettyPrint)
            prettyPrintJson.encodeToString(obj)
        else
            normalPrintJson.encodeToString(obj)
    }

    fun read(text: String) {
        fun JsonPrimitive.string() = toString().removeSurrounding("\"")

        valuesInternal.clear()

        val element = Json.parseToJsonElement(text)

        val obj = try {
            element.jsonObject
        } catch (e: Exception) {
            Gdx.app.error("ERROR", "Properties text does not represent a json object.")
            return
        }

        fun readProperties(obj: JsonObject, properties: Properties) {
            for ((name, valueElement) in obj) {
                val valueObj = try {
                    valueElement.jsonObject
                } catch (e: Exception) {
                    Gdx.app.error("ERROR", "Properties value does not represent a json object ($name).")
                    continue
                }

                val typePrimitive = try {
                    requireNotNull(valueObj["type"]).jsonPrimitive
                } catch (e: Exception) {
                    Gdx.app.error("ERROR", "Properties value type is invalid ($name, ${valueObj["type"]}).")
                    continue
                }

                val typeString = typePrimitive.stringOrNull

                if (typeString == null) {
                    Gdx.app.error("ERROR", "Properties value type is invalid ($name, $typePrimitive).")
                    continue
                }

                val type = enumValueOfOrNull<Type>(typePrimitive.string())

                if (type == null) {
                    Gdx.app.error("ERROR", "Properties value type is invalid ($name, $typePrimitive).")
                    continue
                }

                val isArray = "array" in valueObj

                when (type) {
                    Type.INT -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Int>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.intOrNull

                            if (value == null) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.intOrNull

                        if (value == null) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }

                    Type.FLOAT -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Float>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.floatOrNull

                            if (value == null) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.floatOrNull

                        if (value == null) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }

                    Type.BOOLEAN -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Boolean>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.booleanOrNull

                            if (value == null) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.booleanOrNull

                        if (value == null) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }

                    Type.STRING -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<String>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.stringOrNull

                            if (value == null) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.stringOrNull

                        if (value == null) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }

                    Type.PROPERTIES -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Properties>()

                        for (arrayElement in array) {
                            val objValue = try {
                                arrayElement.jsonObject
                            } catch (e: Exception) {
                                Gdx.app.error("ERROR", "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val valueProperties = Properties()
                            readProperties(objValue, valueProperties)

                            list += valueProperties
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val objValue = try {
                            requireNotNull(valueObj["value"]).jsonObject
                        } catch (e: Exception) {
                            Gdx.app.error("ERROR", "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val valueProperties = Properties()
                        readProperties(objValue, valueProperties)

                        properties.setSingleValue(name, type, valueProperties)
                    }
                }
            }
        }

        readProperties(obj, this)
    }

    fun clear() {
        valuesInternal.clear()
    }

    fun set(properties: Properties) {
        valuesInternal.putAll(properties.valuesInternal)
    }

    override fun hashCode(): Int {
        return valuesInternal.values.contentHashCode()
    }
}

open class IntDelegate(defaultValue: () -> Int) : Properties.Delegate<Int>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getInt(name))

    override fun set(properties: Properties, name: String, value: Int) = properties.setInt(name, value)
}

open class FloatDelegate(defaultValue: () -> Float) : Properties.Delegate<Float>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getFloat(name))

    override fun set(properties: Properties, name: String, value: Float) = properties.setFloat(name, value)
}

open class BooleanDelegate(defaultValue: () -> Boolean) : Properties.Delegate<Boolean>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getBoolean(name))

    override fun set(properties: Properties, name: String, value: Boolean) = properties.setBoolean(name, value)
}

open class StringDelegate(defaultValue: () -> String) : Properties.Delegate<String>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getString(name))

    override fun set(properties: Properties, name: String, value: String) = properties.setString(name, value)
}

open class PropertiesDelegate(defaultValue: () -> Properties) : Properties.Delegate<Properties>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getProperties(name))

    override fun set(properties: Properties, name: String, value: Properties) = properties.setProperties(name, value)
}

open class IntArrayDelegate(defaultValue: () -> Array<Int>) : Properties.Delegate<Array<Int>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getIntArray(name))

    override fun set(properties: Properties, name: String, value: Array<Int>) = properties.setIntArray(name, value)
}

open class FloatArrayDelegate(defaultValue: () -> Array<Float>) : Properties.Delegate<Array<Float>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getFloatArray(name))

    override fun set(properties: Properties, name: String, value: Array<Float>) = properties.setFloatArray(name, value)
}

open class BooleanArrayDelegate(defaultValue: () -> Array<Boolean>) : Properties.Delegate<Array<Boolean>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getBooleanArray(name))

    override fun set(properties: Properties, name: String, value: Array<Boolean>) = properties.setBooleanArray(name, value)
}

open class StringArrayDelegate(defaultValue: () -> Array<String>) : Properties.Delegate<Array<String>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getStringArray(name))

    override fun set(properties: Properties, name: String, value: Array<String>) = properties.setStringArray(name, value)
}

open class PropertiesArrayDelegate(defaultValue: () -> Array<Properties>) : Properties.Delegate<Array<Properties>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getPropertiesArray(name))

    override fun set(properties: Properties, name: String, value: Array<Properties>) = properties.setPropertiesArray(name, value)
}

open class OptionalIntDelegate(defaultValue: (() -> Int)? = null) : Properties.OptionalDelegate<Int>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getInt(name)

    override fun set(properties: Properties, name: String, value: Int) = properties.setInt(name, value)
}

open class OptionalFloatDelegate(defaultValue: (() -> Float)? = null) : Properties.OptionalDelegate<Float>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getFloat(name)

    override fun set(properties: Properties, name: String, value: Float) = properties.setFloat(name, value)
}

open class OptionalBooleanDelegate(defaultValue: (() -> Boolean)? = null) : Properties.OptionalDelegate<Boolean>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getBoolean(name)

    override fun set(properties: Properties, name: String, value: Boolean) = properties.setBoolean(name, value)
}

open class OptionalStringDelegate(defaultValue: (() -> String)? = null) : Properties.OptionalDelegate<String>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getString(name)

    override fun set(properties: Properties, name: String, value: String) = properties.setString(name, value)
}

open class OptionalPropertiesDelegate(defaultValue: (() -> Properties)? = null) : Properties.OptionalDelegate<Properties>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getProperties(name)

    override fun set(properties: Properties, name: String, value: Properties) = properties.setProperties(name, value)
}

open class OptionalIntArrayDelegate(defaultValue: (() -> Array<Int>)? = null) : Properties.OptionalDelegate<Array<Int>>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getIntArray(name)

    override fun set(properties: Properties, name: String, value: Array<Int>) = properties.setIntArray(name, value)
}

open class OptionalFloatArrayDelegate(defaultValue: (() -> Array<Float>)? = null) : Properties.OptionalDelegate<Array<Float>>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getFloatArray(name)

    override fun set(properties: Properties, name: String, value: Array<Float>) = properties.setFloatArray(name, value)
}

open class OptionalBooleanArrayDelegate(defaultValue: (() -> Array<Boolean>)? = null) : Properties.OptionalDelegate<Array<Boolean>>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getBooleanArray(name)

    override fun set(properties: Properties, name: String, value: Array<Boolean>) = properties.setBooleanArray(name, value)
}

open class OptionalStringArrayDelegate(defaultValue: (() -> Array<String>)? = null) : Properties.OptionalDelegate<Array<String>>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getStringArray(name)

    override fun set(properties: Properties, name: String, value: Array<String>) = properties.setStringArray(name, value)
}

open class OptionalPropertiesArrayDelegate(defaultValue: (() -> Array<Properties>)? = null) : Properties.OptionalDelegate<Array<Properties>>(defaultValue) {
    override fun get(properties: Properties, name: String) = properties.getPropertiesArray(name)

    override fun set(properties: Properties, name: String, value: Array<Properties>) = properties.setPropertiesArray(name, value)
}

fun int(defaultValue: () -> Int) = IntDelegate(defaultValue)

fun float(defaultValue: () -> Float) = FloatDelegate(defaultValue)

fun boolean(defaultValue: () -> Boolean) = BooleanDelegate(defaultValue)

fun string(defaultValue: () -> String) = StringDelegate(defaultValue)

fun properties(defaultValue: () -> Properties) = PropertiesDelegate(defaultValue)

fun intArray(defaultValue: () -> Array<Int>) = IntArrayDelegate(defaultValue)

fun floatArray(defaultValue: () -> Array<Float>) = FloatArrayDelegate(defaultValue)

fun booleanArray(defaultValue: () -> Array<Boolean>) = BooleanArrayDelegate(defaultValue)

fun stringArray(defaultValue: () -> Array<String>) = StringArrayDelegate(defaultValue)

fun propertiesArray(defaultValue: () -> Array<Properties>) = PropertiesArrayDelegate(defaultValue)

fun optionalInt(defaultValue: (() -> Int)? = null) = OptionalIntDelegate(defaultValue)

fun optionalFloat(defaultValue: (() -> Float)? = null) = OptionalFloatDelegate(defaultValue)

fun optionalBoolean(defaultValue: (() -> Boolean)? = null) = OptionalBooleanDelegate(defaultValue)

fun optionalString(defaultValue: (() -> String)? = null) = OptionalStringDelegate(defaultValue)

fun optionalProperties(defaultValue: (() -> Properties)? = null) = OptionalPropertiesDelegate(defaultValue)

fun optionalIntArray(defaultValue: (() -> Array<Int>)? = null) = OptionalIntArrayDelegate(defaultValue)

fun optionalFloatArray(defaultValue: (() -> Array<Float>)? = null) = OptionalFloatArrayDelegate(defaultValue)

fun optionalBooleanArray(defaultValue: (() -> Array<Boolean>)? = null) = OptionalBooleanArrayDelegate(defaultValue)

fun optionalStringArray(defaultValue: (() -> Array<String>)? = null) = OptionalStringArrayDelegate(defaultValue)

fun optionalPropertiesArray(defaultValue: (() -> Array<Properties>)? = null) = OptionalPropertiesArrayDelegate(defaultValue)


fun Properties.readIntProperty(property: KMutableProperty0<Int>) {
    val value = getInt(property.name) ?: return
    property.set(value)
}

fun Properties.readFloatProperty(property: KMutableProperty0<Float>) {
    val value = getFloat(property.name) ?: return
    property.set(value)
}

fun Properties.readBooleanProperty(property: KMutableProperty0<Boolean>) {
    val value = getBoolean(property.name) ?: return
    property.set(value)
}

fun Properties.readStringProperty(property: KMutableProperty0<String>) {
    val value = getString(property.name) ?: return
    property.set(value)
}
