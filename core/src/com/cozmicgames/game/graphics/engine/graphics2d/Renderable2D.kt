package com.cozmicgames.game.graphics.engine.graphics2d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.maths.Matrix3x2
import com.cozmicgames.common.utils.collections.Resettable
import com.cozmicgames.game.fonts
import com.cozmicgames.game.graphics.engine.graphics2d.fonts.DefaultFontStyle
import com.cozmicgames.game.graphics.engine.graphics2d.fonts.FontStyle
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.AnimatedSpriteMaterial
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.StaticSpriteMaterial
import com.cozmicgames.game.graphics.engine.shaders.Shader2D
import com.cozmicgames.game.shaders
import com.cozmicgames.game.textures
import com.cozmicgames.common.utils.extensions.infinite

sealed class Renderable2D : Resettable {
    var layer = 0
    val bounds = Rectangle()
    var scissorRectangle: Rectangle? = null

    abstract val textureHandle: Int

    abstract fun updateBounds()
    abstract fun draw(batch: SpriteBatch, overrides: RenderOverrides?)

    override fun reset() {
        layer = 0
        bounds.infinite()
        scissorRectangle = null
    }
}

class StaticSpriteRenderable2D : Renderable2D() {
    private val vertices = FloatArray(4 * 5)
        get() {
            if (isDirty) {
                updateVertices(field)
                isDirty = false
            }

            return field
        }

    private var isDirty = true

    val transform = Matrix3x2()

    var x = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var y = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var width = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var height = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var originX = 0.5f
        set(value) {
            field = value
            isDirty = true
        }

    var originY = 0.5f
        set(value) {
            field = value
            isDirty = true
        }

    var rotation = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    lateinit var material: StaticSpriteMaterial

    var setUniforms: (Shader2D) -> Unit = {}

    override val textureHandle get() = Game.textures.getTexture(material.texture).texture.textureObjectHandle

    private fun updateVertices(vertices: FloatArray) {
        val colorBits = material.color.toFloatBits()
        val region = Game.textures.getTexture(material.texture)

        val worldOriginX = x + originX
        val worldOriginY = y + originY

        val p0x = -originX
        val p0y = -originY
        val p1x = width - originX
        val p1y = -originY
        val p2x = width - originX
        val p2y = height - originY
        val p3x = -originX
        val p3y = height - originY

        val x0: Float
        val y0: Float
        val x1: Float
        val y1: Float
        val x2: Float
        val y2: Float
        val x3: Float
        val y3: Float

        if (rotation != 0.0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)
            x0 = cos * p0x - sin * p0y
            y0 = sin * p0x + cos * p0y
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = x0 + (x2 - x1)
            y3 = y2 - (y1 - y0)
        } else {
            x0 = p0x
            y0 = p0y
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
        }

        var u0 = region.u
        var v0 = region.v
        var u1 = region.u2
        var v1 = region.v
        var u2 = region.u2
        var v2 = region.v2
        var u3 = region.u
        var v3 = region.v2

        if (material.flipX) {
            u0 = region.u2
            u1 = region.u
            u2 = region.u
            u3 = region.u2
        }

        if (material.flipY) {
            v0 = region.v2
            v1 = region.v2
            v2 = region.v
            v3 = region.v
        }

        transform.transform(x0, y0) { xx, yy ->
            vertices[0] = xx + worldOriginX
            vertices[1] = yy + worldOriginY
            vertices[2] = colorBits
            vertices[3] = u0
            vertices[4] = v0
        }

        transform.transform(x1, y1) { xx, yy ->
            vertices[5] = xx + worldOriginX
            vertices[6] = yy + worldOriginY
            vertices[7] = colorBits
            vertices[8] = u1
            vertices[9] = v1
        }

        transform.transform(x2, y2) { xx, yy ->
            vertices[10] = xx + worldOriginX
            vertices[11] = yy + worldOriginY
            vertices[12] = colorBits
            vertices[13] = u2
            vertices[14] = v2
        }

