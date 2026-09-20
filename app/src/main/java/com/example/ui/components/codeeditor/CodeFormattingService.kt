package com.example.ui.components.codeeditor

import java.util.Locale

/**
 * Service providing code formatting for Kotlin, JSON, XML, and general source files.
 * Corrects indentation, cleans excessive whitespace, and normalizes brace/tag nesting.
 */
object CodeFormattingService {

    /**
     * Formats the provided code string according to its file extension.
     */
    fun formatCode(content: String, fileExtension: String): String {
        if (content.isBlank()) return content

        return when (fileExtension.lowercase(Locale.ROOT)) {
            "kt", "kts" -> formatKotlin(content)
            "json" -> formatJson(content)
            "xml" -> formatXml(content)
            else -> formatGeneral(content)
        }
    }

    /**
     * Formats Kotlin code with consistent 4-space indentation and brace spacing.
     */
    private fun formatKotlin(content: String): String {
        val lines = content.lines()
        val formattedLines = mutableListOf<String>()
        var indentLevel = 0
        var consecutiveEmptyLines = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                consecutiveEmptyLines++
                if (consecutiveEmptyLines <= 1 && formattedLines.isNotEmpty()) {
                    formattedLines.add("")
                }
                continue
            }
            consecutiveEmptyLines = 0

            // Check if this line starts with a closing delimiter
            val leadingCloses = countLeadingClosingDelimiters(trimmed)
            val currentLineIndent = (indentLevel - leadingCloses).coerceAtLeast(0)

            val indentSpaces = " ".repeat(currentLineIndent * 4)

            // Normalize spacing before opening brace: ensure " {" instead of "{" attached to keywords
            val spacedLine = normalizeBraceSpacing(trimmed)

            formattedLines.add(indentSpaces + spacedLine)

            // Adjust indentLevel for subsequent lines
            val netDelta = calculateNetIndentationDelta(trimmed)
            indentLevel = (indentLevel + netDelta).coerceAtLeast(0)
        }

        return formattedLines.joinToString("\n").trimEnd() + "\n"
    }

    /**
     * Formats JSON content with clean 2-space indentation.
     */
    private fun formatJson(content: String): String {
        val trimmed = content.trim()
        val sb = StringBuilder()
        var indentLevel = 0
        var inString = false
        var isEscape = false

        var i = 0
        while (i < trimmed.length) {
            val c = trimmed[i]

            if (inString) {
                sb.append(c)
                if (isEscape) {
                    isEscape = false
                } else if (c == '\\') {
                    isEscape = true
                } else if (c == '"') {
                    inString = false
                }
                i++
                continue
            }

            when (c) {
                '"' -> {
                    inString = true
                    sb.append(c)
                }
                '{', '[' -> {
                    sb.append(c)
                    indentLevel++
                    sb.append("\n")
                    sb.append("  ".repeat(indentLevel))
                }
                '}', ']' -> {
                    indentLevel = (indentLevel - 1).coerceAtLeast(0)
                    sb.append("\n")
                    sb.append("  ".repeat(indentLevel))
                    sb.append(c)
                }
                ',' -> {
                    sb.append(c)
                    sb.append("\n")
                    sb.append("  ".repeat(indentLevel))
                }
                ':' -> {
                    sb.append(": ")
                }
                ' ', '\t', '\n', '\r' -> {
                    // Skip outside whitespace
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }

        val result = sb.toString().lines().joinToString("\n") { it.trimEnd() }.trim()
        return if (result.isNotEmpty()) "$result\n" else ""
    }

    /**
     * Formats XML content with 4-space indentation.
     */
    private fun formatXml(content: String): String {
        val lines = content.lines()
        val formattedLines = mutableListOf<String>()
        var indentLevel = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // Check if line closes a tag (e.g. </resources> or </string>)
            val isClosingTag = trimmed.startsWith("</")
            val isSelfClosing = trimmed.endsWith("/>")
            val isDeclaration = trimmed.startsWith("<?") || trimmed.startsWith("<!--")

            if (isClosingTag) {
                indentLevel = (indentLevel - 1).coerceAtLeast(0)
            }

            val indentSpaces = " ".repeat(indentLevel * 4)
            formattedLines.add(indentSpaces + trimmed)

            val isOpeningTag = trimmed.startsWith("<") && !isClosingTag && !isSelfClosing && !isDeclaration
            if (isOpeningTag && !trimmed.contains("</")) {
                indentLevel++
            }
        }

        return formattedLines.joinToString("\n").trimEnd() + "\n"
    }

    /**
     * General indent-based formatting for other languages.
     */
    private fun formatGeneral(content: String): String {
        val lines = content.lines()
        val formattedLines = mutableListOf<String>()
        var indentLevel = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                formattedLines.add("")
                continue
            }

            val leadingCloses = countLeadingClosingDelimiters(trimmed)
            val currentIndent = (indentLevel - leadingCloses).coerceAtLeast(0)
            formattedLines.add(" ".repeat(currentIndent * 4) + trimmed)

            val netDelta = calculateNetIndentationDelta(trimmed)
            indentLevel = (indentLevel + netDelta).coerceAtLeast(0)
        }

        return formattedLines.joinToString("\n").trimEnd() + "\n"
    }

    private fun countLeadingClosingDelimiters(line: String): Int {
        var count = 0
        for (c in line) {
            if (c == '}' || c == ']' || c == ')') {
                count++
            } else if (!c.isWhitespace()) {
                break
            }
        }
        return count
    }

    private fun calculateNetIndentationDelta(line: String): Int {
        var open = 0
        var close = 0
        var inQuotes = false
        var isEscape = false

        for (c in line) {
            if (inQuotes) {
                if (isEscape) {
                    isEscape = false
                } else if (c == '\\') {
                    isEscape = true
                } else if (c == '"') {
                    inQuotes = false
                }
                continue
            }

            when (c) {
                '"' -> inQuotes = true
                '{', '(', '[' -> open++
                '}', ')', ']' -> close++
            }
        }

        return open - close
    }

    private fun normalizeBraceSpacing(line: String): String {
        if (!line.contains("{")) return line
        // Replace something like "){\n" or "){" with ") {"
        return line.replace(Regex("""(\S)\{"""), "$1 {")
    }
}
