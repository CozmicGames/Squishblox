package com.cozmicgames.game.utils.serialization

class IntSerializable : Serializable {
    var value: Int = 0

    override fun serialize(prettyPrint: Boolean) = value.toString()

    override fun deserialize(data: String) {
        data.toIntOrNull()?.let {
            value = it
        }
    }
}