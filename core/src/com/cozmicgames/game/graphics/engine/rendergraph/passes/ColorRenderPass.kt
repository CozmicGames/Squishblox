package com.cozmicgames.game.graphics.engine.rendergraph.passes

import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.standardResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat


class ColorRenderPass(resolution: Resolution = standardResolution(), colorFormat: TextureFormat = TextureFormat.RGBA8_UNORM) : RenderPass(resolution) {
    val color = addColorRenderTarget(colorFormat)
}