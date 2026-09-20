package com.example.ui.components.codeeditor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Diagnostic Severity Level
 */
enum class DiagnosticSeverity {
    ERROR,   // Critical syntax problem (unbalanced braces, unclosed string, syntax error) - Red wavy line
    WARNING, // Logic issue (empty block, assignment in condition, unhandled exception, unreachable code) - Amber wavy line
    INFO     // Optimization or style suggestion - Cyan wavy line
}

/**
 * Detailed code diagnostic item representing a syntax error or logic issue
 * with precise character offsets for wavy underline rendering and gutter indicators.
 */
data class CodeDiagnostic(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val severity: DiagnosticSeverity,
    val line: Int,         // 1-based line number
    val column: Int,       // 1-based column number
    val startOffset: Int,  // 0-based character index in editor text
    val endOffset: Int,    // 0-based character index end in editor text
    val errorSnippet: String = "",
    val rule: String = ""
)

/**
 * Background Service & Static Parser for Real-Time Code Analysis
 * Continuously parses code input to detect syntax discrepancies and logic issues.
 */
object CodeDiagnosticsService {

    /**
     * Suspend function to run lexical and structural analysis on background thread (Dispatchers.Default).
     */
    suspend fun analyzeAsync(text: String, fileExtension: String = "kt"): List<CodeDiagnostic> =
        withContext(Dispatchers.Default) {
            analyze(text, fileExtension)
        }

    /**
     * Synchronous lexical analysis engine.
     */
    fun analyze(text: String, fileExtension: String = "kt"): List<CodeDiagnostic> {
        if (text.isEmpty()) return emptyList()

        val diagnostics = mutableListOf<CodeDiagnostic>()
        val ext = fileExtension.lowercase()

        // 1. Bracket, Parenthesis and Brace Matching
        diagnostics.addAll(checkDelimiters(text))

        // 2. String and Character Literal Checks
        diagnostics.addAll(checkStringLiterals(text, ext))

        // 3. Block Comments Unclosed
        diagnostics.addAll(checkBlockComments(text, ext))

        // 4. Incomplete Declarations & Dangling Operators
        diagnostics.addAll(checkIncompleteStatements(text))

        // 5. Logic Issues (Assignment in condition, Empty catch blocks, Unreachable code)
        diagnostics.addAll(checkLogicIssues(text))

        // 6. JSON Specific Validation
        if (ext == "json") {
            diagnostics.addAll(checkJsonSyntax(text))
        }

        // 7. XML Specific Validation
        if (ext == "xml") {
            diagnostics.addAll(checkXmlSyntax(text))
        }

        // Return sorted by line, column
        return diagnostics.sortedWith(compareBy({ it.line }, { it.column }))
    }

    /**
     * Helper to compute 1-based line and column for a character offset.
     */
    fun getLineAndCol(text: String, offset: Int): Pair<Int, Int> {
        val safeOffset = offset.coerceIn(0, text.length)
        val textBefore = text.take(safeOffset)
        val line = textBefore.count { it == '\n' } + 1
        val lastNewline = textBefore.lastIndexOf('\n')
        val col = if (lastNewline >= 0) safeOffset - lastNewline else safeOffset + 1
        return Pair(line, col)
    }

