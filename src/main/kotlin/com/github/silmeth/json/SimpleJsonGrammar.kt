package com.github.silmeth.json

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separated
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser

object SimpleJsonGrammar : Grammar<Any?>() {
    // the regex "[^\\"]*(\\["nrtbf\\][^\\"]*)*" matches:
    // "               – opening double quote,
    // [^\\"]*         – any number of not escaped characters, nor double quotes
    // (
    //   \\["nrtbf\\]  – backslash followed by special character (\", \n, \r, \\, etc.)
    //   [^\\"]*       – and any number of non-special characters
    // )*              – repeating as a group any number of times
    // "               – closing double quote
    private val stringLiteral by token("\"[^\\\\\"]*(\\\\[\"nrtbf\\\\][^\\\\\"]*)*\"")

    private val whiteSpace by token("\\s+", ignore = true)

    // Punctuation and parentheses
    private val comma by token(",")
    private val colon by token(":")
    private val openingBrace by token("\\{")
    private val closingBrace by token("\\}")
    private val openingBracket by token("\\[")
    private val closingBracket by token("\\]")

    // Keywords
    private val nullToken by token("\\bnull\\b")
    private val trueToken by token("\\btrue\\b")
    private val falseToken by token("\\bfalse\\b")

    // Signs used by numbers
    private val integer by token("\\d+")
    private val dot by token("\\.")
    private val exponent by token("[eE]")
    private val minus by token("-")

    // Json literal values
    private val jsonNull: Parser<Any?> = nullToken asJust null
    private val jsonBool: Parser<Boolean> = (trueToken asJust true) or (falseToken asJust false)
    private val string: Parser<String> = (stringLiteral use { text.substring(1, text.lastIndex) })
            .map { it.replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\b", "\b")
                    .replace("\\f", "\u000C")
                    .replace("\\\\", "\\") }
    // Json number literals
    private val exponentPart = -exponent and integer
    private val floatingPointPart = -dot and optional(integer)
    private val onlyFloatingPart = -dot and integer
    private val positiveNumber: Parser<Double> = ((integer and optional(floatingPointPart))
            .map { (int, floatPart) ->
                int.text + (floatPart?.let { ".${it.text}" } ?: "")
            } or
            (onlyFloatingPart map { ".${it.text}" }) and
            optional(exponentPart map { "e${it.text}" }))
            .map { (p1, p2) ->
                (p1 + (p2 ?: "")).toDouble()
            }

    private val number: Parser<Double> = (optional(minus) and positiveNumber)
            .map { (m, num) -> if (m != null) -num else num }

    private val jsonPrimitiveValue: Parser<Any?> = jsonNull or jsonBool or string or number
    private val jsonObject: Parser<Map<String, Any?>> = (-openingBrace and
            separated(string and -colon and parser(this::jsonValue), comma, true) and
            -closingBrace)
            .map {
                it.terms.map { (key, v) -> Pair(key, v) }.toMap()
            }
    private val jsonArray: Parser<List<Any?>> = (-openingBracket and
            separated(parser(this::jsonValue), comma, true) and
            -closingBracket)
            .map { it.terms }
    private val jsonValue: Parser<Any?> = jsonPrimitiveValue or jsonObject or jsonArray
    override val rootParser = jsonValue
}
