package com.cozmicgames.common.networking

import com.cozmicgames.game.Version

abstract class NetworkMessage {
    var versionMajor = Version.major
    var versionMinor = Version.minor
    var networkingKey = StringEncryption.encrypt(NetworkConstants.NETWORKING_KEY)
}