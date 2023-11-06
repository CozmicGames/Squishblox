package com.cozmicgames.game.graphics.engine.textures

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.game.utils.RectPacker
import com.cozmicgames.game.tasks
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TextureAtlas(private val filter: TextureFilter) : Disposable {
    val texture: Texture
        get() {
            if (textureInternal == null)
                textureInternal = Texture(1, 1, Pixmap.Format.RGBA8888)

            return textureInternal!!
        }

    private var textureInternal: Texture? = null
    private val images = hashMapOf<String, Pixmap>()
    private val packed = hashMapOf<String, TextureRegion>()
    private val versions = hashMapOf<String, Int>()
    private var isDirty = true
    private val packingLock = ReentrantLock()

    operator fun get(name: String): TextureRegion? {
        if (packingLock.isLocked)
            return null

        if (isDirty)
            pack()

        return packed[name]
    }

    fun getImage(name: String) = images[name]

    fun getVersion(name: String) = versions[name]

    fun add(name: String, image: Pixmap) = add(name to image)

    fun add(vararg images: Pair<String, Pixmap>) = add(mapOf(*images))

    fun add(images: Map<String, Pixmap>) = packingLock.withLock {
        this.images.putAll(images)
        isDirty = true
    }

    fun remove(vararg names: String) = packingLock.withLock {
        names.forEach {
            images.remove(it)
        }
        isDirty = true
    }

    fun pack() = packingLock.withLock {
        packed.clear()

        val ids = arrayOfNulls<String>(images.size)
        var currentId = 0
        val rects = images.map { (name, image) ->
            val id = currentId++
            ids[id] = name
            RectPacker.Rectangle(id, image.width + 2, image.height + 2)
        }.toTypedArray()

        var size = 128

        while (true) {
            val packer = RectPacker(size, size)
            packer.pack(rects)

            if (rects.any { !it.isPacked }) {
                size *= 2
                continue
            } else
                break
        }

        val image = Pixmap(size, size, Pixmap.Format.RGBA8888)

        for (rect in rects) {
            val x = rect.x + 1
            val y = rect.y + 1

            val name = ids[rect.id] ?: continue
            val subImage = images[name] ?: continue

            image.drawPixmap(subImage, x, y)
        }

        textureInternal?.dispose()
        textureInternal = Texture(image, true)
        textureInternal!!.setFilter(filter, filter)

        for (rect in rects) {
            val x = rect.x + 1
            val y = rect.y + 1

            val name = ids[rect.id] ?: continue
            val subImage = images[name] ?: continue

            val u0 = x.toFloat() / image.width
            val v0 = y.toFloat() / image.height
            val u1 = (x + subImage.width).toFloat() / image.width
            val v1 = (y + subImage.height).toFloat() / image.height

            packed[name] = TextureRegion(texture, u0, v0, u1, v1)
            versions[name] = versions.getOrDefault(name, 0) + 1
        }

        image.dispose()
        isDirty = false
    }

    fun packAsync(callback: () -> Unit) {
        val rects: Array<RectPacker.Rectangle>
        val ids: Array<String?>

        packingLock.withLock {
            ids = arrayOfNulls(images.size)
            var currentId = 0
            rects = images.map { (name, image) ->
                val id = currentId++
                ids[id] = name
                RectPacker.Rectangle(id, image.width + 2, image.height + 2)
            }.toTypedArray()
        }

        var size = 128

        while (true) {
            val packer = RectPacker(size, size)
            packer.pack(rects)

            if (rects.any { !it.isPacked }) {
                size *= 2
                continue
            } else
                break
        }

        val image = Pixmap(size, size, Pixmap.Format.RGBA8888)

        for (rect in rects) {
            val x = rect.x + 1
            val y = rect.y + 1

            val name = ids[rect.id] ?: continue
            val subImage = images[name] ?: continue

            image.drawPixmap(subImage, x, y)
        }

        Game.tasks.submit({
            textureInternal?.dispose()
            textureInternal = Texture(image, true)
            textureInternal!!.setFilter(filter, filter)
            packed.clear()

            for (rect in rects) {
                val x = rect.x + 1
                val y = rect.y + 1

                val name = ids[rect.id] ?: continue
                val subImage = images[name] ?: continue

                val u0 = x.toFloat() / image.width
                val v0 = y.toFloat() / image.height
                val u1 = (x + subImage.width).toFloat() / image.width
                val v1 = (y + subImage.height).toFloat() / image.height

                packed[name] = TextureRegion(texture, u0, v0, u1, v1)
                versions[name] = versions.getOrDefault(name, 0) + 1
            }

            image.dispose()
            callback()
        })
    }

    override fun dispose() {
        texture.dispose()

        images.forEach { (_, pixmap) ->
            pixmap.dispose()
        }
    }
}