package com.cozmicgames.game.graphics.engine.graphics2d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.GL_SCISSOR_TEST
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.Reflection
import com.cozmicgames.common.utils.collections.ReclaimingPool
import com.cozmicgames.game.fonts
import com.cozmicgames.game.input
import java.util.Random
import kotlin.reflect.KClass

class Graphics2D : Disposable {
    companion object {
        private val VIEWPORT_POINTS_NDC = arrayOf(
                Vector3(-1.0f, -1.0f, 0.0f),
                Vector3(1.0f, -1.0f, 0.0f),
                Vector3(1.0f, 1.0f, 0.0f),
                Vector3(-1.0f, 1.0f, 0.0f)
        )
    }

    private inner class RenderList(val layer: Int) : Iterable<Renderable2D> {
        private val renderables = arrayListOf<Renderable2D>()

        override fun iterator() = renderables.iterator()

        fun sort() {
            renderables.sortWith(compareBy { it.textureHandle })
        }

        fun add(renderable: Renderable2D) {
            renderables += renderable
        }

        fun clear() {
            renderables.forEach {
                if (it.scissorRectangle != null)
                    Pools.free(it.scissorRectangle)
            }
            renderables.clear()
        }
    }

    var sortByTexture = true
    var drawDebugBounds = false
    var showFPS = true

    val currentScissorRectangle get() = scissorStack.currentScissorRectangle

    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val renderLists = IntMap<RenderList>()
    private val sortedRenderLists = arrayListOf<RenderList>()
    private val renderablePools = hashMapOf<KClass<*>, ReclaimingPool<*>>()
    private val cullingRectangle = Rectangle()
    private val viewportPoints = Array(VIEWPORT_POINTS_NDC.size) { Vector3() }
    private val random = Random(0)
    private val scissorStack = ScissorStack()

    private fun getDebugColor() = Color().fromHsv(random.nextFloat() * 360.0f, 1.0f, 1.0f)

    inline fun <reified T : Renderable2D> submit(noinline block: (T) -> Unit) = submit(T::class, block)

    fun <T : Renderable2D> submit(type: KClass<T>, block: (T) -> Unit) {
        val renderablePool = renderablePools.getOrPut(type) { ReclaimingPool(supplier = Reflection.getSupplier(type)!!) }
        val renderable = renderablePool.obtain() as T
        block(renderable)
        renderable.updateBounds()
        scissorStack.currentScissorRectangle?.let {
            renderable.scissorRectangle = Pools.get(Rectangle::class.java).obtain().set(it)
        }
        if (!renderLists.containsKey(renderable.layer)) {
            val list = RenderList(renderable.layer)
            renderLists.put(list.layer, list)
            list.add(renderable)
        } else
            renderLists[renderable.layer].add(renderable)
    }

    fun pushScissor(rectangle: Rectangle) = scissorStack.push(rectangle)

    fun popScissor() = scissorStack.pop()

    fun beginFrame() {
        HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        ScreenUtils.clear(Color.CLEAR)

        random.setSeed(0)
    }

    fun render(camera: OrthographicCamera, overrides: RenderOverrides? = null, renderableFilter: (Renderable2D) -> Boolean = { true }, layerFilter: (Int) -> Boolean = { true }) {
        sortedRenderLists.clear()
        renderLists.values().forEach {
            if (layerFilter(it.layer))
                sortedRenderLists += it
        }
        sortedRenderLists.sortBy { it.layer }

        repeat(viewportPoints.size) {
            viewportPoints[it].set(VIEWPORT_POINTS_NDC[it])
            viewportPoints[it].mul(camera.invProjectionView)
        }

        val cullingMinX = viewportPoints.minBy { it.x }.x
        val cullingMinY = viewportPoints.minBy { it.y }.y
        val cullingMaxX = viewportPoints.maxBy { it.x }.x
        val cullingMaxY = viewportPoints.maxBy { it.y }.y

        cullingRectangle.x = cullingMinX
        cullingRectangle.y = cullingMinY
        cullingRectangle.width = cullingMaxX - cullingMinX
        cullingRectangle.height = cullingMaxY - cullingMinY

        fun applyScissor(scissorRectangle: Rectangle?, block: () -> Unit) {
            if (scissorRectangle != null) {
                batch.flush()
                Gdx.gl.glEnable(GL_SCISSOR_TEST)

                val scissorX = scissorRectangle.x.toInt()
                val scissorY = Gdx.graphics.height - scissorRectangle.y.toInt() - scissorRectangle.height.toInt()
                val scissorWidth = scissorRectangle.width.toInt()
                val scissorHeight = scissorRectangle.height.toInt()

                HdpiUtils.glScissor(scissorX, scissorY, scissorWidth, scissorHeight)
            }

            block()

            if (scissorRectangle != null) {
                batch.flush()
                Gdx.gl.glDisable(GL_SCISSOR_TEST)
            }
        }

        batch.shader = null
        batch.color = Color.WHITE
        batch.projectionMatrix = camera.projection
        batch.transformMatrix = camera.view
        batch.begin()
        sortedRenderLists.forEach {
            if (sortByTexture)
                it.sort()

            it.forEach {
                if (renderableFilter(it) && (it.bounds.overlaps(cullingRectangle) || cullingRectangle.contains(it.bounds))) {
                    applyScissor(it.scissorRectangle) {
                        it.draw(batch, overrides)
                    }
                }
            }
        }
        batch.end()

        if (drawDebugBounds) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.projectionMatrix = camera.projection
            shapeRenderer.transformMatrix = camera.view
            sortedRenderLists.forEach {
                if (sortByTexture)
                    it.sort()

                it.forEach {
                    if (it.bounds.overlaps(cullingRectangle) || cullingRectangle.contains(it.bounds)) {
                        applyScissor(it.scissorRectangle) {
                            shapeRenderer.color = getDebugColor()
                            shapeRenderer.rect(it.bounds.x, it.bounds.y, it.bounds.width, it.bounds.height)
                        }
                    }
                }
            }
            shapeRenderer.end()
        }
    }

    fun endFrame() {
        if (Game.input.isKeyJustDown(Keys.F1))
            showFPS = !showFPS

        renderLists.values().forEach {
            it.clear()
        }

        renderablePools.forEach { (_, pool) ->
            pool.freePooled()
        }

        if (showFPS) {
            batch.begin()
            batch.projectionMatrix.setToOrtho2D(0.0f, Gdx.graphics.height.toFloat(), Gdx.graphics.width.toFloat(), -Gdx.graphics.height.toFloat())
            batch.transformMatrix.idt()
            batch.color = Color.WHITE
            val font = Game.fonts.getFont("default")
            val layout = GlyphLayout()
            font.updateLayout(layout, "FPS: ${Gdx.graphics.framesPerSecond}", 15.0f, Color.LIME)
            font.draw(batch, 3.0f, 3.0f, layout, 15.0f)
            font.updateLayout(layout, "MEM: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024} MB / ${(Runtime.getRuntime().totalMemory()) / 1024 / 1024} MB", 15.0f, Color.LIME)
            font.draw(batch, 3.0f, 20.0f, layout, 15.0f)
            batch.end()
        }
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }
}