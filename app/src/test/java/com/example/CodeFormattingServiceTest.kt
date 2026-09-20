package com.example

import com.example.ui.components.codeeditor.CodeFormattingService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeFormattingServiceTest {

    @Test
    fun testKotlinFormattingIndentsCorrectly() {
        val input = """
            fun calculatePower() {
            val base = 10
            if (base > 5) {
            val boost = 20
            }
            }
        """.trimIndent()

        val formatted = CodeFormattingService.formatCode(input, "kt")

        val lines = formatted.split("\n")
        assertEquals("fun calculatePower() {", lines[0])
        assertEquals("    val base = 10", lines[1])
        assertEquals("    if (base > 5) {", lines[2])
        assertEquals("        val boost = 20", lines[3])
        assertEquals("    }", lines[4])
        assertEquals("}", lines[5])
    }

    @Test
    fun testJsonFormatting() {
        val input = """
            {
            "id": 101,
            "name": "Kuga Yuma",
            "active": true
            }
        """.trimIndent()

        val formatted = CodeFormattingService.formatCode(input, "json")
        val lines = formatted.split("\n")
        assertEquals("{", lines[0])
        assertEquals("  \"id\": 101,", lines[1])
        assertEquals("  \"name\": \"Kuga Yuma\",", lines[2])
        assertEquals("  \"active\": true", lines[3])
        assertEquals("}", lines[4])
    }

    @Test
    fun testXmlFormatting() {
        val input = """
            <resources>
            <string name="title">Border HQ</string>
            </resources>
        """.trimIndent()

        val formatted = CodeFormattingService.formatCode(input, "xml")
        val lines = formatted.split("\n")
        assertEquals("<resources>", lines[0])
        assertEquals("    <string name=\"title\">Border HQ</string>", lines[1])
        assertEquals("</resources>", lines[2])
    }
}
