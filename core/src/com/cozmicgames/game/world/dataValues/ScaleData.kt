package com.cozmicgames.game.world.dataValues

import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.serialization.Readable

class ScaleData : Readable {
    var scale = 0.0f

    override fun read(properties: Properties) {
        properties.getFloat("scale")?.let { scale = it }
    }

    override fun write(properties: Properties) {
        properties.setFloat("scale", scale)
    }
}