    /**
     * Checks balanced delimiters { }, ( ), [ ] ignoring content inside strings and comments.
     */
    private fun checkDelimiters(text: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()
        data class Delim(val char: Char, val offset: Int, val line: Int, val col: Int)

        val stack = ArrayDeque<Delim>()
        var i = 0
        val len = text.length

        var currentLine = 1
        var lineStart = 0

        while (i < len) {
            val c = text[i]

            // Track line numbers
            if (c == '\n') {
                currentLine++
                lineStart = i + 1
                i++
                continue
            }

            // Skip single-line comments
            if (c == '/' && i + 1 < len && text[i + 1] == '/') {
                val nextNewline = text.indexOf('\n', i + 2)
                i = if (nextNewline != -1) nextNewline else len
                continue
            }

            // Skip multi-line comments
            if (c == '/' && i + 1 < len && text[i + 1] == '*') {
                val endComment = text.indexOf("*/", i + 2)
                if (endComment != -1) {
                    val countNl = text.substring(i, endComment + 2).count { it == '\n' }
                    currentLine += countNl
                    val lastNlInComment = text.substring(i, endComment + 2).lastIndexOf('\n')
                    if (lastNlInComment != -1) {
                        lineStart = i + lastNlInComment + 1
                    }
                    i = endComment + 2
                } else {
                    i = len
                }
                continue
            }

            // Skip raw strings """..."""
            if (text.startsWith("\"\"\"", i)) {
                val endRaw = text.indexOf("\"\"\"", i + 3)
                if (endRaw != -1) {
                    val countNl = text.substring(i, endRaw + 3).count { it == '\n' }
                    currentLine += countNl
                    val lastNl = text.substring(i, endRaw + 3).lastIndexOf('\n')
                    if (lastNl != -1) {
                        lineStart = i + lastNl + 1
                    }
                    i = endRaw + 3
                } else {
                    i = len
                }
                continue
            }

            // Skip normal strings "..."
            if (c == '"') {
                var s = i + 1
                while (s < len && text[s] != '"' && text[s] != '\n') {
                    if (text[s] == '\\' && s + 1 < len) s += 2 else s++
                }
                i = if (s < len && text[s] == '"') s + 1 else s
                continue
            }

            // Skip char literals '...'
            if (c == '\'') {
                var s = i + 1
                while (s < len && text[s] != '\'' && text[s] != '\n') {
                    if (text[s] == '\\' && s + 1 < len) s += 2 else s++
                }
                i = if (s < len && text[s] == '\'') s + 1 else s
                continue
            }

            val col = i - lineStart + 1

            // Opening delimiters
            if (c == '{' || c == '(' || c == '[') {
                stack.addLast(Delim(c, i, currentLine, col))
            } else if (c == '}' || c == ')' || c == ']') {
                if (stack.isEmpty()) {
                    diagnostics.add(
                        CodeDiagnostic(
                            message = "Dấu đóng '$c' thừa: Không tìm thấy dấu mở tương ứng.",
                            severity = DiagnosticSeverity.ERROR,
                            line = currentLine,
                            column = col,
                            startOffset = i,
                            endOffset = i + 1,
                            errorSnippet = c.toString(),
                            rule = "SYNTAX_UNEXPECTED_CLOSING_DELIM"
                        )
                    )
                } else {
                    val top = stack.removeLast()
                    val expectedOpen = when (c) {
                        '}' -> '{'
                        ')' -> '('
                        ']' -> '['
                        else -> ' '
                    }
                    if (top.char != expectedOpen) {
                        diagnostics.add(
                            CodeDiagnostic(
                                message = "Cặp dấu không khớp: Mong đợi dấu đóng cho '${top.char}' tại dòng ${top.line} nhưng gặp '$c'.",
                                severity = DiagnosticSeverity.ERROR,
                                line = currentLine,
                                column = col,
                                startOffset = i,
                                endOffset = i + 1,
                                errorSnippet = c.toString(),
                                rule = "SYNTAX_MISMATCHED_DELIM"
                            )
                        )
                    }
                }
            }

            i++
        }

        // Remaining unclosed openings
        while (stack.isNotEmpty()) {
            val unclosed = stack.removeLast()
            val (delimName, expectedClose) = when (unclosed.char) {
                '{' -> Pair("ngoặc nhọn '{ }'", '}')
                '(' -> Pair("ngoặc tròn '( )'", ')')
                '[' -> Pair("ngoặc vuông '[ ]'", ']')
                else -> Pair("'${unclosed.char}'", ' ')
            }
            diagnostics.add(
                CodeDiagnostic(
                    message = "Chưa đóng dấu $delimName: Thiếu dấu đóng '$expectedClose' tương ứng.",
                    severity = DiagnosticSeverity.ERROR,
                    line = unclosed.line,
                    column = unclosed.col,
                    startOffset = unclosed.offset,
                    endOffset = (unclosed.offset + 1).coerceAtMost(len),
                    errorSnippet = unclosed.char.toString(),
                    rule = "SYNTAX_UNCLOSED_DELIM"
                )
            )
        }

        return diagnostics
    }

