package com.cozmicgames.game.utils.serialization

import com.cozmicgames.game.utils.Properties

interface Readable {
    fun read(properties: Properties)
    fun write(properties: Properties)
}