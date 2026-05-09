package net.wanmine.mccolor.color

import com.intellij.ui.JBColor
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Represents a Minecraft _global_variables.json color entry: [ r, g, b ] with floats 0.0..1.0.
 */
data class ColorVector(
    var r: Float = 0f,
    var g: Float = 0f,
    var b: Float = 0f
) {
    fun toJBColor(): JBColor {
        val c = Color(
            (r.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255),
            (g.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255),
            (b.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255)
        )
        return JBColor(c, c)
    }

    fun toGlobalVariablesFormat(): String =
        "[ ${format(r)}, ${format(g)}, ${format(b)} ]"

    companion object {
        fun parse(input: String): ColorVector? {
            // Accept: [ 0.1, 0.2, 0.3 ] or "0.1,0.2,0.3"
            val cleaned = input.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .trim()

            val parts = cleaned.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (parts.size != 3) return null

            val r = parseComponent(parts[0]) ?: return null
            val g = parseComponent(parts[1]) ?: return null
            val b = parseComponent(parts[2]) ?: return null

            return ColorVector(r, g, b)
        }

        /**
         * Supports:
         * - floats 0.0..1.0 (vanilla `_global_variables.json` style)
         * - integers 0..255 (treated as value/255)
         * - fractions like "221/255" or "221 / 255"
         */
        private fun parseComponent(input: String): Float? {
            val s = input.trim()

            // fraction "n/255"
            val slash = s.indexOf('/')
            if (slash >= 0) {
                val left = s.substring(0, slash).trim()
                val right = s.substring(slash + 1).trim()
                val n = left.toFloatOrNull() ?: return null
                val d = right.toFloatOrNull() ?: return null
                if (d == 0f) return null
                return (n / d).coerceIn(0f, 1f)
            }

            // integer 0..255 -> /255
            val asInt = s.toIntOrNull()
            if (asInt != null) {
                return (asInt / 255f).coerceIn(0f, 1f)
            }

            // float 0..1
            val asFloat = s.toFloatOrNull() ?: return null
            return asFloat.coerceIn(0f, 1f)
        }

        private fun format(v: Float): String {
            val rounded = (v * 1000f).roundToInt() / 1000f
            // Keep at least one decimal (matches typical resource JSON style).
            val s = rounded.toString()
            return if (s.contains('.')) s else "$s.0"
        }
    }
}
