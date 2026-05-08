package net.wanmine.mccolor.rendering

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import net.wanmine.mccolor.color.LegacyColorCodeResolver
import java.awt.Color

/**
 * Highlights Minecraft legacy color codes (Bedrock palette) in Java/Kotlin string literals.
 *
 * Examples:
 * - "§dRezzy§5Land §8» §r"
 * - "\\u00A7dRezzy\\u00A75Land" (common source representation)
 */
class MinecraftColorAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text ?: return
        if (!isStringLiteral(text)) return

        if (!text.contains(SECTION_SIGN) && !text.contains(UNICODE_ESCAPE, ignoreCase = true)) return

        val fileOffset = element.textRange.startOffset
        val stringStart = findStringStart(text)
        val stringEnd = findStringEnd(text)
        if (stringStart == -1 || stringEnd == -1 || stringStart >= stringEnd) return

        annotateLegacyCodes(text, holder, fileOffset, stringStart, stringEnd)
    }

    private fun annotateLegacyCodes(
        text: String,
        holder: AnnotationHolder,
        fileOffset: Int,
        stringStart: Int,
        stringEnd: Int
    ) {
        var currentColor: Color? = null
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

            // §r resets formatting (including color) to default.
            if (code == 'r') {
                if (segmentStart < i) {
                    currentColor?.let { color ->
                        TextHighlighter.highlightText(
                            holder,
                            TextRange(fileOffset + segmentStart, fileOffset + i),
                            color
                        )
                    }
                }
                // Use the current color for the reset marker if we have one; otherwise don't force a color.
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

            val nextColor = LegacyColorCodeResolver.resolve(code)
            if (nextColor == null) {
                // Unknown or non-color code (e.g. §l bold). Keep current color.
                i = codeIndex + 1
                continue
            }

            if (segmentStart < i) {
                currentColor?.let { color ->
                    TextHighlighter.highlightText(
                        holder,
                        TextRange(fileOffset + segmentStart, fileOffset + i),
                        color
                    )
                }
            }

            TextHighlighter.highlightCode(
                holder,
                TextRange(fileOffset + i, fileOffset + codeIndex + 1),
                nextColor
            )

            currentColor = nextColor
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

    private fun sectionPrefixLengthAt(text: String, index: Int, stringEnd: Int): Int {
        if (index >= stringEnd) return 0
        if (text[index] == SECTION_SIGN) return 1

        // Support the common source form "\u00A7" inside strings.
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

    private companion object {
        private const val SECTION_SIGN: Char = '\u00A7'
        private const val UNICODE_ESCAPE: String = "\\u00A7"
    }
}

