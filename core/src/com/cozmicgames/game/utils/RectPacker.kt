package com.cozmicgames.game.utils

class RectPacker(val width: Int, val height: Int) {
    class Rectangle(var width: Int, var height: Int) {
        constructor(x: Int, y: Int, width: Int, height: Int) : this(width, height) {
            this.x = x
            this.y = y
        }

        constructor(id: Int, width: Int, height: Int) : this(width, height) {
            this.id = id
        }

        var id = 0

        var x = 0
            internal set

        var y = 0
            internal set

        var isPacked = false
            internal set
    }

    private val spaces = arrayListOf<Rectangle>()
    private val packing = arrayListOf<Rectangle>()
    private val packed = arrayListOf<Rectangle>()

    init {
        spaces += Rectangle(width, height)
    }

    /**
    Altered algorithm from https://observablehq.com/@mourner/simple-rectangle-packing
     */
    fun pack(rects: Array<Rectangle>) {
        packing.clear()
        packing.addAll(rects)
        packing.sortByDescending { it.height }

        for (rect in packing) {
            for (i in spaces.indices.reversed()) {
                val space = spaces.getOrNull(i) ?: continue

                if (rect.width > space.width || rect.height > space.height)
                    continue

                rect.x = space.x
                rect.y = space.y

                packed += rect

                rect.isPacked = true

                if (rect.width == space.width && rect.height == space.height)
                    spaces.removeAt(i)
                else if (rect.height == space.height) {
                    space.x += rect.width
                    space.width -= rect.width
                } else if (rect.width == space.width) {
                    space.y += rect.height
                    space.height -= rect.height
                } else {
                    spaces += Rectangle(space.x + rect.width, space.y, space.width - rect.width, rect.height)
                    space.y += rect.height
                    space.height -= rect.height
                }

                break
            }
        }
    }

    operator fun get(id: Int) = packed.find { it.id == id }
}