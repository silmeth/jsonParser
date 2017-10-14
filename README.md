## json-parser – a simple example JSON parser in Kotlin with [better-parse](https://github.com/h0tk3y/better-parse)

The parsing code is in [SimpleJsonGrammar](src/main/kotlin/com/github/silmeth/json/SimpleJsonGrammar.kt) object,
there are also a few [tests](src/test/kotlin/com/github/silmeth/json/SimpleJsonGrammarTest.kt) showing it works.

It **does not** currently handle unicode escape sequences (like `"\uFDFD"` for `﷽` string).

Tests can be run with `./gradlew test`.
