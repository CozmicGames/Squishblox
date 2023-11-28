package com.cozmicgames.common.utils.extensions

import kotlin.random.Random

fun Random.nextBoolean(probability: Float = 0.5f) = nextFloat() <= probability