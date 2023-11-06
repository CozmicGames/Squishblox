package com.cozmicgames.game.utils.serialization

class BooleanSerializable : Serializable {
    var value: Boolean = false

    override fun serialize(prettyPrint: Boolean) = value.toString()

    override fun deserialize(data: String) {
        data.lowercase().toBooleanStrictOrNull()?.let {
            value = it
        }
    }
}