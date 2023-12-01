package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage

class RequestLevelsMessage: NetworkMessage() {
    val filterLevelUuids = arrayOfNulls<String>(6)
}
