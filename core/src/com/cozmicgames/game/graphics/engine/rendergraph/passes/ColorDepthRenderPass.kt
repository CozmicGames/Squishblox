package com.cozmicgames.game.graphics.engine.rendergraph.passes

import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.addDepthRenderTarget
import com.cozmicgames.game.graphics.engine.rendergraph.standardResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat


class ColorDepthRenderPass(resolution: Resolution = standardResolution(), colorFormat: TextureFormat = TextureFormat.RGBA8_UNORM, depthFormat: TextureFormat = TextureFormat.DEPTH24) : RenderPass(resolution) {
    val color = addColorRenderTarget(colorFormat)
    val depth = addDepthRenderTarget(depthFormat)
}