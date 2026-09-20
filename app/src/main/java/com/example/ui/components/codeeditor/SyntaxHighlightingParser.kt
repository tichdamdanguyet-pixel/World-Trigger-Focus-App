package com.example.ui.components.codeeditor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Syntax Highlighting Parser and VisualTransformation for the Code Editor
 * Fast, single-pass lexical scanner that identifies keywords, strings, comments,
 * annotations, numbers, types, and search highlights.
 */
object SyntaxHighlightingParser {

    // Palette calibrated for dark canvas (Slate950 / Slate900)
    val ColorKeyword = Color(0xFFC678DD)       // Purple / Magenta (val, fun, class, if...)
    val ColorString = Color(0xFF98C379)        // Bright Olive Green ("...", '...')
    val ColorComment = Color(0xFF64748B)       // Slate 500 (//, /* */, <!-- -->)
    val ColorNumber = Color(0xFFD19A66)        // Warm Amber / Peach (123, 0xFF, 14.sp)
    val ColorAnnotation = Color(0xFFE5C07B)    // Gold (@Composable, @Entity)
    val ColorType = Color(0xFF61AFEF)          // Light Sky Blue (String, Int, Modifier)
    val ColorFunction = Color(0xFF67E8F9)      // Cyan 300 (method calls)
    val ColorXmlTag = Color(0xFFE06C75)        // Coral Pink (<manifest>, <string>)
    val ColorXmlAttr = Color(0xFFD19A66)       // Orange (android:name=)
    val ColorSearchBg = Color(0xFFF59E0B).copy(alpha = 0.45f) // Search match background
    val ColorSearchText = Color(0xFFFFFFFF)

