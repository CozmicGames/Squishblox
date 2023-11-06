package com.cozmicgames.game.graphics.engine.textures

import com.badlogic.gdx.graphics.GL30.*

enum class TextureFormat {
    R8_UNSIGNED,
    R8_SIGNED,
    R8_UNORM,
    R8_SNORM,
    R16_UNSIGNED,
    R16_SIGNED,
    R16_FLOAT,
    R32_UNSIGNED,
    R32_SIGNED,
    R32_FLOAT,
    RG8_UNSIGNED,
    RG8_SIGNED,
    RG8_UNORM,
    RG8_SNORM,
    RG16_UNSIGNED,
    RG16_SIGNED,
    RG16_FLOAT,
    RG32_UNSIGNED,
    RG32_SIGNED,
    RG32_FLOAT,
    RGBA8_UNSIGNED,
    RGBA8_SIGNED,
    RGBA8_UNORM,
    RGBA8_SNORM,
    RGBA16_UNSIGNED,
    RGBA16_SIGNED,
    RGBA16_FLOAT,
    RGBA32_UNSIGNED,
    RGBA32_SIGNED,
    RGBA32_FLOAT,
    DEPTH16,
    DEPTH24,
    DEPTH24STENCIL8,
    DEPTH32,
    DEPTH32F,
    STENCIL8
}

val TextureFormat.glFormat get() = when (this) {
    TextureFormat.R8_UNSIGNED -> GL_R8UI
    TextureFormat.RG8_UNSIGNED -> GL_RG8UI
    TextureFormat.RGBA8_UNSIGNED -> GL_RGBA8UI
    TextureFormat.R8_SIGNED -> GL_R8I
    TextureFormat.RG8_SIGNED -> GL_RG8I
    TextureFormat.RGBA8_SIGNED -> GL_RGBA8I
    TextureFormat.R16_UNSIGNED -> GL_R16UI
    TextureFormat.RG16_UNSIGNED -> GL_RG16UI
    TextureFormat.RGBA16_UNSIGNED -> GL_RGBA16UI
    TextureFormat.R16_SIGNED -> GL_R16I
    TextureFormat.RG16_SIGNED -> GL_RG16I
    TextureFormat.RGBA16_SIGNED -> GL_RGBA16I
    TextureFormat.R16_FLOAT -> GL_R16F
    TextureFormat.RG16_FLOAT -> GL_RG16F
    TextureFormat.RGBA16_FLOAT -> GL_RGBA16F
    TextureFormat.R32_UNSIGNED -> GL_R32UI
    TextureFormat.RG32_UNSIGNED -> GL_RG32UI
    TextureFormat.RGBA32_UNSIGNED -> GL_RGBA32UI
    TextureFormat.R32_SIGNED -> GL_R32I
    TextureFormat.RG32_SIGNED -> GL_RG32I
    TextureFormat.RGBA32_SIGNED -> GL_RGBA32I
    TextureFormat.R32_FLOAT -> GL_R32F
    TextureFormat.RG32_FLOAT -> GL_RG32F
    TextureFormat.RGBA32_FLOAT -> GL_RGBA32F
    TextureFormat.R8_UNORM -> GL_R8
    TextureFormat.RG8_UNORM -> GL_RG8
    TextureFormat.RGBA8_UNORM -> GL_RGBA8
    TextureFormat.R8_SNORM -> GL_R8_SNORM
    TextureFormat.RG8_SNORM -> GL_RG8_SNORM
    TextureFormat.RGBA8_SNORM -> GL_RGBA8_SNORM
    TextureFormat.DEPTH16 -> GL_DEPTH_COMPONENT16
    TextureFormat.DEPTH24 -> GL_DEPTH_COMPONENT24
    TextureFormat.DEPTH24STENCIL8 -> GL_DEPTH24_STENCIL8
    TextureFormat.DEPTH32 -> GL_DEPTH_COMPONENT
    TextureFormat.DEPTH32F -> GL_DEPTH_COMPONENT32F
    TextureFormat.STENCIL8 -> GL_STENCIL_INDEX8
}