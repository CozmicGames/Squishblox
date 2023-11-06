package com.cozmicgames.game.graphics.engine.graphics2d

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.graphics.engine.shaders.Shader2D

class RenderOverrides {
    var texture: String? = null
    var shader: String? = null
    var color: Color? = null
    var setUniforms: (Shader2D) -> Unit = {}
}