        transform.transform(x3, y3) { xx, yy ->
            vertices[15] = xx + worldOriginX
            vertices[16] = yy + worldOriginY
            vertices[17] = colorBits
            vertices[18] = u3
            vertices[19] = v3
        }
    }

    override fun updateBounds() {
        val minX = minOf(vertices[0], vertices[5], vertices[10], vertices[15])
        val minY = minOf(vertices[1], vertices[6], vertices[11], vertices[16])
        val maxX = maxOf(vertices[0], vertices[5], vertices[10], vertices[15])
        val maxY = maxOf(vertices[1], vertices[6], vertices[11], vertices[16])

        bounds.x = minX
        bounds.y = minY
        bounds.width = maxX - minX
        bounds.height = maxY - minY
    }

    override fun reset() {
        super.reset()
        transform.setIdentity()
        x = 0.0f
        y = 0.0f
        width = 1.0f
        height = 1.0f
        originX = 0.5f
        originY = 0.5f
        rotation = 0.0f
        isDirty = true
        setUniforms = {}
    }

    override fun draw(batch: SpriteBatch, overrides: RenderOverrides?) {
        val texture = Game.textures.getTexture(overrides?.texture ?: material.texture)
        val shaderName = overrides?.shader ?: material.shader
        val previousShader = batch.shader
        val shader = Game.shaders.getShader(shaderName)

        if (previousShader != shader.shaderProgram) {
            batch.shader = shader.shaderProgram
            material.setUniforms(shader)
            setUniforms(shader)
            overrides?.setUniforms?.let { it(shader) }
            batch.draw(texture.texture, vertices, 0, vertices.size)
            batch.shader = previousShader
        } else
            batch.draw(texture.texture, vertices, 0, vertices.size)
    }
}

class AnimatedSpriteRenderable2D : Renderable2D() {
    private val vertices = FloatArray(4 * 5)
        get() {
            if (isDirty) {
                updateVertices(field)
                isDirty = false
            }

            return field
        }

    private var isDirty = true

    val transform = Matrix3x2()

    var x = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var y = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var width = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var height = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var originX = 0.5f
        set(value) {
            field = value
            isDirty = true
        }

    var originY = 0.5f
        set(value) {
            field = value
            isDirty = true
        }

    var rotation = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    lateinit var material: AnimatedSpriteMaterial
    var animationStateTime = 0.0f

    var setUniforms: (Shader2D) -> Unit = {}

    override val textureHandle get() = Game.textures.getTexture(material.animation.keyFrames.firstOrNull() ?: "<missing>").texture.textureObjectHandle

    private fun updateVertices(vertices: FloatArray) {
        val colorBits = material.color.toFloatBits()
        val region = Game.textures.getTexture(material.animation.getKeyFrame(animationStateTime))

        val worldOriginX = x + originX
        val worldOriginY = y + originY

        val p0x = -originX
        val p0y = -originY
        val p1x = width - originX
        val p1y = -originY
        val p2x = width - originX
        val p2y = height - originY
        val p3x = -originX
        val p3y = height - originY

        val x0: Float
        val y0: Float
        val x1: Float
        val y1: Float
        val x2: Float
        val y2: Float
        val x3: Float
        val y3: Float

        if (rotation != 0.0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)
            x0 = cos * p0x - sin * p0y
            y0 = sin * p0x + cos * p0y
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = x0 + (x2 - x1)
            y3 = y2 - (y1 - y0)
        } else {
            x0 = p0x
            y0 = p0y
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
        }

        var u0 = region.u
        var v0 = region.v
        var u1 = region.u2
        var v1 = region.v
        var u2 = region.u2
        var v2 = region.v2
        var u3 = region.u
        var v3 = region.v2

        if (material.flipX) {
            u0 = region.u2
            u1 = region.u
            u2 = region.u
            u3 = region.u2
        }

        if (material.flipY) {
            v0 = region.v2
            v1 = region.v2
            v2 = region.v
            v3 = region.v
        }

        transform.transform(x0, y0) { xx, yy ->
            vertices[0] = xx + worldOriginX
            vertices[1] = yy + worldOriginY
            vertices[2] = colorBits
            vertices[3] = u0
            vertices[4] = v0
        }

        transform.transform(x1, y1) { xx, yy ->
            vertices[5] = xx + worldOriginX
            vertices[6] = yy + worldOriginY
            vertices[7] = colorBits
            vertices[8] = u1
            vertices[9] = v1
        }

        transform.transform(x2, y2) { xx, yy ->
            vertices[10] = xx + worldOriginX
            vertices[11] = yy + worldOriginY
            vertices[12] = colorBits
            vertices[13] = u2
            vertices[14] = v2
        }

