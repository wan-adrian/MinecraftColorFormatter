package net.wanmine.mccolor.rendering

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.ui.JBColor
import net.wanmine.mccolor.color.LegacyColorCodeResolver

abstract class MinecraftColorAnnotatorBase {

    protected fun highlightStringLiteral(
        project: Project,
        rawText: String,
        holder: AnnotationHolder,
        fileOffset: Int,
        initialColor: JBColor?
    ) {
        if (!isStringLiteral(rawText)) return

        val stringStart = findStringStart(rawText)
        val stringEnd = findStringEnd(rawText)
        if (stringStart == -1 || stringEnd == -1 || stringStart >= stringEnd) return

        highlightLegacyCodes(project, rawText, holder, fileOffset, stringStart, stringEnd, initialColor)
    }

    protected fun highlightLegacyCodes(
        project: Project,
        text: String,
        holder: AnnotationHolder,
        fileOffset: Int,
        stringStart: Int,
        stringEnd: Int,
        initialColor: JBColor?
    ) {
        var currentColor: JBColor? = initialColor
        var segmentStart = stringStart
        var i = stringStart

        while (i < stringEnd) {
            val prefixLen = sectionPrefixLengthAt(text, i, stringEnd)
            if (prefixLen == 0) {
                i++
                continue
            }

            val codeIndex = i + prefixLen
            if (codeIndex >= stringEnd) break

            val code = text[codeIndex].lowercaseChar()

            // Flush text up to the code with the current color.
            if (segmentStart < i) {
                currentColor?.let { color ->
                    TextHighlighter.highlightText(
                        holder,
                        TextRange(fileOffset + segmentStart, fileOffset + i),
                        color
                    )
                }
            }

            if (code == 'r') {
                // Highlight the reset marker in the current color (if any), then clear.
                currentColor?.let { color ->
                    TextHighlighter.highlightCode(
                        holder,
                        TextRange(fileOffset + i, fileOffset + codeIndex + 1),
                        color
                    )
                }
                currentColor = null
                i = codeIndex + 1
                segmentStart = i
                continue
            }

            val nextColor = LegacyColorCodeResolver.resolve(code, project)
            if (nextColor != null) {
                TextHighlighter.highlightCode(
                    holder,
                    TextRange(fileOffset + i, fileOffset + codeIndex + 1),
                    nextColor
                )
                currentColor = nextColor
            }

            i = codeIndex + 1
            segmentStart = i
        }

        if (segmentStart < stringEnd) {
            currentColor?.let { color ->
                TextHighlighter.highlightText(
                    holder,
                    TextRange(fileOffset + segmentStart, fileOffset + stringEnd),
                    color
                )
            }
        }
    }

    protected fun computeFinalColor(project: Project, value: String, startColor: JBColor? = null): JBColor? {
        var current: JBColor? = startColor
        var i = 0
        while (i < value.length) {
            val idx = value.indexOf(SECTION_SIGN, i)
            if (idx < 0 || idx + 1 >= value.length) break
            val code = value[idx + 1].lowercaseChar()
            if (code == 'r') {
                current = null
            } else {
                LegacyColorCodeResolver.resolve(code, project)?.let { current = it }
            }
            i = idx + 2
        }
        return current
    }

    private fun sectionPrefixLengthAt(text: String, index: Int, stringEnd: Int): Int {
        if (index >= stringEnd) return 0
        if (text[index] == SECTION_SIGN) return 1

        // Support the source form "\\u00A7" inside strings.
        if (index + 6 <= stringEnd &&
            text[index] == '\\' &&
            (text[index + 1] == 'u' || text[index + 1] == 'U') &&
            text[index + 2] == '0' &&
            text[index + 3] == '0' &&
            (text[index + 4] == 'a' || text[index + 4] == 'A') &&
            text[index + 5] == '7'
        ) {
            return 6
        }

        return 0
    }

    private fun isStringLiteral(text: String): Boolean {
        val trimmed = text.trim()
        return (trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'"))
    }

    private fun findStringStart(text: String): Int {
        val firstQuote = text.indexOfFirst { it == '"' || it == '\'' }
        return if (firstQuote != -1) firstQuote + 1 else -1
    }

    private fun findStringEnd(text: String): Int {
        val firstQuote = text.indexOfFirst { it == '"' || it == '\'' }
        if (firstQuote == -1) return -1

        val quoteChar = text[firstQuote]
        val lastQuote = text.lastIndexOf(quoteChar)
        return if (lastQuote > firstQuote) lastQuote else -1
    }

    protected companion object {
        const val SECTION_SIGN: Char = '\u00A7'
        const val UNICODE_ESCAPE: String = "\\u00A7"
    }
}
