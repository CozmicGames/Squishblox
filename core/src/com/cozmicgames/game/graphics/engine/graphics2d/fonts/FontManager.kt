package com.cozmicgames.game.graphics.engine.graphics2d.fonts

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable

class FontManager : Disposable {
    private val fonts = hashMapOf<String, DrawableFont>()

    private val defaultFont = MsdfDrawableFont(Gdx.files.classpath("fonts/Roboto-Regular.fnt"), Gdx.files.classpath("fonts/Roboto-Regular.png"))

    fun loadFont(file: FileHandle, name: String = file.path()): Boolean {
        if (!file.exists())
            return false
        //TODO: Make sure file is actually a msdf file, otherwise use normal Bitmapfont
        val previous = fonts.put(name, MsdfDrawableFont(file))
        if (previous !== defaultFont)
            previous?.dispose()

        return true
    }

    fun getFont(name: String): DrawableFont {
        return fonts[name] ?: defaultFont
    }

    fun getOrLoadFont(file: FileHandle, name: String = file.path()): DrawableFont {
        return fonts.getOrPut(name) {
            if (file.exists()) MsdfDrawableFont(file) else defaultFont
        }
    }

    override fun dispose() {
        fonts.forEach { (_, font) ->
            if (font !== defaultFont)
                font.dispose()
        }
        defaultFont.dispose()
    }
}