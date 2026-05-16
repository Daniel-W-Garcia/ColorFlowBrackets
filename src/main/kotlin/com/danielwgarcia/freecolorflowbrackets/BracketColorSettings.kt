package com.danielwgarcia.freecolorflowbrackets

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon
import com.intellij.openapi.util.IconLoader

class BracketColorSettings : ColorSettingsPage {

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return arrayOf(
            AttributesDescriptor("Level 0 - Outermost braces", BracketAnnotation.INDENTATION_LEVEL_0),
            AttributesDescriptor("Level 1 - One TAB over", BracketAnnotation.INDENTATION_LEVEL_1),
            AttributesDescriptor("Level 2 - Two TAB over", BracketAnnotation.INDENTATION_LEVEL_2),
            AttributesDescriptor("Level 3 - Three TAB over", BracketAnnotation.INDENTATION_LEVEL_3),
            AttributesDescriptor("Level 4 - Four TAB over", BracketAnnotation.INDENTATION_LEVEL_4),
            AttributesDescriptor("Level 5 - Five TAB over", BracketAnnotation.INDENTATION_LEVEL_5)
        )
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "ColorFlowBrackets"

    override fun getIcon(): Icon? {
        return IconLoader.getIcon("/META-INF/AoS.png", javaClass)
    }

    override fun getHighlighter(): SyntaxHighlighter = PlainSyntaxHighlighter()

    override fun getDemoText(): String = """
        public class DeepNestingExample
        {                                    // Level 0
            public void ProcessData()
            {                                // Level 1
                for (int i = 0; i < items.Count; i++)
                {                            // Level 2
                    if (items[i].IsValid)
                    {                        // Level 3
                        try
                        {                    // Level 4
                            using (var resource = GetResource())
                            {                // Level 5
                                // Six levels of nesting!
                                ProcessItem(items[i]);
                            }
                        }
                        catch (Exception ex)
                        {                    // Level 4
                            LogError(ex.Message);
                        }
                    }
                }
            }
        }
        
        // Each level gets a different color to track nesting depth
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
}
