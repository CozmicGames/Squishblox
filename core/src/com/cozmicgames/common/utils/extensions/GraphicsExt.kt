package com.cozmicgames.common.utils.extensions

import com.badlogic.gdx.Graphics


val Graphics.safeWidth get() = width - safeInsetRight - safeInsetLeft
val Graphics.safeHeight get() = height - safeInsetTop - safeInsetBottom

