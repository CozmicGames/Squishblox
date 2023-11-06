package com.cozmicgames.game.graphics.engine.graphics2d.fonts

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.game.shaders

//TODO: Use linegapscale

sealed interface DrawableFont : Disposable {
    val bitmapFont: BitmapFont

    fun isDisplayable(char: Char) = bitmapFont.data.getGlyph(char) != null

    fun updateLayout(layout: GlyphLayout, text: String, size: Float, color: Color = Color.WHITE, targetWidth: Float = Float.MAX_VALUE, lineGapScale: Float = 0.0f, horizontalAlign: Int = Align.left, wrap: Boolean = false)

    fun draw(batch: SpriteBatch, x: Float, y: Float, layout: GlyphLayout, size: Float, style: FontStyle = DefaultFontStyle)
}

class BitmapDrawableFont(override val bitmapFont: BitmapFont) : DrawableFont {
    constructor(file: FileHandle) : this(BitmapFont(file, true))

    constructor() : this(BitmapFont(true))

    init {
        bitmapFont.data.markupEnabled = true
    }

    override fun updateLayout(layout: GlyphLayout, text: String, size: Float, color: Color, targetWidth: Float, lineGapScale: Float, horizontalAlign: Int, wrap: Boolean) {
        val scale = size / bitmapFont.data.getGlyph('X').let { it.height - it.yoffset }
        bitmapFont.data.setScale(scale)
        layout.setText(bitmapFont, text, color, targetWidth, horizontalAlign, wrap)
        bitmapFont.data.setScale(1.0f / scale)
    }

    override fun draw(batch: SpriteBatch, x: Float, y: Float, layout: GlyphLayout, size: Float, style: FontStyle) {
        val scale = size / bitmapFont.data.getGlyph('X').height
        bitmapFont.data.setScale(scale)
        bitmapFont.draw(batch, layout, x, y)
        bitmapFont.data.setScale(1.0f / scale)
    }

    override fun dispose() {
        bitmapFont.dispose()
    }
}

//Modified from: https://github.com/maltaisn/msdf-gdx/tree/master
class MsdfDrawableFont(fontFile: FileHandle, imageFile: FileHandle) : DrawableFont {
    constructor(file: FileHandle) : this(file, file.sibling("${file.nameWithoutExtension()}.png"))

    companion object {
        private fun getFontRegionFromFile(file: FileHandle): TextureRegion {
            val texture = Texture(file, Pixmap.Format.RGBA8888, true)
            texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear)
            return TextureRegion(texture)
        }

        private fun parseAttribute(line: String, name: String): Int {
            var start = line.indexOf("$name=")
            if (start == -1)
                throw RuntimeException("Required font parameter '$name' not specified in font file.")
            start += name.length + 1
            var end = line.indexOf(' ', start)
            if (end == -1)
                end = line.length
            return line.substring(start, end).toInt()
        }
    }

    override val bitmapFont = BitmapFont(BitmapFontData(fontFile, true), getFontRegionFromFile(imageFile), false)

    var glyphSize = 0.0f
        private set

    var distanceRange = 0.0f
        private set

    init {
        bitmapFont.data.markupEnabled = true

        fontFile.reader(128).use {
            val infoLine = it.readLine()
            val gameLine = it.readLine()

            glyphSize = parseAttribute(infoLine, "size").toFloat()
            distanceRange = parseAttribute(gameLine, "distanceRange").toFloat()
        }
    }

    override fun updateLayout(layout: GlyphLayout, text: String, size: Float, color: Color, targetWidth: Float, lineGapScale: Float, horizontalAlign: Int, wrap: Boolean) {
        val scale = size / glyphSize
        bitmapFont.data.setScale(scale)
        layout.setText(bitmapFont, text, color, targetWidth, horizontalAlign, wrap)
        bitmapFont.data.setScale(1.0f / scale)
    }

    override fun draw(batch: SpriteBatch, x: Float, y: Float, layout: GlyphLayout, size: Float, style: FontStyle) {
        val previousShader = batch.shader
        val shader = Game.shaders.getOrLoadShader(Gdx.files.internal("shaders/msdf.shader2d"))
        batch.shader = shader.shaderProgram

        shader.setUniform("textureSize", bitmapFont.region.regionWidth.toFloat(), bitmapFont.region.regionHeight.toFloat())
        shader.setUniform("distanceFactor", distanceRange * size / glyphSize)
        shader.setUniform("fontWeight", style.fontWeight)
        shader.setUniform("shadowClipped", if (style.isShadowClipped) 1.0f else 0.0f)
        shader.setUniform("shadowColor", style.shadowColor)
        shader.setUniform("shadowOffset", style.shadowOffset)
        shader.setUniform("shadowSmoothing", style.shadowSmoothing)
        shader.setUniform("innerShadowColor", style.innerShadowColor)
        shader.setUniform("innerShadowRange", style.innerShadowRange)

        val scale = size / glyphSize
        bitmapFont.data.setScale(scale)
        bitmapFont.draw(batch, layout, x, y)
        bitmapFont.data.setScale(1.0f / scale)

        batch.shader = previousShader
    }

    override fun dispose() {
        bitmapFont.dispose()
    }
}
