/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import kiss.I;

class ParserTest {

    @Test
    void empty() {
        parse("{}");
    }

    @Test
    void emptySpace() {
        parse("{ }");
    }

    @Test
    void emptySpaceOutside() {
        parse(" { } ");
    }

    @Test
    void emptyTab() {
        parse("{\t}");
    }

    @Test
    void emptyCarrigeReturn() {
        parse("{\r}");
    }

    @Test
    void emptyLineFeed() {
        parse("{\n}");
    }

    @Test
    void none() {
        parse("");
    }

    @Test
    void noneSpace() {
        parse(" ");
    }

    @Test
    void noneTab() {
        parse("\t");
    }

    @Test
    void keyWithSpace() {
        parse("""
                {
                    " s p a c e " : null
                }
                """);
    }

    @Test
    void invalidEndBrace() {
        assertThrows(IllegalStateException.class, () -> {
            parse("{");
        });
    }

    @Test
    void invalidStartBrace() {
        assertThrows(IllegalStateException.class, () -> {
            parse("}");
        });
    }

    @Test
    void invalidNoSeparator() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "true": true
                        "false": false
                    }
                    """);
        });
    }

    @Test
    void invalidTailSeparator() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "true": true,
                        "false": false,
                    }
                    """);
        });
    }

    @Test
    void primitiveTrue() {
        parse("""
                {
                    "name": true
                }
                """);
    }

    @Test
    void primitiveFalse() {
        parse("""
                {
                    "name": false
                }
                """);
    }

    @Test
    void primitiveNull() {
        parse("""
                {
                    "name": null
                }
                """);
    }

    @Test
    void primitiveNullSpace() {
        parse("""
                {
                    "name": null
                }
                """);
    }

    @Test
    void primitiveNullTab() {
        parse("""
                {
                    "name": null\t
                }
                """);
    }

    @Test
    void primitiveNullNoSpace() {
        parse("""
                {"name":null}
                """);
    }

    @Test
    void invalidPrimitives() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "name": undefined
                    }
                    """);
        });
    }

    @Test
    void invalidPrimitiveName1() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "name": nulll
                    }
                    """);
        });
    }

    @Test
    void array1() {
        parse("""
                {
                    "name" : ["string", true, false, null, 1, -1, 0.2, 3e+1]
                }
                """);
    }

    @Test
    void array2() {
        parse("""
                {
                    "name" : [""," ",\t"withtab"]
                }
                """);
    }

    @Test
    void array3() {
        parse("""
                {
                    "empty" : []
                }
                """);
    }

    @Test
    void object() {
        parse("""
                {
                    "value": {"a": 1, "b": false, "c": null},
                    "space": { " " : " " },
                    "empty": {}
                }
                """);
    }

    @Test
    void string() {
        parse("""
                {
                  "name": "the value",
                  "escepe": "\\b \\f \\n \\r \\t \\\" \\\\ \\/",
                  "unicode": "\\u004Do\\u006E",
                  "non-ascii": "あいうえお",
                  "empty": "",
                  "blank": "    "
                }
                """);
    }

    @Test
    void escapedQuote1() {
        // \"
        parse("""
                {
                    "valid": "\\\""
                }
                """);
    }

    @Test
    void escapedQuote2() {
        // \\"
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "valid": "\\\\\""
                    }
                    """);
        });
    }

    @Test
    void escapedQuote3() {
        // \\\"
        parse("""
                {
                    "valid": "\\\\\\\""
                }
                """);
    }

    @Test
    void escapedQuote4() {
        // \\\\"
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "valid": "\\\\\\\\\""
                    }
                    """);
        });
    }

    @Test
    void escapedQuote5() {
        // \\\\\"
        parse("""
                {
                    "valid": "\\\\\\\\\\\""
                }
                """);
    }

    @Test
    void escapedQuote6() {
        // \\\\\\"
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "valid": "\\\\\\\\\\\\\""
                    }
                    """);
        });
    }

    @Test
    void number() {
        parse("""
                {
                    "0": 0,
                    "1": 1,
                    "2": 2,
                    "3": 3,
                    "4": 4,
                    "5": 5,
                    "6": 6,
                    "7": 7,
                    "8": 8,
                    "9": 9,
                    "minus": -12345,
                    "minusZero": -0,
                    "fraction": 0.12345,
                    "exponetSmall": 1.2e+3,
                    "exponetLarge": 1.2E+3,
                    "exponetMinus": 1.2e-3,
                    "exponetNone": 1.2e3
                }
                """);
    }

    @Test
    void invalidNoQuotedName() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        invalid: "name"
                    }
                    """);
        });
    }

    @Test
    void invalidNaN() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "NaN": NaN
                    }
                    """);
        });
    }

    @Test
    void invalidPlus() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "plus": +1
                    }
                    """);
        });
    }

    @Test
    void invalidZeroPrefix() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "zeroPrefix": 012
                    }
                    """);
        });
    }

    @Test
    void invalidMinusZeroPrefix() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "zeroPrefix": -012
                    }
                    """);
        });
    }

    @Test
    void invalidFraction() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "fraction-abbr": .1
                    }
                    """);
        });
    }

    @Test
    void invalidExponent() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "invalid": 1e*1
                    }
                    """);

        });
    }

    @Test
    void invalidMinusOnly() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "minus": -
                    }
                    """);

        });
    }

    @Test
    void invalidMinus() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "minus": -a
                    }
                    """);
        });
    }

    @Test
    void invalidUnicode1() {
        assertThrows(NumberFormatException.class, () -> {
            parse("""
                    {
                        "invalid": "\\u000"
                    }
                    """);

        });
    }

    @Test
    void invalidUnicode2() {
        assertThrows(NumberFormatException.class, () -> {
            parse("""
                    {
                        "invalid": "\\u000G"
                    }
                    """);

        });
    }

    @Test
    void invalidUnicode3() {
        assertThrows(NumberFormatException.class, () -> {
            parse("""
                    {
                        "invalid": "\\u000-"
                    }
                    """);

        });
    }

    /**
     * Try to paser json.
     * 
     * @param text
     */
    private void parse(String text) {
        parse(text, "Normal");
        parse(text.replaceAll("[ ]", "    "), "Multi Spaces");
        parse(text.replaceAll("[ \\t\\r\\n]", ""), "No Whitespace");
    }

    /**
     * Try to parse json.
     * 
     * @param json
     * @param type
     */
    private void parse(String json, String type) {
        try {
            if (json.length() == 0) {
                return;
            }

            I.json(json);
        } catch (Throwable e) {
            e.addSuppressed(new IllegalArgumentException("Fail to parse json :" + type + "\r\n" + json));

            throw I.quiet(e);
        }
    }
}