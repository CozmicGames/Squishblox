package com.cozmicgames.game.graphics.engine.rendergraph.passes

import com.cozmicgames.game.graphics.engine.rendergraph.RenderPass
import com.cozmicgames.game.graphics.engine.rendergraph.standardResolution
import com.cozmicgames.game.graphics.engine.textures.TextureFormat


class MultiColorRenderPass(resolution: Resolution = standardResolution(), colorFormats: Array<TextureFormat> = arrayOf(TextureFormat.RGBA8_UNORM)) : RenderPass(resolution) {
    constructor(resolution: Resolution = standardResolution(), colorFormat: TextureFormat, attachmentCount: Int) : this(resolution, Array(attachmentCount) { colorFormat })

    init {
        colorFormats.forEach {
            addColorRenderTarget(it)
        }
    }
}