    /**
     * Checks unclosed string literals on each line.
     */
    private fun checkStringLiterals(text: String, ext: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()
        val lines = text.split("\n")
        var currentOffset = 0

        lines.forEachIndexed { lineIdx, line ->
            val lineNum = lineIdx + 1
            var i = 0
            val len = line.length

            while (i < len) {
                // Ignore comments
                if (line.startsWith("//", i)) break

                // Triple quotes can span multiple lines - checked separately
                if (line.startsWith("\"\"\"", i)) {
                    i += 3
                    continue
                }

                if (line[i] == '"') {
                    val stringStart = i
                    var s = i + 1
                    var closed = false
                    while (s < len) {
                        if (line[s] == '\\' && s + 1 < len) {
                            s += 2
                        } else if (line[s] == '"') {
                            closed = true
                            s++
                            break
                        } else {
                            s++
                        }
                    }

                    if (!closed) {
                        val startOff = currentOffset + stringStart
                        val endOff = currentOffset + len
                        diagnostics.add(
                            CodeDiagnostic(
                                message = "Chuỗi ký tự chưa đóng: Thiếu dấu nháy kép '\"' ở cuối dòng.",
                                severity = DiagnosticSeverity.ERROR,
                                line = lineNum,
                                column = stringStart + 1,
                                startOffset = startOff,
                                endOffset = endOff,
                                errorSnippet = line.substring(stringStart),
                                rule = "SYNTAX_UNCLOSED_STRING"
                            )
                        )
                    }
                    i = s
                    continue
                }

                // Check single quotes (char literals) in Kotlin/Java
                if (line[i] == '\'' && ext != "xml") {
                    val charStart = i
                    var s = i + 1
                    var closed = false
                    while (s < len) {
                        if (line[s] == '\\' && s + 1 < len) {
                            s += 2
                        } else if (line[s] == '\'') {
                            closed = true
                            s++
                            break
                        } else {
                            s++
                        }
                    }

                    if (!closed) {
                        val startOff = currentOffset + charStart
                        val endOff = currentOffset + len
                        diagnostics.add(
                            CodeDiagnostic(
                                message = "Ký tự literal chưa đóng: Thiếu dấu nháy đơn '\''.",
                                severity = DiagnosticSeverity.ERROR,
                                line = lineNum,
                                column = charStart + 1,
                                startOffset = startOff,
                                endOffset = endOff,
                                errorSnippet = line.substring(charStart),
                                rule = "SYNTAX_UNCLOSED_CHAR"
                            )
                        )
                    }
                    i = s
                    continue
                }

                i++
            }

            currentOffset += line.length + 1 // +1 for \n
        }

        // Check unclosed triple-quotes
        var rawIdx = 0
        while (rawIdx < text.length) {
            val start = text.indexOf("\"\"\"", rawIdx)
            if (start == -1) break
            val end = text.indexOf("\"\"\"", start + 3)
            if (end == -1) {
                val (line, col) = getLineAndCol(text, start)
                diagnostics.add(
                    CodeDiagnostic(
                        message = "Chuỗi nhiều dòng chưa đóng: Thiếu '\"\"\"' để đóng khối chuỗi.",
                        severity = DiagnosticSeverity.ERROR,
                        line = line,
                        column = col,
                        startOffset = start,
                        endOffset = text.length,
                        errorSnippet = "\"\"\"",
                        rule = "SYNTAX_UNCLOSED_MULTILINE_STRING"
                    )
                )
                break
            }
            rawIdx = end + 3
        }

        return diagnostics
    }

