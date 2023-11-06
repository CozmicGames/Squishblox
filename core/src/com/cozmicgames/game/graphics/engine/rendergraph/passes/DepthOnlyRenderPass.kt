package com.cozmicgames.game.graphics.engine.rendergraph.passes

import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.addDepthRenderTarget
import com.cozmicgames.game.graphics.engine.rendergraph.standardResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat

class DepthOnlyRenderPass(resolution: Resolution = standardResolution(), depthFormat: TextureFormat = TextureFormat.DEPTH24) : RenderPass(resolution) {
    val depth = addDepthRenderTarget(depthFormat)
}