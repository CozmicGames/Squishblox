package com.cozmicgames.common.networking.messages

import com.cozmicgames.common.networking.NetworkMessage
import com.cozmicgames.common.score.ScoreboardEntry

class ScoreboardMessage : NetworkMessage() {
    var uuid = ""
    val scoreboard = arrayOfNulls<ScoreboardEntry>(5)
}