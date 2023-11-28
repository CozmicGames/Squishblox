package com.cozmicgames.common.utils.serialization

import com.cozmicgames.common.utils.Properties

interface Readable {
    fun read(properties: Properties)
    fun write(properties: Properties)
}