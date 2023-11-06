package com.cozmicgames.game.utils.serialization

interface Serializable {
    fun serialize(prettyPrint: Boolean = true): String
    fun deserialize(data: String)
}
