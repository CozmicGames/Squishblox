package com.cozmicgames.game.utils.serialization

class StringSerializable : Serializable {
    var value: String = ""
    
    override fun serialize(prettyPrint: Boolean) = value

    override fun deserialize(data: String) {
        value = data
    }
}