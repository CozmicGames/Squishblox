package com.cozmicgames.game.graphics.gui.skin

import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class GUIElementStyle {
    private val values = hashMapOf<String, GUIStyleValue>()

    val valueNames get() = values.keys.toSet()

    fun <T : GUIStyleValue> add(name: String, type: KClass<T>): T {
        val value = Reflection.createInstance(type)!!
        this[name] = value
        return value
    }

    operator fun <T : GUIStyleValue> set(name: String, value: T?) {
        if (value != null)
            values[name] = value
        else
            values.remove(name)
    }

    operator fun get(name: String) = values[name]

    operator fun contains(name: String) = name in values

    fun remove(name: String) {
        values.remove(name)
    }

    fun clear() {
        values.clear()
    }

    fun getType(name: String) = values[name]?.type

    fun getDrawable(name: String): DrawableValue? {
        return this[name] as? DrawableValue
    }

    fun getFont(name: String): FontValue? {
        return this[name] as? FontValue
    }

    fun getColor(name: String): ColorValue? {
        return this[name] as? ColorValue
    }

    fun getInt(name: String): IntValue? {
        return this[name] as? IntValue
    }

    fun getFloat(name: String): FloatValue? {
        return this[name] as? FloatValue
    }

    fun getBoolean(name: String): BooleanValue? {
        return this[name] as? BooleanValue
    }

    fun getString(name: String): StringValue? {
        return this[name] as? StringValue
    }

    fun read(properties: Properties) {
        values.clear()
        properties.getPropertiesArray("values")?.let {
            for (valueProperties in it) {
                val typeName = valueProperties.getString("typeName") ?: continue
                val type = Reflection.getClassByName(typeName) ?: continue
                val value = Reflection.createInstance(type) as? GUIStyleValue ?: continue
                val name = valueProperties.getString("name") ?: continue
                value.read(properties)
                values[name] = value
            }
        }
    }

    fun write(properties: Properties) {
        val valuesProperties = arrayListOf<Properties>()
        values.forEach { (name, value) ->
            val valueProperties = Properties()
            valueProperties.setString("typeName", Reflection.getClassName(value::class))
            valueProperties.setString("name", name)
            value.write(valueProperties)
            valuesProperties += valueProperties
        }
        properties.setPropertiesArray("values", valuesProperties.toTypedArray())
    }
}

class DrawableStyleValueAccessor(private val createDefault: () -> DrawableValue) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): DrawableValue {
        if (style.getType(property.name) != GUIStyleValue.Type.DRAWABLE)
            style[property.name] = createDefault()
        return style.getDrawable(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: DrawableValue) {
        style[property.name] = value
    }
}

class OptionalDrawableStyleValueAccessor(private val createDefault: (() -> DrawableValue)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): DrawableValue? {
        if (style.getType(property.name) != GUIStyleValue.Type.DRAWABLE)
            if (!isInitialized) {
                createDefault?.let { style[property.name] = it() }
                isInitialized = true
            }
        return style.getDrawable(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: DrawableValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

class FontStyleValueAccessor(private val setDefaults: (FontValue) -> Unit) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): FontValue {
        if (style.getType(property.name) != GUIStyleValue.Type.FONT)
            return style.add(property.name, FontValue::class).also(setDefaults)
        return style.getFont(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: FontValue) {
        style[property.name] = value
    }
}

class OptionalFontStyleValueAccessor(private val setDefaults: ((FontValue) -> Unit)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): FontValue? {
        if (!isInitialized && style.getType(property.name) != GUIStyleValue.Type.FONT) {
            setDefaults?.let { it(style.add(property.name, FontValue::class)) }
            isInitialized = true
        }

        return style.getFont(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: FontValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

class ColorStyleValueAccessor(private val setDefaults: (ColorValue) -> Unit) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): ColorValue {
        if (style.getType(property.name) != GUIStyleValue.Type.COLOR)
            return style.add(property.name, ColorValue::class).also(setDefaults)
        return style.getColor(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: ColorValue) {
        style[property.name] = value
    }
}

class OptionalColorStyleValueAccessor(private val setDefaults: ((ColorValue) -> Unit)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): ColorValue? {
        if (!isInitialized && style.getType(property.name) != GUIStyleValue.Type.COLOR) {
            setDefaults?.let { it(style.add(property.name, ColorValue::class)) }
            isInitialized = true
        }

