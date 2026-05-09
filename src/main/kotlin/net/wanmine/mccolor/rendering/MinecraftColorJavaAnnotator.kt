package net.wanmine.mccolor.rendering

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.JavaTokenType
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiPolyadicExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiVariable
import com.intellij.ui.JBColor

class MinecraftColorJavaAnnotator : MinecraftColorAnnotatorBase(), Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val literal = element as? PsiLiteralExpression ?: return
        val value = literal.value as? String ?: return

        val rawText = literal.text ?: return
        val fileOffset = literal.textRange.startOffset

        val incoming = computeIncomingColorForJavaLiteral(literal, value)

        // Fast path: if neither this literal nor its incoming state contains color, don't touch it.
        val hasCodes = rawText.contains(SECTION_SIGN) || rawText.contains(UNICODE_ESCAPE, ignoreCase = true)
        if (!hasCodes && incoming == null) return

        highlightStringLiteral(literal.project, rawText, holder, fileOffset, incoming)
    }

    private fun computeIncomingColorForJavaLiteral(literal: PsiLiteralExpression, literalValue: String): JBColor? {
        val poly = findParentPlusExpression(literal) ?: return null
        val operands = poly.operands
        val idx = operands.indexOf(literal as PsiExpression)
        if (idx <= 0) return null

        var state: JBColor? = null
        val evaluator = JavaPsiFacade.getInstance(literal.project).constantEvaluationHelper

        for (i in 0 until idx) {
            val op = operands[i]
            val s = evalConstantString(evaluator, op) ?: return null
            state = computeFinalColor(literal.project, s, state)
        }

        // If the literal itself is the first operand we don't need incoming; otherwise state might be used.
        // Also update state with the literal value so later operands can carry (handled in their own annotate).
        @Suppress("UNUSED_VARIABLE")
        val _ignored = computeFinalColor(literal.project, literalValue, state)
        return state
    }

    private fun findParentPlusExpression(element: PsiElement): PsiPolyadicExpression? {
        var current: PsiElement? = element.parent
        while (current != null) {
            val poly = current as? PsiPolyadicExpression
            if (poly != null && poly.operationTokenType == JavaTokenType.PLUS) {
                return poly
            }
            current = current.parent
        }
        return null
    }

    private fun evalConstantString(
        evaluator: com.intellij.psi.PsiConstantEvaluationHelper,
        expr: PsiExpression
    ): String? {
        val direct = (expr as? PsiLiteralExpression)?.value as? String
        if (direct != null) return direct

        // Heuristic: allow carry-over from non-constant fields/vals when their initializer is a literal.
        // This helps cases like `Main.PREFIX + "message"` when PREFIX is declared as `static String` / `val`.
        val ref = expr as? PsiReferenceExpression
        if (ref != null) {
            val resolved = runCatching { ref.resolve() }.getOrNull()
            val variable = resolved as? PsiVariable
            val init = variable?.initializer
            if (init != null) {
                val initDirect = (init as? PsiLiteralExpression)?.value as? String
                if (initDirect != null) return initDirect

                val initComputed = evaluator.computeConstantExpression(init)
                if (initComputed is String) return initComputed
            }
        }

        val computed = evaluator.computeConstantExpression(expr)
        return computed as? String
    }
}
