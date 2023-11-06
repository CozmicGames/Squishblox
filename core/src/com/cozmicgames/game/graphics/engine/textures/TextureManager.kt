package com.cozmicgames.game.graphics.engine.textures

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.game.tasks
import kotlin.reflect.KProperty

class TextureManager : Disposable {
    inner class Getter(private val file: FileHandle, private val filter: TextureFilter) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrLoadTexture(file, filter)
    }

    private class SingleTexture(val image: Pixmap, filter: TextureFilter) : Disposable {
        val texture = Texture(image)
        val region = TextureRegion(texture)

        init {
            texture.setFilter(filter, filter)
        }

        override fun dispose() {
            texture.dispose()
            image.dispose()
        }
    }

    private val missingTexture: Texture
    private val blankTexture: Texture
    private val missingTextureRegion: TextureRegion
    private val blankTextureRegion: TextureRegion

    private val singleTextures = hashMapOf<String, SingleTexture>()
    private val textureAtlases = hashMapOf<TextureFilter, TextureAtlas>()
    private val textureFilters = hashMapOf<String, TextureFilter>()

    init {
        val missingImage = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        missingImage.drawPixel(0, 0, 0x000000FF)
        missingImage.drawPixel(0, 1, 0xFF00FFFF.toInt())
        missingImage.drawPixel(1, 0, 0xFF00FFFF.toInt())
        missingImage.drawPixel(1, 1, 0x000000FF)
        missingTexture = Texture(missingImage)
        missingTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)
        missingTextureRegion = TextureRegion(missingTexture)
        missingImage.dispose()

        val blankImage = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        blankImage.drawPixel(0, 0, 0xFFFFFFFF.toInt())
        blankTexture = Texture(blankImage)
        blankTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)
        blankTextureRegion = TextureRegion(blankTexture)
        blankImage.dispose()
    }

    fun loadTexture(file: FileHandle, filter: TextureFilter, name: String = file.path()): Boolean {
        if (!file.exists())
            return false

        setImage(Pixmap(file), filter, name)
        return true
    }

    fun loadTextureSingle(file: FileHandle, filter: TextureFilter, name: String = file.path()): Boolean {
        if (!file.exists())
            return false

        val image = Pixmap(file)
        val texture = SingleTexture(image, filter)
        singleTextures.put(name, texture)?.dispose()

        return true
    }

    fun loadTextureSingleAsync(file: FileHandle, filter: TextureFilter, name: String = file.path(), callback: () -> Unit): Boolean {
        if (!file.exists())
            return false

        Game.tasks.submitAsync({
            val image = Pixmap(file)
            Gdx.app.postRunnable {
                val texture = SingleTexture(image, filter)
                singleTextures.put(name, texture)?.dispose()
                callback()
            }
        })

        return true
    }

    fun loadTextureAsync(file: FileHandle, filter: TextureFilter, name: String = file.path(), callback: () -> Unit): Boolean {
        if (!file.exists())
            return false

        Game.tasks.submitAsync({
            val image = Pixmap(file)
            Gdx.app.postRunnable {
                setImage(image, filter, name)
                callback()
            }
        })

        return true
    }

    fun getTexture(file: FileHandle) = getTexture(file.path())

    fun getTexture(name: String, default: (() -> String)? = null): TextureRegion {
        if (name.equals("missing", true))
            return missingTextureRegion

        if (name.equals("blank", true))
            return blankTextureRegion

        val singleTexture = singleTextures[name]
        if (singleTexture != null)
            return singleTexture.region

        val filter = textureFilters[name] ?: return getTexture(default?.invoke() ?: "missing")
        val atlas = textureAtlases[filter] ?: return getTexture(default?.invoke() ?: "missing")
        return atlas[name] ?: getTexture(default?.invoke() ?: "missing")
    }

    fun setImage(image: Pixmap, filter: TextureFilter, name: String) {
        val atlas = textureAtlases.getOrPut(filter) {
            TextureAtlas(filter).also {
                it.texture.setFilter(filter, filter)
            }
        }

        atlas.add(name, image)
        textureFilters[name] = filter
    }

    fun getImage(file: FileHandle) = getImage(file.path())

    fun getImage(name: String): Pixmap? {
        val filter = textureFilters[name] ?: return null
        val atlas = textureAtlases[filter] ?: return null
        return atlas.getImage(name)
    }

    fun getOrLoadTexture(file: FileHandle, filter: TextureFilter): TextureRegion {
        val atlas = textureAtlases[filter]
        val name = file.name()

        if (atlas == null || atlas[name] == null)
            loadTexture(file, filter, name)

        return getTexture(name)
    }

    fun getVersion(name: String): Int? {
        val filter = textureFilters[name] ?: return null
        val atlas = textureAtlases[filter] ?: return null
        return atlas.getVersion(name)
    }

    fun packAtlases() {
        textureAtlases.values.forEach {
            it.pack()
        }
    }

    fun packAtlasesAsync(callback: () -> Unit) {
        Game.tasks.submitAsync({
            val packCount = textureAtlases.size
            var packedCount = 0
            val toPack = textureAtlases.values.toMutableList()

            fun packFirst() {
                if (toPack.isEmpty())
                    return

                Game.tasks.submitAsync({
                    toPack.removeFirst().packAsync {
                        packedCount++
                    }
                })
            }

            packFirst()

            while (packCount != packedCount)
                Thread.sleep(1)

            callback()
        })
    }

    operator fun invoke(file: FileHandle, filter: TextureFilter) = Getter(file, filter)

    override fun dispose() {
        textureAtlases.forEach { (_, atlas) ->
            atlas.dispose()
        }

        singleTextures.forEach { (_, texture) ->
            texture.dispose()
        }

        missingTexture.dispose()
        blankTexture.dispose()
    }
}