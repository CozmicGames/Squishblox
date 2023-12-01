package com.cozmicgames.game

import com.cozmicgames.common.utils.*

class GameSettings : Properties() {
    var isFullscreen by boolean { false }
    var width by int { 800 }
    var height by int { 600 }
    var vsync by boolean { false }
    var language by string { "English" }
    var name by string { "" }
    var soundVolume by float { 1.0f }
    var musicVolume by float { 1.0f }
}