    /**
     * Checks unclosed block comments /* ... */ and XML comments <!-- ... -->
     */
    private fun checkBlockComments(text: String, ext: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()

        var i = 0
        while (i < text.length) {
            val start = text.indexOf("/*", i)
            if (start == -1) break
            val end = text.indexOf("*/", start + 2)
            if (end == -1) {
                val (line, col) = getLineAndCol(text, start)
                diagnostics.add(
                    CodeDiagnostic(
                        message = "Khối chú thích chưa đóng: Thiếu '*/' để kết thúc bình luận khối.",
                        severity = DiagnosticSeverity.ERROR,
                        line = line,
                        column = col,
                        startOffset = start,
                        endOffset = (start + 2).coerceAtMost(text.length),
                        errorSnippet = "/*",
                        rule = "SYNTAX_UNCLOSED_BLOCK_COMMENT"
                    )
                )
                break
            }
            i = end + 2
        }

        if (ext == "xml") {
            var xmlCommentIdx = 0
            while (xmlCommentIdx < text.length) {
                val start = text.indexOf("<!--", xmlCommentIdx)
                if (start == -1) break
                val end = text.indexOf("-->", start + 4)
                if (end == -1) {
                    val (line, col) = getLineAndCol(text, start)
                    diagnostics.add(
                        CodeDiagnostic(
                            message = "Chú thích XML chưa đóng: Thiếu '-->' để kết thúc.",
                            severity = DiagnosticSeverity.ERROR,
                            line = line,
                            column = col,
                            startOffset = start,
                            endOffset = (start + 4).coerceAtMost(text.length),
                            errorSnippet = "<!--",
                            rule = "SYNTAX_UNCLOSED_XML_COMMENT"
                        )
                    )
                    break
                }
                xmlCommentIdx = end + 3
            }
        }

        return diagnostics
    }

    /**
     * Checks incomplete declarations (val =, var =, fun ()) and dangling operators (+, -, &&, ||)
     */
    private fun checkIncompleteStatements(text: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()
        val lines = text.split("\n")
        var currentOffset = 0

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            val lineNum = index + 1

            // Incomplete val / var / fun declaration
            if (trimmed.startsWith("val ") || trimmed.startsWith("var ")) {
                val rest = trimmed.substring(4).trim()
                if (rest.isEmpty() || rest.startsWith("=")) {
                    val startOff = currentOffset + line.indexOf(if (trimmed.startsWith("val")) "val" else "var")
                    diagnostics.add(
                        CodeDiagnostic(
                            message = "Khai báo biến chưa hoàn chỉnh: Thiếu tên định danh biến sau '${trimmed.take(3)}'.",
                            severity = DiagnosticSeverity.ERROR,
                            line = lineNum,
                            column = 1,
                            startOffset = startOff,
                            endOffset = startOff + trimmed.length,
                            errorSnippet = trimmed,
                            rule = "SYNTAX_MISSING_VARIABLE_NAME"
                        )
                    )
                }
            }

            // Incomplete function declaration: fun ()
            if (trimmed.startsWith("fun ")) {
                val rest = trimmed.substring(4).trim()
                if (rest.startsWith("(") || rest.isEmpty()) {
                    val startOff = currentOffset + line.indexOf("fun")
                    diagnostics.add(
                        CodeDiagnostic(
                            message = "Khai báo hàm chưa hoàn chỉnh: Thiếu tên hàm sau 'fun'.",
                            severity = DiagnosticSeverity.ERROR,
                            line = lineNum,
                            column = 1,
                            startOffset = startOff,
                            endOffset = startOff + trimmed.length,
                            errorSnippet = trimmed,
                            rule = "SYNTAX_MISSING_FUNCTION_NAME"
                        )
                    )
                }
            }

            // Dangling binary operators at end of line (e.g. "val x = 10 +")
            val danglingOperators = listOf(" +", " -", " *", " /", " &&", " ||", " ==")
            danglingOperators.forEach { op ->
                if (trimmed.endsWith(op) && !trimmed.startsWith("//")) {
                    val nextLine = lines.getOrNull(index + 1)?.trim() ?: ""
                    // If next line is empty or a closing bracket, it's dangling
                    if (nextLine.isEmpty() || nextLine.startsWith("}") || nextLine.startsWith(")")) {
                        val opIndex = line.lastIndexOf(op.trim())
                        if (opIndex != -1) {
                            val startOff = currentOffset + opIndex
                            diagnostics.add(
                                CodeDiagnostic(
                                    message = "Toán tử dở dang '${op.trim()}': Thiếu toán hạng bên phải biểu thức.",
                                    severity = DiagnosticSeverity.ERROR,
                                    line = lineNum,
                                    column = opIndex + 1,
                                    startOffset = startOff,
                                    endOffset = startOff + op.trim().length,
                                    errorSnippet = op.trim(),
                                    rule = "SYNTAX_DANGLING_OPERATOR"
                                )
                            )
                        }
                    }
                }
            }

            currentOffset += line.length + 1
        }

