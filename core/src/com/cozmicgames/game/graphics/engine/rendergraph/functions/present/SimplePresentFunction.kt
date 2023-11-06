package com.cozmicgames.game.graphics.engine.rendergraph.functions.present

open class SimplePresentFunction(dependencyName: String, dependencyIndex: Int) : PresentFunction(
    """
        vec4 effect() {
            return getColor();
        }
    """, dependencyName, dependencyIndex
)