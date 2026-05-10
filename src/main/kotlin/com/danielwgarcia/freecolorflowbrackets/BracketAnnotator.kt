package com.danielwgarcia.freecolorflowbrackets

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.Font

class BracketAnnotator : Annotator {

    companion object {
        // Single source of truth for default colors
        private object DefaultColors {
            val LEVEL_0 = Color(255, 251, 0)        // Yellow
            val LEVEL_1 = Color(44, 237, 16)         // Green
            val LEVEL_2 = Color(0xFF, 0x8C, 0x00)    // Orange
            val LEVEL_3 = Color(79, 79, 227)    // Blue
            val LEVEL_4 = Color(0x99, 0x00, 0xAA)    // Purple
            val LEVEL_5 = Color(0x00, 0x77, 0x99)    // Teal
        }

        // Pass TextAttributes directly as the fallback — no intermediate named key needed
        private fun createBracketKey(name: String, color: Color): TextAttributesKey =
            TextAttributesKey.createTextAttributesKey(
                name,
                TextAttributes(color, null, null, null, Font.PLAIN)
            )

        val BRACKET_LEVEL_0 = createBracketKey("BRACKET_LEVEL_0", DefaultColors.LEVEL_0)
        val BRACKET_LEVEL_1 = createBracketKey("BRACKET_LEVEL_1", DefaultColors.LEVEL_1)
        val BRACKET_LEVEL_2 = createBracketKey("BRACKET_LEVEL_2", DefaultColors.LEVEL_2)
        val BRACKET_LEVEL_3 = createBracketKey("BRACKET_LEVEL_3", DefaultColors.LEVEL_3)
        val BRACKET_LEVEL_4 = createBracketKey("BRACKET_LEVEL_4", DefaultColors.LEVEL_4)
        val BRACKET_LEVEL_5 = createBracketKey("BRACKET_LEVEL_5", DefaultColors.LEVEL_5)

        private val bracketColors = arrayOf(
            BRACKET_LEVEL_0, BRACKET_LEVEL_1, BRACKET_LEVEL_2,
            BRACKET_LEVEL_3, BRACKET_LEVEL_4, BRACKET_LEVEL_5
        )
    }



    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text

        // Only process single character curly brackets
        if (text.length == 1 && isBracket(text[0])) {
            val depth = calculateNestingDepth(element)
            val colorKey = bracketColors[depth % bracketColors.size]

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(colorKey)
                .create()
        }
    }

    private fun isBracket(char: Char): Boolean {
        return char in "{}"
    }

    private fun calculateNestingDepth(bracketElement: PsiElement): Int {
        val bracketChar = bracketElement.text[0]
        val file = bracketElement.containingFile
        val bracketOffset = bracketElement.textRange.startOffset

        var depth = 0
        val text = file.text

        // Count only curly brackets from start of file UP TO this bracket
        for (i in 0 until bracketOffset) {
            when (text[i]) {
                '{' -> depth++
                '}' -> depth--
            }
        }

        // For closing brackets, they should be at the same level as their opening bracket
        if (bracketChar == '}') {
            return maxOf(0, depth - 1)
        } else {
            return maxOf(0, depth)
        }
    }
}
