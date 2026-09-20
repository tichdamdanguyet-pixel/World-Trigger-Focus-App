package com.example.ui.components.codeeditor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Tactical Automatic Indentation & Code Navigation Helper
 * Detects newline insertion on 'Enter' keypress and applies smart indentation
 * based on leading whitespace, block opening braces '{', '(', '[', ':', '->',
 * and auto-expands closing brackets with indented interior cursor positioning.
 */
object AutoIndentHelper {
    const val DEFAULT_INDENT_SPACES = 4
    val INDENT_UNIT = " ".repeat(DEFAULT_INDENT_SPACES)

    /**
     * Computes the new TextFieldValue when user presses Enter, preserving
     * preceding line indentation and adding smart nested indentation for blocks.
     */
    fun computeAutoIndent(
        oldVal: TextFieldValue,
        newVal: TextFieldValue,
        indentSpaces: Int = DEFAULT_INDENT_SPACES
    ): TextFieldValue {
        val oldText = oldVal.text
        val newText = newVal.text

        val oldSelStart = oldVal.selection.min
        val oldSelEnd = oldVal.selection.max
        val oldSelLength = oldSelEnd - oldSelStart

        // Verify that a single character was inserted
        val insertedCount = newText.length - (oldText.length - oldSelLength)
        if (insertedCount != 1) {
            return newVal
        }

        // Verify that the inserted character was indeed a newline '\n'
        val cursorAfter = newVal.selection.start
        if (cursorAfter <= 0 || newText[cursorAfter - 1] != '\n') {
            return newVal
        }

        val newlineIndex = cursorAfter - 1

        // Extract the line preceding the newly inserted newline
        val textBeforeNewline = newText.substring(0, newlineIndex)
        val prevLineStartIndex = textBeforeNewline.lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
        val prevLine = textBeforeNewline.substring(prevLineStartIndex)

        // Extract base leading whitespace (spaces and tabs)
        val baseIndent = prevLine.takeWhile { it == ' ' || it == '\t' }

        val trimmedPrev = prevLine.trimEnd()
        val opensBlock = trimmedPrev.endsWith("{") ||
                         trimmedPrev.endsWith("(") ||
                         trimmedPrev.endsWith("[") ||
                         trimmedPrev.endsWith(":") ||
                         trimmedPrev.endsWith("->") ||
                         (trimmedPrev.endsWith(">") && !trimmedPrev.endsWith("/>") && !trimmedPrev.startsWith("</") && !trimmedPrev.startsWith("<!--"))

        val indentStep = " ".repeat(indentSpaces)
        val textAfterNewline = newText.substring(newlineIndex + 1)

        // Check if cursor was positioned between matching opening & closing braces
        val closesBlockDirectly = when {
            trimmedPrev.endsWith("{") && textAfterNewline.startsWith("}") -> true
            trimmedPrev.endsWith("(") && textAfterNewline.startsWith(")") -> true
            trimmedPrev.endsWith("[") && textAfterNewline.startsWith("]") -> true
            trimmedPrev.endsWith(">") && textAfterNewline.startsWith("</") -> true
            else -> false
        }

        val (insertedIndentText, newCursorPos) = when {
            closesBlockDirectly -> {
                // Expanding block:
                // {
                //     |cursor
                // }
                val innerIndent = baseIndent + indentStep
                val fullInsertion = innerIndent + "\n" + baseIndent
                Pair(fullInsertion, newlineIndex + 1 + innerIndent.length)
            }
            opensBlock -> {
                val nextIndent = baseIndent + indentStep
                Pair(nextIndent, newlineIndex + 1 + nextIndent.length)
            }
            else -> {
                Pair(baseIndent, newlineIndex + 1 + baseIndent.length)
            }
        }

        if (insertedIndentText.isEmpty() && !closesBlockDirectly) {
            return newVal
        }

        val finalString = textBeforeNewline + "\n" + insertedIndentText + textAfterNewline
        return TextFieldValue(
            text = finalString,
            selection = TextRange(newCursorPos)
        )
    }

    /**
     * Indent current cursor position or selected lines by inserting 4 spaces
     */
    fun indent(currentVal: TextFieldValue, indentSpaces: Int = DEFAULT_INDENT_SPACES): TextFieldValue {
        val indentStr = " ".repeat(indentSpaces)
        val text = currentVal.text
        val selStart = currentVal.selection.min
        val selEnd = currentVal.selection.max

        if (selStart == selEnd) {
            val newText = text.substring(0, selStart) + indentStr + text.substring(selStart)
            return TextFieldValue(newText, TextRange(selStart + indentStr.length))
        }

        val lineStart = text.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', selEnd).let { if (it == -1) text.length else it }
        val affectedSubstring = text.substring(lineStart, lineEnd)
        val indentedSubstring = affectedSubstring.split("\n").joinToString("\n") { indentStr + it }

        val newText = text.substring(0, lineStart) + indentedSubstring + text.substring(lineEnd)
        return TextFieldValue(newText, TextRange(selStart + indentStr.length, selEnd + (indentedSubstring.length - affectedSubstring.length)))
    }

    /**
     * Unindent current line(s) by removing up to 4 spaces from line beginnings
     */
    fun unindent(currentVal: TextFieldValue, indentSpaces: Int = DEFAULT_INDENT_SPACES): TextFieldValue {
        val text = currentVal.text
        val selStart = currentVal.selection.min
        val selEnd = currentVal.selection.max

        val lineStart = text.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', selEnd).let { if (it == -1) text.length else it }
        val affectedSubstring = text.substring(lineStart, lineEnd)

        val unindentedSubstring = affectedSubstring.split("\n").joinToString("\n") { line ->
            val spacesToRemove = line.take(indentSpaces).count { it == ' ' }
            val tabsToRemove = if (spacesToRemove == 0 && line.startsWith("\t")) 1 else 0
            val removedCount = spacesToRemove + tabsToRemove
            line.substring(removedCount)
        }

        val newText = text.substring(0, lineStart) + unindentedSubstring + text.substring(lineEnd)
        val newCursor = (selStart - indentSpaces).coerceAtLeast(lineStart).coerceAtLeast(0)
        return TextFieldValue(newText, TextRange(newCursor))
    }

    /**
     * Inserts surrounding pair e.g. {}, (), [], "" or symbol at cursor
     */
    fun insertPair(currentVal: TextFieldValue, open: String, close: String): TextFieldValue {
        val text = currentVal.text
        val selStart = currentVal.selection.min
        val selEnd = currentVal.selection.max

        val selectedText = text.substring(selStart, selEnd)
        val newText = text.substring(0, selStart) + open + selectedText + close + text.substring(selEnd)
        val newCursor = if (selStart == selEnd) selStart + open.length else selStart + open.length + selectedText.length + close.length
        return TextFieldValue(newText, TextRange(newCursor))
    }
}