    // Kotlin, Java and Gradle Keywords
    private val KOTLIN_KEYWORDS = setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "try", "catch", "finally", "throw", "this", "super", "is", "as", "in", "out",
        "typealias", "data", "sealed", "open", "override", "private", "protected",
        "public", "internal", "enum", "annotation", "companion", "by", "lazy",
        "lateinit", "const", "suspend", "inline", "crossinline", "noinline", "tailrec",
        "operator", "infix", "external", "actual", "expect", "get", "set",
        "null", "true", "false",
        // Gradle / DSL keywords
        "plugins", "dependencies", "android", "namespace", "defaultConfig", "buildTypes",
        "implementation", "ksp", "testImplementation", "androidTestImplementation",
        "kotlinOptions", "buildFeatures", "compose", "composeOptions"
    )

    // SQL / Room Query Keywords
    private val SQL_KEYWORDS = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
        "DELETE", "ORDER", "BY", "GROUP", "HAVING", "JOIN", "INNER", "LEFT",
        "RIGHT", "ON", "LIMIT", "ASC", "DESC", "AND", "OR", "NOT", "LIKE",
        "IN", "IS", "NULL", "CREATE", "TABLE", "PRIMARY", "KEY", "DISTINCT"
    )

    // Common standard library types
    private val BUILTIN_TYPES = setOf(
        "String", "Int", "Long", "Boolean", "Float", "Double", "Byte", "Short", "Char",
        "Unit", "Any", "Nothing", "List", "Map", "Set", "Array", "StateFlow",
        "MutableStateFlow", "Flow", "Modifier", "Color", "Dp", "TextRange", "TextFieldValue"
    )

    /**
     * Parses the code input text and applies syntax styles according to file extension.
     */
    fun parse(
        text: String,
        fileExtension: String = "kt",
        searchQuery: String = ""
    ): AnnotatedString {
        if (text.isEmpty()) {
            return AnnotatedString("")
        }

        val builder = AnnotatedString.Builder(text)
        val ext = fileExtension.lowercase()
        val isXml = ext == "xml"
        val isJson = ext == "json"

        var i = 0
        val len = text.length

        while (i < len) {
            // 1. XML Comments <!-- ... -->
            if (isXml && text.startsWith("<!--", i)) {
                val end = text.indexOf("-->", i + 4)
                val tokenEnd = if (end != -1) end + 3 else len
                builder.addStyle(
                    SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 2. Multi-line Block Comments /* ... */
            if (text.startsWith("/*", i)) {
                val end = text.indexOf("*/", i + 2)
                val tokenEnd = if (end != -1) end + 2 else len
                builder.addStyle(
                    SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 3. Single-line Comments // ...
            if (text.startsWith("//", i)) {
                val end = text.indexOf('\n', i + 2)
                val tokenEnd = if (end != -1) end else len
                builder.addStyle(
                    SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 4. Hash Comments # ... (properties, yml, sh, env)
            if (text[i] == '#' && (ext == "properties" || ext == "env" || ext == "sh" || ext == "yml")) {
                val end = text.indexOf('\n', i + 1)
                val tokenEnd = if (end != -1) end else len
                builder.addStyle(
                    SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 5. Triple-quoted Raw Strings """ ... """
            if (text.startsWith("\"\"\"", i)) {
                val end = text.indexOf("\"\"\"", i + 3)
                val tokenEnd = if (end != -1) end + 3 else len
                builder.addStyle(
                    SpanStyle(color = ColorString),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 6. Double-quoted Strings " ... "
            if (text[i] == '"') {
                var end = i + 1
                while (end < len) {
                    if (text[end] == '\\') {
                        end += 2 // Skip escaped character
                    } else if (text[end] == '"') {
                        end++
                        break
                    } else if (text[end] == '\n') {
                        // Unclosed string on newline
                        break
                    } else {
                        end++
                    }
                }
                val tokenEnd = end.coerceAtMost(len)
                builder.addStyle(
                    SpanStyle(color = ColorString),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 7. Single-quoted Character literals 'c'
            if (text[i] == '\'') {
                var end = i + 1
                while (end < len) {
                    if (text[end] == '\\') {
                        end += 2
                    } else if (text[end] == '\'') {
                        end++
                        break
                    } else if (text[end] == '\n') {
                        break
                    } else {
                        end++
                    }
                }
                val tokenEnd = end.coerceAtMost(len)
                builder.addStyle(
                    SpanStyle(color = ColorString),
                    i,
                    tokenEnd
                )
                i = tokenEnd
                continue
            }

            // 8. Annotations @Annotation
            if (text[i] == '@' && i + 1 < len && (text[i + 1].isLetter() || text[i + 1] == '_')) {
                var end = i + 1
                while (end < len && (text[end].isLetterOrDigit() || text[end] == '_' || text[end] == '.')) {
                    end++
                }
                builder.addStyle(
                    SpanStyle(color = ColorAnnotation, fontWeight = FontWeight.SemiBold),
                    i,
                    end
                )
                i = end
                continue
            }

            // 9. XML Tags and Attributes
            if (isXml && text[i] == '<') {
                var end = i + 1
                if (end < len && (text[end] == '/' || text[end] == '?')) {
                    end++
                }
                while (end < len && (text[end].isLetterOrDigit() || text[end] == ':' || text[end] == '-' || text[end] == '_')) {
                    end++
                }
                if (end > i + 1) {
                    builder.addStyle(
                        SpanStyle(color = ColorXmlTag, fontWeight = FontWeight.SemiBold),
                        i,
                        end
                    )
                    i = end
                    continue
                }
            }

            // 10. Numbers (Hex, Floats, Decimals)
            if (text[i].isDigit() || (text[i] == '.' && i + 1 < len && text[i + 1].isDigit())) {
                var end = i
                val isHex = text.startsWith("0x", i, ignoreCase = true)
                if (isHex) {
                    end += 2
                    while (end < len && (text[end].isDigit() || text[end] in 'a'..'f' || text[end] in 'A'..'F' || text[end] == 'L')) {
                        end++
                    }
                } else {
                    while (end < len && (text[end].isDigit() || text[end] == '.' || text[end] == 'f' || text[end] == 'F' || text[end] == 'L' || text[end] == '_')) {
                        end++
                    }
                }
                builder.addStyle(
                    SpanStyle(color = ColorNumber),
                    i,
                    end
                )
                i = end
                continue
            }

            // 11. Identifiers: Keywords, Types, Functions
            if (text[i].isLetter() || text[i] == '_') {
                var end = i + 1
                while (end < len && (text[end].isLetterOrDigit() || text[end] == '_')) {
                    end++
                }
                val word = text.substring(i, end)

                when {
                    // Keyword
                    KOTLIN_KEYWORDS.contains(word) || (isJson && (word == "true" || word == "false" || word == "null")) -> {
                        builder.addStyle(
                            SpanStyle(color = ColorKeyword, fontWeight = FontWeight.SemiBold),
                            i,
                            end
                        )
                    }
                    // SQL Keyword (uppercase check)
                    SQL_KEYWORDS.contains(word) -> {
                        builder.addStyle(
                            SpanStyle(color = ColorKeyword, fontWeight = FontWeight.SemiBold),
                            i,
                            end
                        )
                    }
                    // Built-in or Capitalized Type Name
                    BUILTIN_TYPES.contains(word) || (word[0].isUpperCase() && !isJson) -> {
                        builder.addStyle(
                            SpanStyle(color = ColorType, fontWeight = FontWeight.Medium),
                            i,
                            end
                        )
                    }
                    // Function Call (followed by '(')
                    else -> {
                        var nextIdx = end
                        while (nextIdx < len && (text[nextIdx] == ' ' || text[nextIdx] == '\t')) {
                            nextIdx++
                        }
                        if (nextIdx < len && text[nextIdx] == '(') {
                            builder.addStyle(
                                SpanStyle(color = ColorFunction),
                                i,
                                end
                            )
                        } else if (isXml && i > 0 && text[i - 1] == ' ') {
                            // XML Attribute name
                            builder.addStyle(
                                SpanStyle(color = ColorXmlAttr),
                                i,
                                end
                            )
                        }
                    }
                }

                i = end
                continue
            }

            // Advance to next character
            i++
        }

        // 12. Search Query Highlighting
        if (searchQuery.isNotBlank() && searchQuery.length <= len) {
            var searchIdx = 0
            while (searchIdx < len) {
                val matchIdx = text.indexOf(searchQuery, searchIdx, ignoreCase = true)
                if (matchIdx == -1) break
                val matchEnd = (matchIdx + searchQuery.length).coerceAtMost(len)
                builder.addStyle(
                    SpanStyle(
                        background = ColorSearchBg,
                        color = ColorSearchText,
                        fontWeight = FontWeight.Bold
                    ),
                    matchIdx,
                    matchEnd
                )
                searchIdx = matchEnd
            }
        }

        return builder.toAnnotatedString()
    }
}

/**
 * Compose VisualTransformation to render syntax highlighted code inside BasicTextField.
 * Maintains exact 1:1 character index mapping via OffsetMapping.Identity for cursor positioning.
 */
class CodeSyntaxVisualTransformation(
    private val fileExtension: String = "kt",
    private val searchQuery: String = ""
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = SyntaxHighlightingParser.parse(
            text = text.text,
            fileExtension = fileExtension,
            searchQuery = searchQuery
        )
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}
