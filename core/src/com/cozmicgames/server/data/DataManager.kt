package com.cozmicgames.server.data

import com.cozmicgames.common.score.ScoreboardEntry
import com.cozmicgames.server.Server
import com.cozmicgames.server.dataRoot
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedDeque

class DataManager {
    private val uuids = ConcurrentLinkedDeque<UUID>()
    private val tempScoreboard = ThreadLocal.withInitial { arrayOfNulls<ScoreboardEntry>(5) }

    init {
        Server.dataRoot.list("json").forEach {
            try {
                val uuid = UUID.fromString(it.nameWithoutExtension())
                uuids += uuid
            } catch (_: Exception) {
                // This shouldn't happen, but just in case
                it.delete()
                val scoresFile = it.sibling("${it.name()}_scores.txt")
                if (scoresFile.exists())
                    scoresFile.delete()
            }
        }
    }

    fun submitLevel(levelData: String): UUID {
        var uuid = UUID.randomUUID()

        while (uuid in uuids)
            uuid = UUID.randomUUID()

        uuids += uuid

        val file = Server.dataRoot.child("$uuid.json")
        file.writeString(levelData, false)

        return uuid
    }

    fun submitScore(uuid: UUID, name: String, time: Long) {
        val scoreboard = tempScoreboard.get()
        getScoreboard(uuid, scoreboard)

        var index = scoreboard.size

        repeat(scoreboard.size) {
            val scoreboardIndex = scoreboard.size - 1 - it
            val entry = scoreboard[scoreboardIndex]
            if (entry == null || entry.time > time)
                index--
        }

        if (index < scoreboard.size) {
            if (index < scoreboard.lastIndex)
                scoreboard.copyInto(scoreboard, index + 1, index, scoreboard.size - 1)

            scoreboard[index] = ScoreboardEntry().also {
                it.name = name
                it.time = time
            }

            val file = Server.dataRoot.child("${uuid}_scores.txt")
            file.writeString(buildString {
                for (entry in scoreboard) {
                    if (entry == null)
                        break

                    appendLine("${entry.name}=${entry.time}")
                }
            }, false)
        }
    }

    fun getScoreboard(uuid: UUID, scoreboard: Array<ScoreboardEntry?>) {
        val file = Server.dataRoot.child("${uuid}_scores.txt")
        if (!file.exists())
            return

        var index = 0
        for (line in file.readString().lineSequence()) {
            val parts = line.split("=")
            val name = parts.getOrNull(0) ?: continue
            val time = parts.getOrNull(1)?.toLongOrNull() ?: continue

            scoreboard[index] = ScoreboardEntry().also {
                it.name = name
                it.time = time
            }
            index++

            if (index >= scoreboard.size)
                return
        }
    }

    fun getLevels(levelUuids: Array<String?>, filter: (UUID) -> Boolean) {
        repeat(levelUuids.size) {
            var tries = 0

            var uuid = uuids.random()

            while (!filter(uuid) || uuid.toString() in levelUuids) {
                uuid = uuids.random()
                tries++

                if (tries > 100) //If we didn't find a fitting uuid, maybe there aren't enough available. Just return then.
                    return
            }

            levelUuids[it] = uuid.toString()
        }
    }

    fun getLevelData(uuid: UUID): String? {
        val file = Server.dataRoot.child("$uuid.json")
        if (!file.exists())
            return null

        return file.readString()
    }
}