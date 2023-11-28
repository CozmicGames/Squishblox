package com.cozmicgames.game

import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.boolean
import com.cozmicgames.common.utils.int
import com.cozmicgames.common.utils.string
import com.cozmicgames.common.utils.stringArray

class GameSettings : Properties() {
    var isFullscreen by boolean { false }
    var width by int { 800 }
    var height by int { 600 }
    var vsync by boolean { false }
    var language by string { "English" }
}