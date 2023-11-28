package com.cozmicgames.game.graphics.gui.skin

import com.badlogic.gdx.files.FileHandle
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.Reflection
import kotlin.reflect.KClass

class GUISkin {
    private val styleMaps = hashMapOf<KClass<*>, MutableMap<String, GUIElementStyle>>()

    //TODO: Add base style, so variants are easier to create.
    fun <T : GUIElementStyle> addStyle(type: KClass<T>, name: String = "default"): T {
        val styleMap = styleMaps.getOrPut(type) { hashMapOf() }
        return styleMap.getOrPut(name) { Reflection.createInstance(type)!! } as T
    }

    fun <T : GUIElementStyle> getStyle(type: KClass<T>, name: String = "default"): T? {
        val styleMap = styleMaps[type] ?: return null
        return styleMap[name] as? T
    }

    fun removeStyle(type: KClass<*>, name: String) {
        styleMaps[type]?.remove(name)
    }

    fun removeStyles(type: KClass<*>) {
        styleMaps.remove(type)
    }

    fun clear() {
        styleMaps.clear()
    }

    fun read(file: FileHandle) {
        val properties = Properties()
        properties.read(file.readString())
        read(properties)
    }

    fun read(properties: Properties) {
        styleMaps.clear()
        properties.getPropertiesArray("styles")?.let {
            for (styleProperties in it) {
                val elementStyleTypeName = styleProperties.getString("styleTypeName") ?: continue
                val elementStyleType = Reflection.getClassByName(elementStyleTypeName) as? KClass<GUIElementStyle> ?: continue
                val styleName = styleProperties.getString("styleName") ?: continue
                addStyle(elementStyleType, styleName).read(styleProperties)
            }
        }
    }

    fun write(file: FileHandle) {
        val properties = Properties()
        write(properties)
        file.writeString(properties.write(), false)
    }

    fun write(properties: Properties) {
        val stylesProperties = arrayListOf<Properties>()
        styleMaps.forEach { (styleType, styles) ->
            styles.forEach { (name, style) ->
                val styleProperties = Properties()
                styleProperties.setString("styleTypeName", Reflection.getClassName(styleType))
                styleProperties.setString("styleName", name)
                style.write(styleProperties)
                stylesProperties += styleProperties
            }
        }
        properties.setPropertiesArray("styles", stylesProperties.toTypedArray())
    }
}