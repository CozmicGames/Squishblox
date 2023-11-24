package com.cozmicgames.game.world.dataValues

import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.serialization.Readable

class PlatformData : Readable {
    var fromMinX = 0.0f
    var fromMinY = 0.0f
    var toMinX = 0.0f
    var toMinY = 0.0f
    var currentMoveDirection = 1.0f
    var playerBlockId: Int? = null
    var currentDeltaX = 0.0f
    var currentDeltaY = 0.0f

    override fun read(properties: Properties) {
        properties.getFloat("fromMinX")?.let { fromMinX = it }
        properties.getFloat("fromMinY")?.let { fromMinY = it }
        properties.getFloat("toMinX")?.let { toMinX = it }
        properties.getFloat("toMinY")?.let { toMinY = it }
    }

    override fun write(properties: Properties) {
        properties.setFloat("fromMinX", fromMinX)
        properties.setFloat("fromMinY", fromMinY)
        properties.setFloat("toMinX", toMinX)
        properties.setFloat("toMinY", toMinY)
    }
}