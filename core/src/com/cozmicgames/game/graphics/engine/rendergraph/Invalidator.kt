package com.cozmicgames.game.graphics.engine.rendergraph

class Invalidator {
    internal var node: RenderGraph.Node.OnInvalid? = null

    fun invalidate() {
        node?.setDirty()
    }
}