        transform.transform(x3, y3) { xx, yy ->
            vertices[15] = xx + worldOriginX
            vertices[16] = yy + worldOriginY
            vertices[17] = colorBits
            vertices[18] = u3
            vertices[19] = v3
        }
    }

    override fun updateBounds() {
        val minX = minOf(vertices[0], vertices[5], vertices[10], vertices[15])
        val minY = minOf(vertices[1], vertices[6], vertices[11], vertices[16])
        val maxX = maxOf(vertices[0], vertices[5], vertices[10], vertices[15])
        val maxY = maxOf(vertices[1], vertices[6], vertices[11], vertices[16])

        bounds.x = minX
        bounds.y = minY
        bounds.width = maxX - minX
        bounds.height = maxY - minY
    }

    override fun reset() {
        super.reset()
        transform.setIdentity()
        x = 0.0f
        y = 0.0f
        width = 1.0f
        height = 1.0f
        originX = 0.5f
        originY = 0.5f
        rotation = 0.0f
        isDirty = true
        setUniforms = {}
    }

    override fun draw(batch: SpriteBatch, overrides: RenderOverrides?) {
        val texture = Game.textures.getTexture(overrides?.texture ?: material.animation.getKeyFrame(animationStateTime))
        val shaderName = overrides?.shader ?: material.shader
        val previousShader = batch.shader
        val shader = Game.shaders.getShader(shaderName)

        if (previousShader != shader.shaderProgram) {
            batch.shader = shader.shaderProgram
            material.setUniforms(shader, animationStateTime)
            setUniforms(shader)
            overrides?.setUniforms?.let { it(shader) }
            batch.draw(texture.texture, vertices, 0, vertices.size)
            batch.shader = previousShader
        } else
            batch.draw(texture.texture, vertices, 0, vertices.size)
    }
}

class TransformedRenderable2D : Renderable2D() {
    private val vertices = FloatArray(4 * 5)
        get() {
            if (isDirty) {
                updateVertices(field)
                isDirty = false
            }

            return field
        }

    private var isDirty = true

    val transform = Matrix3x2()

    var color = Color.WHITE
        set(value) {
            field = value
            isDirty = true
        }

    var texture = "missing"
        set(value) {
            field = value
            isDirty = true
        }

    var shader: String? = null

    var setUniforms: (Shader2D) -> Unit = {}

    var x = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var y = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var width = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var height = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var originX = 0.5f
        set(value) {
            field = value
            isDirty = true
        }

    var originY = 0.5f
        set(value) {
            field = value
            isDirty = true
        }

    var rotation = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var flipX = false
        set(value) {
            field = value
            isDirty = true
        }

    var flipY = false
        set(value) {
            field = value
            isDirty = true
        }

    override val textureHandle get() = Game.textures.getTexture(texture).texture.textureObjectHandle

    private fun updateVertices(vertices: FloatArray) {
        val colorBits = color.toFloatBits()
        val region = Game.textures.getTexture(texture)

        val worldOriginX = x + originX
        val worldOriginY = y + originY

        val p0x = -originX
        val p0y = -originY
        val p1x = width - originX
        val p1y = -originY
        val p2x = width - originX
        val p2y = height - originY
        val p3x = -originX
        val p3y = height - originY

        val x0: Float
        val y0: Float
        val x1: Float
        val y1: Float
        val x2: Float
        val y2: Float
        val x3: Float
        val y3: Float

        if (rotation != 0.0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)
            x0 = cos * p0x - sin * p0y
            y0 = sin * p0x + cos * p0y
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = x0 + (x2 - x1)
            y3 = y2 - (y1 - y0)
        } else {
            x0 = p0x
            y0 = p0y
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
        }

        var u0 = region.u
        var v0 = region.v
        var u1 = region.u2
        var v1 = region.v
        var u2 = region.u2
        var v2 = region.v2
        var u3 = region.u
        var v3 = region.v2

        if (flipX) {
            u0 = region.u2
            u1 = region.u
            u2 = region.u
            u3 = region.u2
        }

        if (flipY) {
            v0 = region.v2
            v1 = region.v2
            v2 = region.v
            v3 = region.v
        }

        transform.transform(x0, y0) { xx, yy ->
            vertices[0] = xx + worldOriginX
            vertices[1] = yy + worldOriginY
            vertices[2] = colorBits
            vertices[3] = u0
            vertices[4] = v0
        }

        transform.transform(x1, y1) { xx, yy ->
            vertices[5] = xx + worldOriginX
            vertices[6] = yy + worldOriginY
            vertices[7] = colorBits
            vertices[8] = u1
            vertices[9] = v1
        }

        transform.transform(x2, y2) { xx, yy ->
            vertices[10] = xx + worldOriginX
            vertices[11] = yy + worldOriginY
            vertices[12] = colorBits
            vertices[13] = u2
            vertices[14] = v2
        }

