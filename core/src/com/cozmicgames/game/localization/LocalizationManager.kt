package com.cozmicgames.game.localization

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.cozmicgames.game.Game
import com.cozmicgames.game.gameSettings
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.lang.Exception

class LocalizationManager {
    private val languages = hashMapOf<String, Map<String, String>>()

    val availableLanguages get() = languages.keys.toSet()

    init {
        load(Gdx.files.internal("languages.csv"))
    }

    fun load(file: FileHandle) {
        if (!file.exists())
            return

        languages.clear()
        val rows = csvReader {
            delimiter = ';'
        }.readAll(file.readString())

        for (i in 1 until rows[0].size) {
            val map = hashMapOf<String, String>()

            for (j in 1 until rows.size) {
                val key = try {
                    rows[j][0]
                } catch (e: Exception) {
                    null
                } ?: continue

                val value = try {
                    rows[j][i]
                } catch (e: Exception) {
                    null
                } ?: continue

                map[key] = value
            }

            languages[rows[0][i]] = map
        }
    }

    operator fun get(key: String): String {
        return languages[Game.gameSettings.language]?.get(key) ?: key
    }
}