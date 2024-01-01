/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import kiss.I;
import kiss.JSON;

@Execution(ExecutionMode.SAME_THREAD)
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
    void onlySpace() {
        parse(" ");
    }

    @Test
    void onlyTab() {
        parse("\t");
    }

    @Test
    void onlyNum() {
        parse("15");
    }

    @Test
    void onlyString() {
        parse("\"text\"");
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
        JSON json = parse("""
                {
                    "key": true
                }
                """);

        assert json.get(boolean.class, "key") == true;
    }

    @Test
    void primitiveFalse() {
        JSON json = parse("""
                {
                    "key": false
                }
                """);

        assert json.get(boolean.class, "key") == false;
    }

    @Test
    void primitivePositiveInt() {
        JSON json = parse("""
                {
                    "key": 10
                }
                """);

        assert json.get(int.class, "key") == 10;
    }

    @Test
    void primitiveNegativeInt() {
        JSON json = parse("""
                {
                    "key": -1
                }
                """);

        assert json.get(int.class, "key") == -1;
    }

    @Test
    void primitivePositiveDecimal() {
        JSON json = parse("""
                {
                    "key": 0.123
                }
                """);

        assert json.get(float.class, "key") == 0.123f;
    }

    @Test
    void primitiveNegativeDecimal() {
        JSON json = parse("""
                {
                    "key": -50.2
                }
                """);

        assert json.get(float.class, "key") == -50.2f;
    }

    @Test
    void primitiveNegativeDecimalWithZeroStart() {
        JSON json = parse("""
                {
                    "key": -0.02
                }
                """);

        assert json.get(float.class, "key") == -0.02f;
    }

    @Test
    void primitivePositiveExponetial() {
        JSON json = parse("""
                {
                    "key": 3e-1
                }
                """);

        assert json.get(float.class, "key") == 0.3f;
    }

    @Test
    void primitiveNegativeExponetial() {
        JSON json = parse("""
                {
                    "key": -3e-1
                }
                """);

        assert json.get(float.class, "key") == -0.3f;
    }

    @Test
    void primitivePositiveExponetialPlus() {
        JSON json = parse("""
                {
                    "key": 3e+1
                }
                """);

        assert json.get(float.class, "key") == 30f;
    }

    @Test
    void primitiveNegativeExponetialPlus() {
        JSON json = parse("""
                {
                    "key": -3e+1
                }
                """);

        assert json.get(float.class, "key") == -30f;
    }

    @Test
    void primitiveZero() {
        JSON json = parse("""
                {
                    "key": 0
                }
                """);

        assert json.get(float.class, "key") == 0f;
    }

    @Test
    void primitiveNull() {
        JSON json = parse("""
                {
                    "key": null
                }
                """);

        assert json.get("key") == null;
    }

    @Test
    void primitiveNullSpace() {
        JSON json = parse("""
                {
                    "key": null\s
                }
                """);

        assert json.get("key") == null;
    }

    @Test
    void primitiveNullTab() {
        JSON json = parse("""
                {
                    "key": null\t
                }
                """);

        assert json.get("key") == null;
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
    void invalidKeywordNull() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "invlid": nullll
                    }
                    """);
        });
    }

    @Test
    void invalidKeywordTrue() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "invlid": TRUE
                    }
                    """);
        });
    }

    @Test
    void invalidKeywordFalse() {
        assertThrows(IllegalStateException.class, () -> {
            parse("""
                    {
                        "invlid": falsee
                    }
                    """);
        });
    }

    @Test
    void array1() {
        JSON json = parse("""
                {
                    "name" : ["string", true, false, null, 1, -1, 0.2, 3e+1]
                }
                """);

        assert json.get("name").get(String.class, "0").equals("string");
        assert json.get("name").get(boolean.class, "1") == true;
        assert json.get("name").get(boolean.class, "2") == false;
        assert json.get("name").get(int.class, "4") == 1;
        assert json.get("name").get(int.class, "5") == -1;
        assert json.get("name").get(float.class, "6") == 0.2f;
        assert json.get("name").get(float.class, "7") == 30f;
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
    private JSON parse(String text) {
        parse(text.replaceAll("[ ]", "    "), "Multi Spaces");
        parse(text.replaceAll("[ \\t\\r\\n]", ""), "No Whitespace");
        return parse(text, "Normal");
    }

    /**
     * Try to parse json.
     * 
     * @param text
     * @param type
     */
    private JSON parse(String text, String type) {
        try {
            if (text.length() == 0) {
                return null;
            }

            return I.json(text);
        } catch (Throwable e) {
            e.addSuppressed(new IllegalArgumentException("Fail to parse json :" + type + "\r\n" + text));

            throw I.quiet(e);
        }
    }
}