        return diagnostics
    }

    /**
     * Checks common logic bugs (assignment in conditions, empty catch blocks, empty if/while bodies, dead code).
     */
    private fun checkLogicIssues(text: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()

        // 1. Logic Issue: Assignment in Condition `if (x = y)` instead of `==`
        val ifConditionRegex = Regex("""\bif\s*\(([^)]+)\)""")
        ifConditionRegex.findAll(text).forEach { match ->
            val conditionContent = match.groupValues[1]
            // If condition contains '=' but not '==', '!=', '<=', '>='
            if (conditionContent.contains("=") &&
                !conditionContent.contains("==") &&
                !conditionContent.contains("!=") &&
                !conditionContent.contains("<=") &&
                !conditionContent.contains(">=")) {
                val eqOffset = match.range.first + match.value.indexOf('=')
                val (line, col) = getLineAndCol(text, eqOffset)
                diagnostics.add(
                    CodeDiagnostic(
                        message = "Lỗi logic tiềm ẩn: Sử dụng phép gán '=' trong điều kiện; bạn có định dùng phép so sánh '==' không?",
                        severity = DiagnosticSeverity.WARNING,
                        line = line,
                        column = col,
                        startOffset = eqOffset,
                        endOffset = eqOffset + 1,
                        errorSnippet = match.value,
                        rule = "LOGIC_ASSIGNMENT_IN_CONDITION"
                    )
                )
            }
        }

        // 2. Logic Issue: Empty Catch Block `catch (...) {}`
        val catchRegex = Regex("""\bcatch\s*\([^)]+\)\s*\{\s*\}""")
        catchRegex.findAll(text).forEach { match ->
            val (line, col) = getLineAndCol(text, match.range.first)
            diagnostics.add(
                CodeDiagnostic(
                    message = "Vấn đề logic: Khối catch rỗng bỏ qua ngoại lệ một cách âm thầm mà không xử lý.",
                    severity = DiagnosticSeverity.WARNING,
                    line = line,
                    column = col,
                    startOffset = match.range.first,
                    endOffset = match.range.last + 1,
                    errorSnippet = match.value,
                    rule = "LOGIC_EMPTY_CATCH_BLOCK"
                )
            )
        }

        // 3. Logic Issue: Empty if or loop blocks `if (...) {}`
        val emptyIfRegex = Regex("""\b(if|while|for)\s*\([^)]+\)\s*\{\s*\}""")
        emptyIfRegex.findAll(text).forEach { match ->
            val (line, col) = getLineAndCol(text, match.range.first)
            val keyword = match.groupValues[1]
            diagnostics.add(
                CodeDiagnostic(
                    message = "Vấn đề logic: Khối '${keyword}' rỗng không có câu lệnh nào được thực thi.",
                    severity = DiagnosticSeverity.WARNING,
                    line = line,
                    column = col,
                    startOffset = match.range.first,
                    endOffset = match.range.last + 1,
                    errorSnippet = match.value,
                    rule = "LOGIC_EMPTY_CONTROL_BLOCK"
                )
            )
        }

        // 4. Logic Issue: Unreachable / Dead Code after return or throw
        val lines = text.split("\n")
        var currentOffset = 0
        var insideBlockAfterReturn = false
        var returnLine = 0

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            val lineNum = index + 1

            if (trimmed.startsWith("return ") || trimmed == "return" || trimmed.startsWith("throw ")) {
                insideBlockAfterReturn = true
                returnLine = lineNum
            } else if (insideBlockAfterReturn) {
                if (trimmed == "}" || trimmed.endsWith("}")) {
                    insideBlockAfterReturn = false
                } else if (trimmed.isNotEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("/*")) {
                    val startOff = currentOffset + line.indexOf(trimmed)
                    diagnostics.add(
                        CodeDiagnostic(
                            message = "Vấn đề logic: Mã lệnh không thể tiếp cận (unreachable code) sau câu lệnh 'return' tại dòng $returnLine.",
                            severity = DiagnosticSeverity.WARNING,
                            line = lineNum,
                            column = 1,
                            startOffset = startOff,
                            endOffset = startOff + trimmed.length,
                            errorSnippet = trimmed,
                            rule = "LOGIC_UNREACHABLE_CODE"
                        )
                    )
                    insideBlockAfterReturn = false // only report first unreachable statement
                }
            }

            currentOffset += line.length + 1
        }

        return diagnostics
    }

    /**
     * Checks JSON specific syntax issues (trailing commas, unquoted keys).
     */
    private fun checkJsonSyntax(text: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()

        // Trailing comma before closing } or ]
        val trailingCommaRegex = Regex(""",\s*([\}\]])""")
        trailingCommaRegex.findAll(text).forEach { match ->
            val commaOffset = match.range.first
            val (line, col) = getLineAndCol(text, commaOffset)
            diagnostics.add(
                CodeDiagnostic(
                    message = "Cú pháp JSON không hợp lệ: Không được có dấu phẩy thừa trước '${match.groupValues[1]}'.",
                    severity = DiagnosticSeverity.ERROR,
                    line = line,
                    column = col,
                    startOffset = commaOffset,
                    endOffset = commaOffset + 1,
                    errorSnippet = ",",
                    rule = "JSON_TRAILING_COMMA"
                )
            )
        }

        return diagnostics
    }

    /**
     * Checks basic XML tag structure.
     */
    private fun checkXmlSyntax(text: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()

        // Malformed opening tag lacking closing >
        val unclosedTagRegex = Regex("""<([a-zA-Z0-9_\-\:]+)[^>]*\n""")
        unclosedTagRegex.findAll(text).forEach { match ->
            if (!match.value.contains(">") && !match.value.startsWith("<!--")) {
                val (line, col) = getLineAndCol(text, match.range.first)
                diagnostics.add(
                    CodeDiagnostic(
                        message = "Thẻ XML chưa đóng dấu '>': Thẻ '<${match.groupValues[1]}' bị ngắt dòng trước khi đóng.",
                        severity = DiagnosticSeverity.ERROR,
                        line = line,
                        column = col,
                        startOffset = match.range.first,
                        endOffset = match.range.first + match.value.trim().length,
                        errorSnippet = match.value.trim(),
                        rule = "XML_UNCLOSED_TAG"
                    )
                )
            }
        }

        return diagnostics
    }
}
