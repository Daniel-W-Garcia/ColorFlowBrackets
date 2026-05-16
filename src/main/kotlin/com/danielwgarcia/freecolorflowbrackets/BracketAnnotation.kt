package com.danielwgarcia.freecolorflowbrackets

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.ui.JBColor
import java.awt.Color

class BracketAnnotation : Annotator {

    companion object {
        val INDENTATION_LEVEL_0 = key("FREE_CFB_LEVEL_0")
        val INDENTATION_LEVEL_1 = key("FREE_CFB_LEVEL_1")
        val INDENTATION_LEVEL_2 = key("FREE_CFB_LEVEL_2")
        val INDENTATION_LEVEL_3 = key("FREE_CFB_LEVEL_3")
        val INDENTATION_LEVEL_4 = key("FREE_CFB_LEVEL_4")
        val INDENTATION_LEVEL_5 = key("FREE_CFB_LEVEL_5")

        private fun key(name: String): TextAttributesKey =
            TextAttributesKey.createTextAttributesKey(
                name,
                DefaultLanguageHighlighterColors.BRACES
            )

        private val bracketKeys = arrayOf(
            INDENTATION_LEVEL_0, INDENTATION_LEVEL_1, INDENTATION_LEVEL_2,
            INDENTATION_LEVEL_3, INDENTATION_LEVEL_4, INDENTATION_LEVEL_5
        )

        // (light, dark) pairs — works on ANY scheme, including third-party themes.
        private val defaultColors = arrayOf(
            JBColor(Color(235, 63, 0), Color(255, 251, 0)),
            JBColor(Color(26, 64, 252), Color(34, 235, 31)),
            JBColor(Color(224, 37, 242), Color(48, 140, 252)),
            JBColor(Color(0, 212, 212), Color(224, 37, 242)),
            JBColor(Color(217, 178, 42), Color(50, 255, 253)),
            JBColor(Color(13, 227, 12), Color(255, 104, 50))
        )
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LeafPsiElement) return
        val text = element.text
        if (text.length != 1) return
        val ch = text[0]
        if (ch != '{' && ch != '}') return

        // Skip braces that are inside strings, char literals, or comments.
        if (isInsideStringCharOrComment(element)) return

        val depth = calculateNestingDepth(element, ch)
        val idx = depth % bracketKeys.size
        val colorKey = bracketKeys[idx]

        val attrs = resolveAttributes(colorKey, idx)

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.textRange)
            .enforcedTextAttributes(attrs)
            .create()
    }
    private fun isInsideStringCharOrComment(element: PsiElement): Boolean {
        var current: PsiElement? = element.parent
        while (current != null && current !== element.containingFile) {
            val typeName = current.node?.elementType?.toString()?.uppercase().orEmpty()
            if ("STRING"    in typeName ||
                "CHAR"      in typeName ||
                "COMMENT"   in typeName ||
                "REGEX"     in typeName ||      // regex literals (JS, etc.)
                "TEMPLATE"  in typeName) {      // string templates / interpolation hosts
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun resolveAttributes(key: TextAttributesKey, depth: Int): TextAttributes {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val keyAttrs = scheme.getAttributes(key)
        val bracesFg = scheme.getAttributes(DefaultLanguageHighlighterColors.BRACES)?.foregroundColor

        val keyFg = keyAttrs?.foregroundColor
        val userCustomized = keyFg != null && keyFg != bracesFg

        return if (userCustomized) {
            keyAttrs.clone()
        } else {
            TextAttributes().apply {
                foregroundColor = defaultColors[depth]
            }
        }
    }

    private fun calculateNestingDepth(bracketElement: PsiElement, ch: Char): Int {
        val text = bracketElement.containingFile.text
        val end = bracketElement.textRange.startOffset

        var depth = 0
        var i = 0
        while (i < end) {
            when (text[i]) {
                '{' -> depth++
                '}' -> depth--
                '/' -> {
                    if (i + 1 < end && text[i + 1] == '/') {
                        while (i < end && text[i] != '\n') i++
                        continue
                    } else if (i + 1 < end && text[i + 1] == '*') {
                        i += 2
                        while (i + 1 < end && !(text[i] == '*' && text[i + 1] == '/')) i++
                        i += 2
                        continue
                    }
                }
                '"', '\'' -> {
                    val quote = text[i]; i++
                    while (i < end && text[i] != quote) {
                        if (text[i] == '\\') i++
                        i++
                    }
                }
            }
            i++
        }
        return if (ch == '}') maxOf(0, depth - 1) else maxOf(0, depth)
    }
}