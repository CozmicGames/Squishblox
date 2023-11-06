package com.cozmicgames.game

import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.boolean
import com.cozmicgames.game.utils.int
import com.cozmicgames.game.utils.string
import com.cozmicgames.game.utils.stringArray

class GameSettings : Properties() {
    var isFullscreen by boolean { false }
    var width by int { 800 }
    var height by int { 600 }
    var vsync by boolean { false }
    var localPlayers by stringArray { emptyArray() }
    var players by stringArray { emptyArray() }
    var language by string { "English" }
}