        transform.transform(x3, y3) { xx, yy ->
            vertices[15] = xx + worldOriginX
            vertices[16] = yy + worldOriginY
            vertices[17] = colorBits
            vertices[18] = u3
            vertices[19] = v3
        }
    }

    override fun updateBounds() {
        val minX = minOf(vertices[0], vertices[5], vertices[10], vertices[15])
        val minY = minOf(vertices[1], vertices[6], vertices[11], vertices[16])
        val maxX = maxOf(vertices[0], vertices[5], vertices[10], vertices[15])
        val maxY = maxOf(vertices[1], vertices[6], vertices[11], vertices[16])

        bounds.x = minX
        bounds.y = minY
        bounds.width = maxX - minX
        bounds.height = maxY - minY
    }

    override fun reset() {
        super.reset()
        transform.setIdentity()
        color = Color.WHITE
        texture = "missing"
        x = 0.0f
        y = 0.0f
        width = 1.0f
        height = 1.0f
        originX = 0.5f
        originY = 0.5f
        rotation = 0.0f
        isDirty = true
        shader = null
        setUniforms = {}
    }

    override fun draw(batch: SpriteBatch, overrides: RenderOverrides?) {
        val texture = Game.textures.getTexture(overrides?.texture ?: texture)
        val shaderName = overrides?.shader ?: shader

        if (shaderName != null) {
            val previousShader = batch.shader
            val shader = Game.shaders.getShader(shaderName)
            batch.shader = shader.shaderProgram
            setUniforms(shader)
            overrides?.setUniforms?.let { it(shader) }
            batch.draw(texture.texture, vertices, 0, vertices.size)
            batch.shader = previousShader
        } else
            batch.draw(texture.texture, vertices, 0, vertices.size)
    }
}

class DirectRenderable2D : Renderable2D() {
    var x = 0.0f
    var y = 0.0f
    var width = 0.0f
    var height = 0.0f
    var rotation = 0.0f
    var originX = 0.0f
    var originY = 0.0f
    var flipX = false
    var flipY = false
    var color = Color.WHITE
    var texture = "missing"
    var shader: String? = null
    var setUniforms: (Shader2D) -> Unit = {}

    override val textureHandle get() = Game.textures.getTexture(texture).texture.textureObjectHandle

    override fun updateBounds() {
        val worldOriginX = x + originX
        val worldOriginY = y + originY

        val p0x = -originX
        val p0y = -originY
        val p1x = -originX
        val p1y = height - originY
        val p2x = width - originX
        val p2y = height - originY
        val p3x = width - originX
        val p3y = -originY

        var x0: Float
        var y0: Float
        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float

        if (rotation != 0.0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)
            x0 = cos * p0x - sin * p0y
            y0 = sin * p0x + cos * p0y
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = x0 + (x2 - x1)
            y3 = y2 - (y1 - y0)
        } else {
            x0 = p0x
            y0 = p0y
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
        }

        x0 += worldOriginX
        y0 += worldOriginY
        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY

        val minX = minOf(x0, x1, x2, x3)
        val minY = minOf(y0, y1, y2, y3)
        val maxX = maxOf(x0, x1, x2, x3)
        val maxY = maxOf(y0, y1, y2, y3)

        bounds.x = minX
        bounds.y = minY
        bounds.width = maxX - minX
        bounds.height = maxY - minY
    }

    override fun reset() {
        super.reset()
        x = 0.0f
        y = 0.0f
        width = 0.0f
        height = 0.0f
        rotation = 0.0f
        originX = 0.0f
        originY = 0.0f
        flipX = false
        flipY = false
        color = Color.WHITE
        texture = "missing"
        shader = null
        setUniforms = {}
    }

    override fun draw(batch: SpriteBatch, overrides: RenderOverrides?) {
        val previousColor = batch.color
        val texture = Game.textures.getTexture(overrides?.texture ?: texture)
        val shaderName = overrides?.shader ?: shader
        batch.color = overrides?.color ?: color

        if (shaderName != null) {
            val previousShader = batch.shader
            val shader = Game.shaders.getShader(shaderName)
            batch.shader = shader.shaderProgram
            setUniforms(shader)
            overrides?.setUniforms?.let { it(shader) }
            batch.draw(texture.texture, x, y, originX, originY, width, height, 1.0f, 1.0f, rotation, texture.regionX, texture.regionY, texture.regionWidth, texture.regionHeight, flipX, flipY)
            batch.shader = previousShader
        } else
            batch.draw(texture.texture, x, y, originX, originY, width, height, 1.0f, 1.0f, rotation, texture.regionX, texture.regionY, texture.regionWidth, texture.regionHeight, flipX, flipY)

        batch.color = previousColor
    }
}

