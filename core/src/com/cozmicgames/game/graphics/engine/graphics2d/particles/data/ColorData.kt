package com.cozmicgames.game.graphics.engine.graphics2d.particles.data

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData

data class ColorData(val color: Color = Color(Color.WHITE), val startColor: Color = Color(Color.WHITE), val endColor: Color = Color(Color.WHITE)) : ParticleData.DataType

