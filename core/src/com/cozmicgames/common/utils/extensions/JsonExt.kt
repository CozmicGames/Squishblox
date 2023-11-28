package com.cozmicgames.common.utils.extensions

import kotlinx.serialization.json.JsonPrimitive

val JsonPrimitive.stringOrNull get() = if (isString) content.removeSurrounding("\"") else null
