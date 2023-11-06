package com.cozmicgames.game.graphics.engine.rendergraph

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.utils.Resizable

class RenderGraph(presentRenderFunction: RenderFunction?) : Resizable, Disposable {
    sealed class Node(val pass: RenderPass, renderFunction: RenderFunction) : Disposable {
        class OnRender(pass: RenderPass, renderFunction: RenderFunction) : Node(pass, renderFunction) {
            private var renderedFrame = -1L

            override fun shouldRender(delta: Float): Boolean {
                if (Gdx.graphics.frameId != renderedFrame) {
                    renderedFrame = Gdx.graphics.frameId
                    return true
                }

                return false
            }
        }

        class OnInvalid(invalidator: Invalidator?, pass: RenderPass, renderFunction: RenderFunction) : Node(pass, renderFunction) {
            private var isValid = false

            init {
                invalidator?.node = this
            }

            override fun shouldRender(delta: Float): Boolean {
                if (isValid)
                    return false

                isValid = true
                return true
            }

            fun setDirty() {
                isValid = false
            }
        }

        class OnInterval(val frequency: Double, pass: RenderPass, renderFunction: RenderFunction) : Node(pass, renderFunction) {
            constructor(rate: Int, pass: RenderPass, renderFunction: RenderFunction) : this(1.0 / rate, pass, renderFunction)

            private var counter = 0.0

            override fun shouldRender(delta: Float): Boolean {
                counter += delta
                var result = false

                while (counter >= frequency) {
                    counter -= frequency.toFloat()
                    result = true
                }

                return result
            }
        }

        lateinit var graph: RenderGraph

        var renderFunction = renderFunction
            set(value) {
                (field as? Disposable)?.dispose()
                value.graph = graph
                value.pass = pass
                field = value
            }

        abstract fun shouldRender(delta: Float): Boolean

        fun render(delta: Float) {
            if (!shouldRender(delta))
                return

            renderFunction.dependencies.forEach {
                it.getNode().render(delta)
            }

            pass.begin()
            renderFunction.render(delta)
        }

        override fun dispose() {
            pass.dispose()
            (renderFunction as? Disposable)?.dispose()
        }
    }

    private val nodes = hashMapOf<String, Node>()

    var presentRenderFunction: RenderFunction? = presentRenderFunction
        set(value) {
            (field as? Disposable)?.dispose()
            field = value
            field?.graph = this
        }

    init {
        presentRenderFunction?.graph = this
    }

    fun addNode(name: String, node: Node) {
        node.graph = this
        node.renderFunction.graph = this
        node.renderFunction.pass = node.pass
        nodes[name]?.dispose()
        nodes[name] = node
    }

    fun remove(name: String) {
        nodes.remove(name)?.dispose()
    }

    fun getNode(name: String) = nodes[name]

    fun getPass(name: String) = getNode(name)?.pass

    fun render(delta: Float) {
        presentRenderFunction?.dependencies?.forEach {
            it.getNode().render(delta)
        }

        FrameBuffer.unbind()
        presentRenderFunction?.render(delta)
    }

    override fun resize(width: Int, height: Int) {
        for ((_, node) in nodes) {
            node.pass.resize(width, height)
            node.renderFunction.onResize(width, height)
        }
    }

    override fun dispose() {
        for ((_, node) in nodes)
            node.dispose()

        (presentRenderFunction as? Disposable)?.dispose()
    }
}

fun emptyRenderGraph(color: Color = Color.RED) = RenderGraph(object : RenderFunction() {
    override fun render(delta: Float) {
        ScreenUtils.clear(color)
    }
})

fun RenderGraph.onRender(name: String, pass: RenderPass, function: RenderFunction): RenderGraph.Node {
    val node = RenderGraph.Node.OnRender(pass, function)
    addNode(name, node)
    return node
}

fun RenderGraph.onInvalid(name: String, invalidator: Invalidator, pass: RenderPass, function: RenderFunction): RenderGraph.Node {
    val node = RenderGraph.Node.OnInvalid(invalidator, pass, function)
    addNode(name, node)
    return node
}

fun RenderGraph.once(name: String, pass: RenderPass, function: RenderFunction): RenderGraph.Node {
    val node = RenderGraph.Node.OnInvalid(null, pass, function)
    addNode(name, node)
    return node
}

fun RenderGraph.onInterval(name: String, frequency: Double, pass: RenderPass, function: RenderFunction): RenderGraph.Node {
    val node = RenderGraph.Node.OnInterval(frequency, pass, function)
    addNode(name, node)
    return node
}

fun RenderGraph.onInterval(name: String, rate: Int, pass: RenderPass, function: RenderFunction): RenderGraph.Node {
    val node = RenderGraph.Node.OnInterval(rate, pass, function)
    addNode(name, node)
    return node
}