class TextRenderable2D : Renderable2D() {
    private var isDirty = true

    var x = 0.0f
    var y = 0.0f

    var text = ""
        set(value) {
            field = value
            isDirty = true
        }

    var color = Color.WHITE
        set(value) {
            field = value
            isDirty = true
        }

    var size = 15.0f
        set(value) {
            field = value
            isDirty = true
        }

    var targetWidth = Float.MAX_VALUE
        set(value) {
            field = value
            isDirty = true
        }

    var lineGapScale = 0.0f
        set(value) {
            field = value
            isDirty = true
        }

    var horizontalAlign = Align.left
        set(value) {
            field = value
            isDirty = true
        }

    var wrap = true
        set(value) {
            field = value
            isDirty = true
        }

    var font = ""
        set(value) {
            field = value
            isDirty = true
        }

    var style: FontStyle = DefaultFontStyle

    val layout = GlyphLayout()
        get() {
            if (isDirty) {
                val drawableFont = Game.fonts.getFont(font)
                drawableFont.updateLayout(field, text, size, color, targetWidth, lineGapScale, horizontalAlign, wrap)
                isDirty = false
            }

            return field
        }

    override val textureHandle get() = Game.fonts.getFont(font).bitmapFont.region.texture.textureObjectHandle

    override fun updateBounds() {
        bounds.x = x
        bounds.y = y
        bounds.width = layout.width
        bounds.height = layout.height
    }

    override fun reset() {
        super.reset()
        x = 0.0f
        y = 0.0f
        text = ""
        color = Color.WHITE
        size = 15.0f
        targetWidth = Float.MAX_VALUE
        horizontalAlign = Align.left
        wrap = true
        font = ""
        style = DefaultFontStyle
    }

    override fun draw(batch: SpriteBatch, overrides: RenderOverrides?) {
        val drawableFont = Game.fonts.getFont(font)
        drawableFont.draw(batch, x, y, layout, size, style)
    }
}

class NinepatchRenderable2D : Renderable2D() {
    var x = 0.0f
    var y = 0.0f
    var width = 0.0f
    var height = 0.0f
    var rotation = 0.0f
    var originX = 0.0f
    var originY = 0.0f
    var color = Color.WHITE
    var shader: String? = null
    var setUniforms: (Shader2D) -> Unit = {}

    lateinit var ninePatch: NinePatch

    override val textureHandle get() = ninePatch.texture.textureObjectHandle

    override fun updateBounds() {
        val worldOriginX = x + originX
        val worldOriginY = y + originY

        val p0x = -originX
        val p0y = -originY
        val p1x = -originX
        val p1y = height - originY
        val p2x = width - originX
        val p2y = height - originY
        val p3x = width - originX
        val p3y = -originY

        var x0: Float
        var y0: Float
        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float

        if (rotation != 0.0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)
            x0 = cos * p0x - sin * p0y
            y0 = sin * p0x + cos * p0y
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = x0 + (x2 - x1)
            y3 = y2 - (y1 - y0)
        } else {
            x0 = p0x
            y0 = p0y
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
        }

        x0 += worldOriginX
        y0 += worldOriginY
        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY

        val minX = minOf(x0, x1, x2, x3)
        val minY = minOf(y0, y1, y2, y3)
        val maxX = maxOf(x0, x1, x2, x3)
        val maxY = maxOf(y0, y1, y2, y3)

        bounds.x = minX
        bounds.y = minY
        bounds.width = maxX - minX
        bounds.height = maxY - minY
    }

    override fun reset() {
        super.reset()
        x = 0.0f
        y = 0.0f
        width = 0.0f
        height = 0.0f
        rotation = 0.0f
        originX = 0.0f
        originY = 0.0f
        color = Color.WHITE
        shader = null
        setUniforms = {}
    }

    override fun draw(batch: SpriteBatch, overrides: RenderOverrides?) {
        val previousColor = batch.color
        val shaderName = overrides?.shader ?: shader
        batch.color = overrides?.color ?: color

        if (shaderName != null) {
            val previousShader = batch.shader
            val shader = Game.shaders.getShader(shaderName)
            batch.shader = shader.shaderProgram
            setUniforms(shader)
            overrides?.setUniforms?.let { it(shader) }
            ninePatch.draw(batch, x, y, originX, originY, width, height, 1.0f, 1.0f, rotation)
            batch.shader = previousShader
        } else
            ninePatch.draw(batch, x, y, originX, originY, width, height, 1.0f, 1.0f, rotation)

        batch.color = previousColor
    }
}
