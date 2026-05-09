package net.wanmine.mccolor.settings

import net.wanmine.mccolor.color.ColorVector

/**
 * Very small importer for Minecraft `_global_variables.json`-style entries.
 *
 * The file isn't always strict JSON (often contains `//` comments), so we parse it permissively by regex.
 */
object GlobalVariablesImporter {

    private val ENTRY_REGEX = Regex(
        "\"(?<key>\\$[^\"]+)\"\\s*:\\s*\\[(?<vec>[^\\]]+)]",
        setOf(RegexOption.MULTILINE)
    )

    fun parse(text: String): Map<String, ColorVector> {
        val noComments = stripLineComments(text)
        val out = LinkedHashMap<String, ColorVector>()

        for (m in ENTRY_REGEX.findAll(noComments)) {
            val key = m.groups["key"]?.value ?: continue
            val vecText = m.groups["vec"]?.value ?: continue
            val vec = ColorVector.parse(vecText) ?: continue
            out[key] = vec
        }

        return out
    }

    private fun stripLineComments(input: String): String =
        input.lineSequence()
            .map { line ->
                val idx = line.indexOf("//")
                if (idx >= 0) line.substring(0, idx) else line
            }
            .joinToString("\n")
}

