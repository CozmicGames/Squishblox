package com.cozmicgames.common.utils.serialization

class FloatSerializable : Serializable {
    var value: Float = 0.0f

    override fun serialize(prettyPrint: Boolean) = value.toString()

    override fun deserialize(data: String) {
        data.toFloatOrNull()?.let {
            value = it
        }
    }
}