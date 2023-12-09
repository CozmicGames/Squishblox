package com.cozmicgames.game.graphics.engine.rendergraph

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.graphics.engine.textures.TextureFormat
import com.cozmicgames.game.graphics.engine.textures.glFormat

open class RenderPass(resolution: Resolution, block: RenderPass.() -> Unit = {}) : Disposable {
    interface Resolution {
        fun getWidth(totalWidth: Int = Gdx.graphics.width): Int
        fun getHeight(totalHeight: Int = Gdx.graphics.height): Int
    }

    object StandardResolution : Resolution {
        override fun getWidth(totalWidth: Int) = totalWidth
        override fun getHeight(totalHeight: Int) = totalHeight
    }

    class AbsoluteResolution(val width: Int, val height: Int) : Resolution {
        override fun getWidth(totalWidth: Int) = width
        override fun getHeight(totalHeight: Int) = height
    }

    class ScaledResolution(val scaleWidth: Float, val scaleHeight: Float) : Resolution {
        companion object {
            val HALF = ScaledResolution(0.5f)
            val ONE_THIRD = ScaledResolution(1.0f / 3.0f)
            val ONE_FOURTH = ScaledResolution(0.25f)
            val TWO_THIRDS = ScaledResolution(2.0f / 3.0f)
            val ONE_EIGHTH = ScaledResolution(0.125f)
        }

        constructor(scale: Float) : this(scale, scale)

        override fun getWidth(totalWidth: Int) = (totalWidth * scaleWidth).toInt()
        override fun getHeight(totalHeight: Int) = (totalHeight * scaleHeight).toInt()
    }

    sealed class RenderTarget(val pass: RenderPass, val format: TextureFormat) {
        var textureIndex = 0
            internal set
    }

    class ColorRenderTarget(pass: RenderPass, format: TextureFormat) : RenderTarget(pass, format)

    class DepthRenderTarget(pass: RenderPass, format: TextureFormat) : RenderTarget(pass, format)

    class StencilRenderTarget(pass: RenderPass, format: TextureFormat) : RenderTarget(pass, format)

    var resolution = resolution
        set(value) {
            field = value
            resize(width, height)
        }

    val colorRenderTargets: List<ColorRenderTarget> get() = internalColorRenderTargets

    var depthRenderTarget: DepthRenderTarget? = null
        set(value) {
            field = value
            isDirty = true
        }

    var stencilRenderTarget: StencilRenderTarget? = null
        set(value) {
            field = value
            isDirty = true
        }

    val hasColorRenderTargets get() = internalColorRenderTargets.isNotEmpty()
    val hasDepthRenderTarget get() = depthRenderTarget != null
    val hasStencilRenderTarget get() = stencilRenderTarget != null

    private val colorRenderTargetTextureIndices = arrayListOf<Int>()
    private var depthRenderTargetTextureIndex = -1
    private var stencilRenderTargetTextureIndex = -1

    private val internalColorRenderTargets = arrayListOf<ColorRenderTarget>()

    private var framebuffer: FrameBuffer? = null
    private var isDirty = true
    private var resizeWidth = Gdx.graphics.width
    private var resizeHeight = Gdx.graphics.height

    var width = 0
        private set

    var height = 0
        private set

    var name = "???"
        internal set

    init {
        block(this)
    }

    private fun buildFramebuffer(width: Int, height: Int): FrameBuffer {
        val builder = FrameBufferBuilder(width, height)
        var index = 0
        colorRenderTargetTextureIndices.clear()
        colorRenderTargets.forEach {
            builder.addColorTextureAttachment(it.format.glFormat, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE)
            it.textureIndex = index++
            colorRenderTargetTextureIndices += it.textureIndex
        }
        depthRenderTargetTextureIndex = depthRenderTarget?.let {
            builder.addDepthTextureAttachment(it.format.glFormat, GL20.GL_UNSIGNED_BYTE)
            it.textureIndex = index++
            it.textureIndex
        } ?: -1
        stencilRenderTargetTextureIndex = stencilRenderTarget?.let {
            builder.addStencilTextureAttachment(it.format.glFormat, GL20.GL_UNSIGNED_BYTE)
            it.textureIndex = index++
            it.textureIndex
        } ?: -1
        return builder.build()
    }

    fun getColorTexture(index: Int): Texture? = if (hasColorRenderTargets) framebuffer?.textureAttachments?.get(colorRenderTargetTextureIndices[index]) else null

    fun getDepthTexture(): Texture? = if (hasDepthRenderTarget) framebuffer?.textureAttachments?.get(depthRenderTargetTextureIndex) else null

    fun getStencilTexture(): Texture? = if (hasStencilRenderTarget) framebuffer?.textureAttachments?.get(stencilRenderTargetTextureIndex) else null

    fun addColorRenderTarget(format: TextureFormat): RenderTarget {
        val target = ColorRenderTarget(this, format)
        internalColorRenderTargets += target
        isDirty = true
        return target
    }

    fun removeColorRenderTarget(renderTarget: ColorRenderTarget) {
        internalColorRenderTargets -= renderTarget
        isDirty = true
    }

    fun resize(width: Int, height: Int) {
        isDirty = true
        this.resizeWidth = width
        this.resizeHeight = height
    }

    fun begin() {
        if (isDirty) {
            framebuffer?.dispose()
            width = resolution.getWidth(this.resizeWidth)
            height = resolution.getHeight(this.resizeHeight)
            framebuffer = buildFramebuffer(width, height)
            isDirty = false
        }

        GraphicsUtils.beginFramebuffer(framebuffer)
    }

    override fun dispose() {
        framebuffer?.dispose()
        name = "???"
    }
}

val RenderPass.aspectRatio get() = width.toFloat() / height.toFloat()

fun standardResolution() = RenderPass.StandardResolution

fun absoluteResolution(width: Int, height: Int) = RenderPass.AbsoluteResolution(width, height)

fun absoluteResolution(size: Int) = RenderPass.AbsoluteResolution(size, size)

fun scaledResolution(scaleWidth: Float, scaleHeight: Float) = RenderPass.ScaledResolution(scaleWidth, scaleHeight)

fun scaledResolution(scale: Float) = RenderPass.ScaledResolution(scale)

fun RenderPass.depthRenderTarget(format: TextureFormat) = RenderPass.DepthRenderTarget(this, format)

fun RenderPass.stencilRenderTarget(format: TextureFormat) = RenderPass.StencilRenderTarget(this, format)

fun RenderPass.addDepthRenderTarget(format: TextureFormat): RenderPass.RenderTarget {
    val target = depthRenderTarget(format)
    depthRenderTarget = target
    return target
}

fun RenderPass.addStencilRenderTarget(format: TextureFormat): RenderPass.RenderTarget {
    val target = stencilRenderTarget(format)
    stencilRenderTarget = target
    return target
}