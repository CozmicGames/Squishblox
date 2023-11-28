package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage

class ConfirmNameMessage : NetworkMessage() {
    var name = ""
    var isConfirmed = false
}