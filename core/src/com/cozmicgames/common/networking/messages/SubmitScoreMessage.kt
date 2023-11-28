package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage

class SubmitScoreMessage : NetworkMessage() {
    var uuid = ""
    var name = ""
    var time = 0L
}