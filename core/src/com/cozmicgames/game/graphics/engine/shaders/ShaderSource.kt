package com.cozmicgames.game.graphics.engine.shaders

class ShaderSource(val uniforms: String, val vertex: String, val fragment: String) {
    companion object {
        private const val UNIFORMS_SECTION = "#uniforms"
        private const val VERTEX_SECTION = "#vertex"
        private const val FRAGMENT_SECTION = "#fragment"

        private fun parseUniformSection(source: String): String {
            val lines = source.lines()
            val uniformsSourceIndex = lines.indexOfFirst { it.trim() == UNIFORMS_SECTION }
            var vertexSourceIndex = lines.indexOfFirst { it.trim() == VERTEX_SECTION }
            var fragmentSourceIndex = lines.indexOfFirst { it.trim() == FRAGMENT_SECTION }

            if (uniformsSourceIndex < 0)
                return ""

            if (vertexSourceIndex < uniformsSourceIndex)
                vertexSourceIndex = Int.MAX_VALUE

            if (fragmentSourceIndex < uniformsSourceIndex)
                fragmentSourceIndex = Int.MAX_VALUE

            val endIndex = minOf(vertexSourceIndex, fragmentSourceIndex, lines.size)

            if (endIndex == uniformsSourceIndex - 1)
                return ""

            return lines.subList(uniformsSourceIndex + 1, endIndex).joinToString("\n")
        }

        private fun parseVertexSection(source: String): String {
            val lines = source.lines()
            val vertexSourceIndex = lines.indexOfFirst { it.trim() == VERTEX_SECTION }
            var uniformsSourceIndex = lines.indexOfFirst { it.trim() == UNIFORMS_SECTION }
            var fragmentSourceIndex = lines.indexOfFirst { it.trim() == FRAGMENT_SECTION }

            if (vertexSourceIndex < 0)
                return ""

            if (uniformsSourceIndex < vertexSourceIndex)
                uniformsSourceIndex = Int.MAX_VALUE

            if (fragmentSourceIndex < vertexSourceIndex)
                fragmentSourceIndex = Int.MAX_VALUE

            val endIndex = minOf(uniformsSourceIndex, fragmentSourceIndex, lines.size)

            if (endIndex == vertexSourceIndex - 1)
                return ""

            return lines.subList(vertexSourceIndex + 1, endIndex).joinToString("\n")
        }

        private fun parseFragmentSection(source: String): String {
            val lines = source.lines()
            val fragmentSourceIndex = lines.indexOfFirst { it.trim() == FRAGMENT_SECTION }
            var uniformsSourceIndex = lines.indexOfFirst { it.trim() == UNIFORMS_SECTION }
            var vertexSourceIndex = lines.indexOfFirst { it.trim() == VERTEX_SECTION }

            if (fragmentSourceIndex < 0)
                return ""

            if (uniformsSourceIndex < fragmentSourceIndex)
                uniformsSourceIndex = Int.MAX_VALUE

            if (vertexSourceIndex < fragmentSourceIndex)
                vertexSourceIndex = Int.MAX_VALUE

            val endIndex = minOf(uniformsSourceIndex, vertexSourceIndex, lines.size)

            if (endIndex == fragmentSourceIndex - 1)
                return ""

            return lines.subList(fragmentSourceIndex + 1, endIndex).joinToString("\n")
        }
    }

    constructor(source: String) : this(parseUniformSection(source), parseVertexSection(source), parseFragmentSection(source))
}