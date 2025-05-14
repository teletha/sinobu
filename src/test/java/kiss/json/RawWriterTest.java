/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;

class RawWriterTest {

    private void validate(String json, Consumer<String>... validators) {
        JSON original = I.json(json);
        String encoded = original.toString();
        JSON decoded = I.json(encoded);
        assert encoded.equals(decoded.toString());

        for (Consumer<String> validator : validators) {
            validator.accept(encoded);
        }
    }

    private Consumer<String> requireArray(int count) {
        return json -> {
            assert json.chars().filter(c -> c == '[').count() == count;
            assert json.chars().filter(c -> c == ']').count() == count;
        };
    }

    private Consumer<String> requireNull(int count) {
        return json -> {
            assert count(json, "[^\"]null[^\"]") == count;
        };
    }

    private int count(String input, String p) {
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(input);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    @Test
    void string() {
        validate("""
                {
                    "name": "Takina"
                }
                """);
    }

    @Test
    void num() {
        validate("""
                {
                    "num": 1
                }
                """);
    }

    @Test
    void minusNum() {
        validate("""
                {
                    "num": -1
                }
                """);
    }

    @Test
    void bool() {
        validate("""
                {
                    "bool": true
                }
                """);
    }

    @Test
    void nullValue() {
        validate("""
                {
                    "value": null
                }
                """, requireNull(1));
    }

    @Test
    void nullString() {
        validate("""
                {
                    "value": "null"
                }
                """, requireNull(0));
    }

    @Test
    void nestedObject() {
        validate("""
                {
                    "character": {
                        "name": "Chisato",
                        "age": 17
                    }
                }
                """);
    }

    @Test
    void arrayOfNumbers() {
        validate("""
                {
                    "numbers": [1, 2, 3, 4]
                }
                """, requireArray(1));
    }

    @Test
    void arrayOfObjects() {
        validate("""
                {
                    "members": [
                        {"name": "Chisato"},
                        {"name": "Takina"}
                    ]
                }
                """, requireArray(1));
    }

    @Test
    void emptyObject() {
        validate("""
                {
                    "empty": {}
                }
                """);
    }

    @Test
    void emptyArray() {
        validate("""
                {
                    "list": []
                }
                """, requireArray(1));
    }

    @Test
    void escapedCharacters() {
        validate("""
                {
                    "text": "Line1\\nLine2\\tTabbed\\\\"
                }
                """);
    }

    @Test
    void unicodeCharacters() {
        validate("""
                {
                    "message": "こんにちは世界"
                }
                """);
    }

    @Test
    void deeplyNestedObjects() {
        validate("""
                {
                    "a": {
                        "b": {
                            "c": {
                                "d": {
                                    "e": "deep"
                                }
                            }
                        }
                    }
                }
                """);
    }

    @Test
    void mixedArray() {
        validate("""
                {
                    "mixed": [1, "two", true, null, {"key": "value"}]
                }
                """, requireArray(1));
    }

    @Test
    void keysWithSpecialCharacters() {
        validate("""
                {
                    "spaced key": "value",
                    "key.with.dots": "value",
                    "key-with-dash": "value",
                    "key_with_underscore": "value",
                    "キー": "値"
                }
                """);
    }

    @Test
    void numericEdgeCases() {
        validate("""
                {
                    "intMax": 2147483647,
                    "intMin": -2147483648,
                    "longMax": 9223372036854775807,
                    "longMin": -9223372036854775808
                }
                """);
    }

    @Test
    void floatingPointNumbers() {
        validate("""
                {
                    "pi": 3.14159,
                    "negativeFloat": -0.123,
                    "scientific": 1.23e4,
                    "small": 5e-6
                }
                """);
    }

    @Test
    void booleanEdgeCases() {
        validate("""
                {
                    "trueValue": true,
                    "falseValue": false
                }
                """);
    }

    @Test
    void objectWithAllTypes() {
        validate("""
                {
                    "string": "text",
                    "number": 42,
                    "boolean": false,
                    "null": null,
                    "object": { "key": "value" },
                    "array": [1, 2, 3]
                }
                """, requireArray(1));
    }

    @Test
    void emptyJsonObject() {
        validate("{}");
    }

    @Test
    void rootIsArray() {
        validate("""
                [1, "text", true, {"key": "value"}, null, [10, 20]]
                """, requireArray(2));
    }

    @Test
    void rootIsArrayOfObjects() {
        validate("""
                [
                    {"id": 1, "name": "Alice", "tags": ["user", "active"]},
                    {"id": 2, "name": "Bob", "tags": ["user", "inactive"]}
                ]
                """, requireArray(3));
    }

    @Test
    void rootIsEmptyArray() {
        validate("[]", requireArray(1));
    }

    @Test
    void numberZeroVariants() {
        // JSON specification allows -0 as a number.
        // How it's serialized (e.g., as "0", "0.0", or "-0.0") depends on the library.
        // The test checks for consistency in serialization and deserialization.
        validate("""
                {
                    "integerZero": 0,
                    "floatZero": 0.0,
                    "negativeFloatZero": -0.0
                }
                """);
    }

    @Test
    void moreEscapedCharactersInString() {
        // Existing test covers \n, \t, \\
        // Adding \", \b, \f, \r, and \/ (escaped solidus, though solidus doesn't strictly need
        // escaping)
        validate("""
                {
                    "text": "Quotes: \\"Hello\\". Backspace: \\b. Form feed: \\f. Carriage return: \\r. Solidus: a\\/b."
                }
                """);
    }

    @Test
    void emptyStringKeyInObject() {
        validate("""
                {
                    "": "Value for empty string key"
                }
                """);
    }

    @Test
    void unicodeEscapeSequencesInString() {
        validate("""
                {
                    "latinCapitalA": "\\u0041",
                    "hiraganaA": "\\u3042",
                    "mixed": "Text with \\u0041 and あ"
                }
                """);
    }

    @Test
    void largeIntegerNumber() {
        // This number likely exceeds standard `long` type, testing for `BigDecimal`-like handling
        // if available.
        // The key is that the number is preserved exactly through serialization/deserialization.
        validate("""
                {
                    "veryLargeInteger": 123456789012345678901234567890,
                    "veryLargeNegativeInteger": -123456789012345678901234567890
                }
                """);
    }

    @Test
    void floatingPointNumberVariations() {
        // Test different exponent notations and numbers that might be normalized.
        // e.g., 123.0 might be serialized as 123. 0.000000789 might become 7.89e-7.
        // The test ensures consistency.
        validate("""
                {
                    "positiveExponentExplicitPlus": 1.23e+5,
                    "capitalExponent": 2.34E6,
                    "negativeCapitalExponent": 3.45E-7,
                    "integerLikeFloat": 123.0,
                    "smallDecimal": 0.000000789,
                    "largeDecimal": 7890000000.0
                }
                """);
    }
}
