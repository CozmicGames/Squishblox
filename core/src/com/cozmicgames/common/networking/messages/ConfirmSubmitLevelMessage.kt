package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage

class ConfirmSubmitLevelMessage: NetworkMessage() {
    var isConfirmed = false
    var uuid = ""
}
