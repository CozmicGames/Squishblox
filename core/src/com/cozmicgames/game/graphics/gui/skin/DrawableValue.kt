package com.cozmicgames.game.graphics.gui.skin

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.game.graphics.gui.AnimatedDrawable
import com.cozmicgames.game.graphics.gui.GUIDrawable
import com.cozmicgames.game.graphics.gui.NinepatchDrawable
import com.cozmicgames.game.graphics.gui.TextureDrawable
import com.cozmicgames.game.textures

sealed class DrawableValue : GUIStyleValue {
    override val type get() = GUIStyleValue.Type.DRAWABLE

    protected var isDirty = true

    var paddingLeft = 0.0f
    var paddingRight = 0.0f
    var paddingTop = 0.0f
    var paddingBottom = 0.0f

    val color = Color(Color.WHITE)

    val drawable: GUIDrawable
        get() {
            if (internalDrawable == null || isDirty) {
                internalDrawable = createDrawable()
                isDirty = false
            }
            return internalDrawable!!
        }

    private var internalDrawable: GUIDrawable? = null

    fun setPadding(value: Float) {
        paddingLeft = value
        paddingRight = value
        paddingTop = value
        paddingBottom = value
    }

    protected abstract fun createDrawable(): GUIDrawable

    override fun read(properties: Properties) {
        paddingLeft = properties.getFloat("paddingLeft") ?: 0.0f
        paddingRight = properties.getFloat("paddingRight") ?: 0.0f
        paddingTop = properties.getFloat("paddingTop") ?: 0.0f
        paddingBottom = properties.getFloat("paddingBottom") ?: 0.0f
        properties.getProperties("color")?.let {
            color.r = it.getFloat("r") ?: 1.0f
            color.g = it.getFloat("g") ?: 1.0f
            color.b = it.getFloat("b") ?: 1.0f
            color.a = it.getFloat("a") ?: 1.0f
        } ?: color.set(Color.CLEAR)
    }

    override fun write(properties: Properties) {
        properties.setFloat("paddingLeft", paddingLeft)
        properties.setFloat("paddingRight", paddingRight)
        properties.setFloat("paddingTop", paddingTop)
        properties.setFloat("paddingBottom", paddingBottom)
        properties.setProperties("color", Properties().also {
            it.setFloat("r", color.r)
            it.setFloat("g", color.g)
            it.setFloat("b", color.b)
            it.setFloat("a", color.a)
        })
    }
}

class TextureDrawableValue : DrawableValue() {
    var texture = ""
        set(value) {
            isDirty = true
            field = value
        }

    var flipX = false
        set(value) {
            isDirty = true
            field = value
        }

    var flipY = false
        set(value) {
            isDirty = true
            field = value
        }

    override fun createDrawable(): GUIDrawable {
        return TextureDrawable(texture, flipX, flipY)
    }

    override fun read(properties: Properties) {
        super.read(properties)
        texture = properties.getString("texture") ?: ""
        flipX = properties.getBoolean("flipX") ?: false
        flipY = properties.getBoolean("flipY") ?: false
    }

    override fun write(properties: Properties) {
        super.write(properties)
        properties.setString("texture", texture)
        properties.setBoolean("flipX", flipX)
        properties.setBoolean("flipY", flipY)
    }
}

class ColorDrawableValue : DrawableValue() {
    override fun createDrawable(): GUIDrawable {
        return TextureDrawable("blank", false, false)
    }
}

class NinepatchDrawableValue : DrawableValue() {
    var texture = ""
        set(value) {
            isDirty = true
            field = value
        }

    var leftSplitWidth = 0
        set(value) {
            isDirty = true
            field = value
        }

    var rightSplitWidth = 0
        set(value) {
            isDirty = true
            field = value
        }

    var topSplitHeight = 0
        set(value) {
            isDirty = true
            field = value
        }

    var bottomSplitHeight = 0
        set(value) {
            isDirty = true
            field = value
        }

    fun setLeftRight(value: Int) {
        leftSplitWidth = value
        rightSplitWidth = value
    }

    fun setTopBottom(value: Int) {
        topSplitHeight = value
        bottomSplitHeight = value
    }

    fun setSplitSize(value: Int) {
        setLeftRight(value)
        setTopBottom(value)
    }

    fun autoSetSplitSizes() {
        val image = Game.textures.getImage(texture) ?: return
        setLeftRight(image.width / 3)
        setTopBottom(image.height / 3)
    }

    override fun createDrawable(): GUIDrawable {
        return NinepatchDrawable(texture, leftSplitWidth, rightSplitWidth, topSplitHeight, bottomSplitHeight)
    }

    override fun read(properties: Properties) {
        super.read(properties)
        texture = properties.getString("texture") ?: ""
        leftSplitWidth = properties.getInt("leftWidth") ?: 0
        rightSplitWidth = properties.getInt("rightWidth") ?: 0
        topSplitHeight = properties.getInt("topHeight") ?: 0
        bottomSplitHeight = properties.getInt("bottomHeight") ?: 0
    }

    override fun write(properties: Properties) {
        super.write(properties)
        properties.setString("texture", texture)
        properties.setInt("leftWidth", leftSplitWidth)
        properties.setInt("rightWidth", rightSplitWidth)
        properties.setInt("topHeight", topSplitHeight)
        properties.setInt("bottomHeight", bottomSplitHeight)
    }
}

class AnimatedDrawableValue : DrawableValue() {
    var textures: Array<String> = emptyArray()
        set(value) {
            isDirty = true
            field = value
        }

    var keyFrameDuration = 0.0f
        set(value) {
            isDirty = true
            field = value
        }

    var flipX = false
        set(value) {
            isDirty = true
            field = value
        }

    var flipY = false
        set(value) {
            isDirty = true
            field = value
        }

    override fun createDrawable(): GUIDrawable {
        return AnimatedDrawable(textures, keyFrameDuration, flipX, flipY)
    }

    override fun read(properties: Properties) {
        super.read(properties)
        textures = properties.getStringArray("textures") ?: emptyArray()
        keyFrameDuration = properties.getFloat("keyFrameDuration") ?: 0.0f
        flipX = properties.getBoolean("flipX") ?: false
        flipY = properties.getBoolean("flipY") ?: false
    }

    override fun write(properties: Properties) {
        super.write(properties)
        properties.setStringArray("textures", textures)
        properties.setFloat("keyFrameDuration", keyFrameDuration)
        properties.setBoolean("flipX", flipX)
        properties.setBoolean("flipY", flipY)
    }
}
