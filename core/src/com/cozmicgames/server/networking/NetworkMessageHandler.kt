package com.cozmicgames.server.networking

import com.badlogic.gdx.Gdx
import com.cozmicgames.common.networking.NetworkConstants
import com.cozmicgames.common.networking.NetworkMessage
import com.cozmicgames.common.networking.StringEncryption
import com.cozmicgames.common.networking.messages.*
import com.cozmicgames.game.Version
import com.cozmicgames.server.*
import java.util.UUID
import kotlin.Exception

class NetworkMessageHandler {
    private fun checkVersion(message: NetworkMessage): Boolean {
        if (Version.major > message.versionMajor)
            return false
        return Version.minor <= message.versionMinor
    }

    private fun checkKey(message: NetworkMessage): Boolean {
        return StringEncryption.decrypt(message.networkingKey) == NetworkConstants.NETWORKING_KEY
    }

    fun process(message: ReceivedMessage) {
        if (!checkVersion(message.message))
            return

        if (!checkKey(message.message))
            return

        try {
            when (message.message) {
                is SubmitLevelMessage -> {
                    Server.tasks.submitAsync({
                        if (Server.data.canSubmit(message.connection)) {
                            Gdx.app.log("SERVER", "Level submitted from ${message.connection}.")
                            val uuid = Server.data.submitLevel(message.connection, message.message.levelData)
                            Server.networking.send(message.connection, ConfirmSubmitLevelMessage().also {
                                it.isConfirmed = true
                                it.uuid = uuid.toString()
                            })
                        } else {
                            Gdx.app.log("SERVER", "Level submission denied from ${message.connection}.")
                            Server.networking.send(message.connection, ConfirmSubmitLevelMessage().also {
                                it.isConfirmed = false
                            })
                        }
                    })
                }

                is RequestLevelsMessage -> {
                    Server.tasks.submitAsync({
                        Server.networking.send(message.connection, LevelsMessage().also {
                            Server.data.getLevels(it.levels) {
                                it.toString() !in message.message.filterLevelUuids
                            }
                        })
                    })
                }

                is RequestScoreboardMessage -> {
                    try {
                        val uuid = UUID.fromString(message.message.uuid)
                        Server.networking.send(message.connection, ScoreboardMessage().also {
                            it.uuid = uuid.toString()
                            Server.data.getScoreboard(uuid, it.scoreboard)
                        })
                    } catch (_: Exception) {
                    }
                }

                is CheckNameMessage -> {
                    Server.networking.send(message.connection, ConfirmNameMessage().also {
                        it.name = message.message.name
                        it.isConfirmed = !Server.profanityFilter.test("en", message.message.name)
                        Gdx.app.log("SERVER", "Name ${if (it.isConfirmed) "confirmed" else "denied"} (${it.name}).")
                    })
                }

                is SubmitScoreMessage -> {
                    try {
                        val uuid = UUID.fromString(message.message.uuid)
                        Server.data.submitScore(uuid, message.message.name, message.message.time)
                        Gdx.app.log("SERVER", "Score submitted from ${message.connection} (Level: $uuid).")
                    } catch (_: Exception) {
                    }
                }

                is RequestLevelDataMessage -> {
                    Server.tasks.submitAsync({
                        try {
                            val uuid = UUID.fromString(message.message.uuid)
                            val data = Server.data.getLevelData(uuid)
                            if (data != null)
                                Server.networking.send(message.connection, LevelDataMessage().also {
                                    it.uuid = uuid.toString()
                                    it.levelData = data
                                })
                        } catch (_: Exception) {
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Gdx.app.log("SERVER", "An error occured: ${e.message}")
        }
    }
}
