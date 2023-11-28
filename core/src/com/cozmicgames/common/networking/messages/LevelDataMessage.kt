package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage

class LevelDataMessage : NetworkMessage() {
    var uuid = ""
    var levelData = ""
}