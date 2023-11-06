package com.cozmicgames.game.graphics.engine.graphics2d.sprite

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.cozmicgames.game.Game
import com.cozmicgames.game.utils.string
import com.cozmicgames.game.graphics.engine.shaders.Shader2D
import com.cozmicgames.game.textures

class StaticSpriteMaterial : SpriteMaterial() {
    var texture by string { "blank" }
}