package com.cozmicgames.game.graphics.engine.rendergraph.passes

import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.addDepthRenderTarget
import com.cozmicgames.game.graphics.engine.rendergraph.standardResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat


class MultiColorDepthRenderPass(resolution: Resolution = standardResolution(), colorFormats: Array<TextureFormat> = arrayOf(TextureFormat.RGBA8_UNORM), depthFormat: TextureFormat = TextureFormat.DEPTH24) : RenderPass(resolution) {
    val colors = Array(colorFormats.size) {
        addColorRenderTarget(colorFormats[it])
    }
    val depth = addDepthRenderTarget(depthFormat)
}