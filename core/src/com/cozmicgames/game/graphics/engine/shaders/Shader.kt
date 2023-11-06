package com.cozmicgames.game.graphics.engine.shaders

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import java.lang.RuntimeException

open class Shader(val shaderProgram: ShaderProgram) : Disposable {
    val isCompiled get() = shaderProgram.isCompiled

    init {
        if (!isCompiled)
            throw RuntimeException("Failed to compile shader:\n${shaderProgram.log}")
    }

    fun setUniform(name: String, value: Boolean) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, if (value) 1.0f else 0.0f)
    }

    fun setUniform(name: String, value: Float) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, value)
    }

    fun setUniform(name: String, value0: Float, value1: Float) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, value0, value1)
    }

    fun setUniform(name: String, value0: Float, value1: Float, value2: Float) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, value0, value1, value2)
    }

    fun setUniform(name: String, value0: Float, value1: Float, value2: Float, value3: Float) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, value0, value1, value2, value3)
    }

    fun setUniform(name: String, value: Int) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformi(name, value)
    }

    fun setUniform(name: String, value0: Int, value1: Int) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformi(name, value0, value1)
    }

    fun setUniform(name: String, value0: Int, value1: Int, value2: Int) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformi(name, value0, value1, value2)
    }

    fun setUniform(name: String, value0: Int, value1: Int, value2: Int, value3: Int) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformi(name, value0, value1, value2, value3)
    }

    fun setUniform(name: String, vector: Vector2) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, vector)
    }

    fun setUniform(name: String, vector: Vector3) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, vector)
    }

    fun setUniform(name: String, color: Color) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformf(name, color)
    }

    fun setUniform(name: String, matrix: Matrix3, transpose: Boolean = false) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformMatrix(name, matrix, transpose)
    }

    fun setUniform(name: String, matrix: Matrix4, transpose: Boolean = false) {
        if (shaderProgram.hasUniform(name))
            shaderProgram.setUniformMatrix(name, matrix, transpose)
    }

    override fun dispose() {
        shaderProgram.dispose()
    }
}