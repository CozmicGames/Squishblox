package com.cozmicgames.game.graphics.engine.shaders

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable

class ShaderManager : Disposable {
    private val shaders = hashMapOf<String, Shader2D>()
    private val defaultShader = Shader2D(SpriteBatch.createDefaultShader())

    fun registerShader(name: String, source: String): Boolean {
        val shader = Shader2D(source)
        shaders.put(name, shader)?.dispose()
        return shader.isCompiled
    }

    fun loadShader(file: FileHandle, name: String = file.path()): Boolean {
        if (file.exists())
            return registerShader(name, file.readString())

        return false
    }

    fun getShader(name: String): Shader2D {
        return shaders[name] ?: defaultShader
    }

    fun getOrLoadShader(file: FileHandle, name: String = file.pathWithoutExtension()): Shader2D {
        return shaders.getOrPut(name) {
            if (file.exists()) Shader2D(file.readString()) else defaultShader
        }
    }

    override fun dispose() {
        shaders.forEach { (_, shader) ->
            if (shader !== defaultShader)
                shader.dispose()
        }
    }
}