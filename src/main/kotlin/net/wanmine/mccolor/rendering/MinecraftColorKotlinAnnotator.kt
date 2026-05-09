package net.wanmine.mccolor.rendering

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class MinecraftColorKotlinAnnotator : MinecraftColorAnnotatorBase(), Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val literal = element as? KtStringTemplateExpression ?: return
        if (literal.hasInterpolation()) return

        val rawText = literal.text ?: return
        val fileOffset = literal.textRange.startOffset

        val incoming = computeIncomingColorForKotlinLiteral(literal)

        val hasCodes = rawText.contains(SECTION_SIGN) || rawText.contains(UNICODE_ESCAPE, ignoreCase = true)
        if (!hasCodes && incoming == null) return

        highlightStringLiteral(literal.project, rawText, holder, fileOffset, incoming)
    }

    private fun computeIncomingColorForKotlinLiteral(literal: KtStringTemplateExpression): JBColor? {
        val plusRoot = findTopmostPlusExpression(literal) ?: return null
        val operands = flattenPlusOperands(plusRoot)
        val idx = operands.indexOfFirst { it.textRange == literal.textRange }
        if (idx <= 0) return null

        var state: JBColor? = null
        for (i in 0 until idx) {
            val s = evalConstantString(operands[i]) ?: return null
            state = computeFinalColor(literal.project, s, state)
        }
        return state
    }

    private fun findTopmostPlusExpression(start: PsiElement): KtBinaryExpression? {
        var current: PsiElement? = start.parent
        var top: KtBinaryExpression? = null
        while (current != null) {
            val bin = current as? KtBinaryExpression
            if (bin != null && bin.operationToken == KtTokens.PLUS) {
                top = bin
            }
            current = current.parent
        }
        return top
    }

    private fun flattenPlusOperands(expr: KtExpression): List<KtExpression> {
        val out = ArrayList<KtExpression>()
        fun walk(e: KtExpression) {
            val deparen = (e as? KtParenthesizedExpression)?.expression ?: e
            val bin = deparen as? KtBinaryExpression
            if (bin != null && bin.operationToken == KtTokens.PLUS) {
                val left = bin.left
                val right = bin.right
                if (left != null) walk(left)
                if (right != null) walk(right)
            } else {
                out.add(deparen)
            }
        }
        walk(expr)
        return out
    }

    private fun evalConstantString(expr: KtExpression): String? {
        val deparen = (expr as? KtParenthesizedExpression)?.expression ?: expr

        val lit = deparen as? KtStringTemplateExpression
        if (lit != null) {
            if (lit.hasInterpolation()) return null
            return plainContent(lit)
        }

        val ref = deparen as? KtNameReferenceExpression
        if (ref != null) {
            val resolved = runCatching { ref.mainReference.resolve() }.getOrNull()
            val prop = resolved as? KtProperty ?: return null
            // Prefer const vals, but also accept regular `val` properties with a plain string initializer.
            // This enables carry-over for patterns like `Main.PREFIX + "message"` in typical codebases.
            if (prop.isVar) return null
            val init = prop.initializer as? KtStringTemplateExpression ?: return null
            if (init.hasInterpolation()) return null
            return plainContent(init)
        }

        return null
    }

    private fun plainContent(literal: KtStringTemplateExpression): String =
        literal.entries.joinToString(separator = "") { it.text }
}
