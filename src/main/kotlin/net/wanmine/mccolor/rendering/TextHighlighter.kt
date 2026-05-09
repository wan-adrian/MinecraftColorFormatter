package net.wanmine.mccolor.rendering

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import java.awt.Font
import com.intellij.ui.JBColor

object TextHighlighter {

    fun highlightText(holder: AnnotationHolder, range: TextRange, color: JBColor) {
        val attributes = TextAttributes(color, null, null, null, Font.PLAIN)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .enforcedTextAttributes(attributes)
            .create()
    }

    fun highlightCode(holder: AnnotationHolder, range: TextRange, color: JBColor) {
        val attributes = TextAttributes(color, null, null, null, Font.BOLD)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .enforcedTextAttributes(attributes)
            .create()
    }
}
