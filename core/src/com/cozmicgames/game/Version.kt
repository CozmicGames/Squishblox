package com.cozmicgames.game

object Version {
    enum class Type(val designation: String) {
        DEVELOPMENT("indev"),
        RELEASE("release")
    }

    val major = 0
    val minor = 1
    val type = Type.DEVELOPMENT

    val versionString get() = "Version $major.$minor - ${type.designation}"
}