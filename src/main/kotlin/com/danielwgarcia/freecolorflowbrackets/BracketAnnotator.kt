package com.danielwgarcia.freecolorflowbrackets

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement

class BracketAnnotator : Annotator {

    companion object {
        // 6 depth levels of nesting
        val BRACKET_LEVEL_0 = TextAttributesKey.createTextAttributesKey("BRACKET_LEVEL_0",
            DefaultLanguageHighlighterColors.BRACES)
        val BRACKET_LEVEL_1 = TextAttributesKey.createTextAttributesKey("BRACKET_LEVEL_1",
            DefaultLanguageHighlighterColors.BRACES)
        val BRACKET_LEVEL_2 = TextAttributesKey.createTextAttributesKey("BRACKET_LEVEL_2",
            DefaultLanguageHighlighterColors.BRACES)
        val BRACKET_LEVEL_3 = TextAttributesKey.createTextAttributesKey("BRACKET_LEVEL_3",
            DefaultLanguageHighlighterColors.BRACES)
        val BRACKET_LEVEL_4 = TextAttributesKey.createTextAttributesKey("BRACKET_LEVEL_4",
            DefaultLanguageHighlighterColors.BRACES)
        val BRACKET_LEVEL_5 = TextAttributesKey.createTextAttributesKey("BRACKET_LEVEL_5",
            DefaultLanguageHighlighterColors.BRACES)

        //Generic fallbacks for all levels. Update in Settings > Editor > Color Scheme > Free Bracket Colorer

        private val bracketColors = arrayOf(BRACKET_LEVEL_0, BRACKET_LEVEL_1, BRACKET_LEVEL_2, BRACKET_LEVEL_3, BRACKET_LEVEL_4, BRACKET_LEVEL_5)
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
