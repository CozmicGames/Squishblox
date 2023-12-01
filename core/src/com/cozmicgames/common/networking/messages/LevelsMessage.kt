package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage

class LevelsMessage: NetworkMessage() {
    val levels = arrayOfNulls<String>(6)
}