        return style.getColor(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: ColorValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

class IntStyleValueAccessor(private val setDefaults: (IntValue) -> Unit) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): IntValue {
        if (style.getType(property.name) != GUIStyleValue.Type.INT)
            return style.add(property.name, IntValue::class).also(setDefaults)
        return style.getInt(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: IntValue) {
        style[property.name] = value
    }
}

class OptionalIntStyleValueAccessor(private val setDefaults: ((IntValue) -> Unit)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): IntValue? {
        if (!isInitialized && style.getType(property.name) != GUIStyleValue.Type.INT) {
            setDefaults?.let { it(style.add(property.name, IntValue::class)) }
            isInitialized = true
        }

        return style.getInt(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: IntValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

class FloatStyleValueAccessor(private val setDefaults: (FloatValue) -> Unit) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): FloatValue {
        if (style.getType(property.name) != GUIStyleValue.Type.FLOAT)
            return style.add(property.name, FloatValue::class).also(setDefaults)
        return style.getFloat(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: FloatValue) {
        style[property.name] = value
    }
}

class OptionalFloatStyleValueAccessor(private val setDefaults: ((FloatValue) -> Unit)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): FloatValue? {
        if (!isInitialized && style.getType(property.name) != GUIStyleValue.Type.FLOAT) {
            setDefaults?.let { it(style.add(property.name, FloatValue::class)) }
            isInitialized = true
        }

        return style.getFloat(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: FloatValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

class BooleanStyleValueAccessor(private val setDefaults: (BooleanValue) -> Unit) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): BooleanValue {
        if (style.getType(property.name) != GUIStyleValue.Type.BOOLEAN)
            return style.add(property.name, BooleanValue::class).also(setDefaults)
        return style.getBoolean(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: BooleanValue) {
        style[property.name] = value
    }
}

class OptionalBooleanStyleValueAccessor(private val setDefaults: ((BooleanValue) -> Unit)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): BooleanValue? {
        if (!isInitialized && style.getType(property.name) != GUIStyleValue.Type.BOOLEAN) {
            setDefaults?.let { it(style.add(property.name, BooleanValue::class)) }
            isInitialized = true
        }

        return style.getBoolean(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: BooleanValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

class StringStyleValueAccessor(private val setDefaults: (StringValue) -> Unit) {
    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): StringValue {
        if (style.getType(property.name) != GUIStyleValue.Type.STRING)
            return style.add(property.name, StringValue::class).also(setDefaults)
        return style.getString(property.name)!!
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: StringValue) {
        style[property.name] = value
    }
}

class OptionalStringStyleValueAccessor(private val setDefaults: ((StringValue) -> Unit)? = null) {
    private var isInitialized = false

    operator fun getValue(style: GUIElementStyle, property: KProperty<*>): StringValue? {
        if (!isInitialized && style.getType(property.name) != GUIStyleValue.Type.STRING) {
            setDefaults?.let { it(style.add(property.name, StringValue::class)) }
            isInitialized = true
        }

        return style.getString(property.name)
    }

    operator fun setValue(style: GUIElementStyle, property: KProperty<*>, value: StringValue?) {
        style[property.name] = value
        isInitialized = true
    }
}

fun <T : GUIElementStyle> T.drawable(createDefault: () -> DrawableValue) = DrawableStyleValueAccessor(createDefault)
fun <T : GUIElementStyle> T.font(setDefaults: (FontValue) -> Unit) = FontStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.color(setDefaults: (ColorValue) -> Unit) = ColorStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.int(setDefaults: (IntValue) -> Unit) = IntStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.float(setDefaults: (FloatValue) -> Unit) = FloatStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.boolean(setDefaults: (BooleanValue) -> Unit) = BooleanStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.string(setDefaults: (StringValue) -> Unit) = StringStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.optionalDrawable(createDefault: (() -> DrawableValue)? = null) = OptionalDrawableStyleValueAccessor(createDefault)
fun <T : GUIElementStyle> T.optionalFont(setDefaults: ((FontValue) -> Unit)? = null) = OptionalFontStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.optionalColor(setDefaults: ((ColorValue) -> Unit)? = null) = OptionalColorStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.optionalInt(setDefaults: ((IntValue) -> Unit)? = null) = OptionalIntStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.optionalFloat(setDefaults: ((FloatValue) -> Unit)? = null) = OptionalFloatStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.optionalBoolean(setDefaults: ((BooleanValue) -> Unit)? = null) = OptionalBooleanStyleValueAccessor(setDefaults)
fun <T : GUIElementStyle> T.optionalString(setDefaults: ((StringValue) -> Unit)? = null) = OptionalStringStyleValueAccessor(setDefaults)
