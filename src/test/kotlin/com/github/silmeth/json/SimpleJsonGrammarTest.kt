package com.github.silmeth.json

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SimpleJsonGrammarTest {
    @Test
    fun shouldParseLiterals() {
        assertEquals(true, SimpleJsonGrammar.parseToEnd("true"))
        assertEquals(false, SimpleJsonGrammar.parseToEnd("false"))

        assertNull(SimpleJsonGrammar.parseToEnd("null"))

        assertEquals("test string", SimpleJsonGrammar.parseToEnd("\"test string\""))
        assertEquals("Śǫmę Üniçoðe characters × 10¹⁰⁰",
                SimpleJsonGrammar.parseToEnd("\"Śǫmę Üniçoðe characters × 10¹⁰⁰\""))
        assertEquals("escaped\nstring\tliteral",
                SimpleJsonGrammar.parseToEnd("\"escaped\\nstring\\tliteral\""))

        assertEquals(1.0, SimpleJsonGrammar.parseToEnd("1"))
        assertEquals(-1.0, SimpleJsonGrammar.parseToEnd("-1"))
        assertEquals(1.0, SimpleJsonGrammar.parseToEnd("1.0"))
        assertEquals(100.0, SimpleJsonGrammar.parseToEnd("1e2"))
        assertEquals(100.0, SimpleJsonGrammar.parseToEnd("1.E2"))
        assertEquals(-13.5, SimpleJsonGrammar.parseToEnd("-1.350e1"))
    }

    @Test
    fun shouldParseSimpleObjects() {
        // empty object
        assertEquals(mapOf<String, Any?>(), SimpleJsonGrammar.parseToEnd("{}"))

        // json with a few simple fields
        val json = """
            {
              "boolField": true,
              "numField": 33.25,
              "string": "is a string"
            }
        """.trimIndent()

        val expected = mapOf("boolField" to true, "numField" to 33.25, "string" to "is a string")
        assertEquals(expected, SimpleJsonGrammar.parseToEnd(json))
    }

    @Test
    fun shouldParseArrays() {
        assertEquals(listOf(1.0, 2.0, 3.0), SimpleJsonGrammar.parseToEnd("[1, 2, 3]"))

        val json = """
            [
              [1, 2, "abc"],
              [{}, "abc"]
            ]
        """.trimIndent()

        val expected = listOf(listOf(1.0, 2.0, "abc"), listOf(mapOf<String, Any?>(), "abc"))
        assertEquals(expected, SimpleJsonGrammar.parseToEnd(json))
    }

    @Test
    fun shouldParseComplexJson() {
        val json = """
            {
              "Image": {
                "Width":  800,
                "Height": 600,
                "Title":  "View from 15th Floor",
                "Thumbnail": {
                  "Url":    "http://www.example.com/image/481989943",
                  "Height": 125,
                  "Width":  100,
                  "Visible": true
                },
                "Animated" : false,
                "IDs": [1.16E2, -943, 234, 38793]
              },
              "Escaped characters": "\n\r\"\t\\",
              "Non-escaped unicode characters" : "Ążćřǫ × 38.0e5¹²³"
            }
        """.trimIndent()

        val expected = mapOf(
                "Image" to mapOf(
                        "Width" to 800.0,
                        "Height" to 600.0,
                        "Title" to "View from 15th Floor",
                        "Thumbnail" to mapOf(
                                "Url" to "http://www.example.com/image/481989943",
                                "Height" to 125.0,
                                "Width" to 100.0,
                                "Visible" to true
                        ),
                        "Animated" to false,
                        "IDs" to listOf(116.0, -943.0, 234.0, 38793.0)
                ),
                "Escaped characters" to "\n\r\"\t\\",
                "Non-escaped unicode characters" to "Ążćřǫ × 38.0e5¹²³"
        )

        assertEquals(expected, SimpleJsonGrammar.parseToEnd(json))
    }
}
