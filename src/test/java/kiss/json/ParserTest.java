/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;
import kiss.I;

/**
 * @version 2018/03/31 22:51:43
 */
public class ParserTest {

    @Test
    public void empty() {
        parse("{}");
        parse("{ }");
        parse("{\t}");
        parse("{\r}");
        parse("{\n}");
        parse(" { } ");
    }

    @Test
    public void space() {
        // @formatter:off
        parse("  {  ",
        "   ' s p a c e '  :   null    ",
        "  }  ");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidEndBrace() {
        parse("{");
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidStartBrace() {
        parse("}");
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidNoSeparator() {
        // @formatter:off
        parse("{",
        "  'true': true",
        "  'false': false",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidTailSeparator() {
        // @formatter:off
        parse("{",
        "  'true': true,",
        "  'false': false,",
        "}");
        // @formatter:on
    }

    @Test
    public void primitives() {
        // @formatter:off
        parse("{",
        "  'true': true,",
        "  'false': false,",
        "  'null': null",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidPrimitives() {
        // @formatter:off
        parse("{",
        "  'name': undefined",
        "}");
        // @formatter:on 
    }

    @Test
    public void array() {
        // @formatter:off
        parse("{",
        "  'value': ['a', true, false, null, 1, -1, 0.2, 3e+1],",
        "  'space': [' ',''   ,\t'with tab'],",
        "  'empty': []",
        "}");
        // @formatter:on
    }

    @Test
    public void object() {
        // @formatter:off
        parse("{",
        "  'value': {'a': 1, 'b': false, 'c': null},",
        "  'space': { ' ' : ' ' },",
        "  'empty': {}",
        "}");
        // @formatter:on
    }

    @Test
    public void string() {
        // @formatter:off
        parse("{",
        "  'name': 'value',",
        "  'escepe': '\\b \\f \\n \\r \\t \\\" \\\\ \\/',",
        "  'unicode': '\\u004Do\\u006E',",
        "  'non-ascii': 'あいうえお',",
        "  'empty': ''",
        "}");
        // @formatter:on
    }

    @Test
    public void escapedQuote1() {
        // @formatter:off
        parse("{",
        "  'valid': '\\\"'", // \"
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void escapedQuote2() {
        // @formatter:off
        parse("{",
        "  'invalid': '\\\\\"'", // \\"
        "}");
        // @formatter:on
    }

    @Test
    public void escapedQuote3() {
        // @formatter:off
        parse("{",
        "  'valid': '\\\\\\\"'", // \\\"
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void escapedQuote4() {
        // @formatter:off
        parse("{",
        "  'invalid': '\\\\\\\\\"'", // \\\\"
        "}");
        // @formatter:on
    }

    @Test
    public void escapedQuote5() {
        // @formatter:off
        parse("{",
        "  'valid': '\\\\\\\\\\\"'", // \\\\\"
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void escapedQuote6() {
        // @formatter:off
        parse("{",
        "  'invalid': '\\\\\\\\\\\\\"'", // \\\\\\"
        "}");
        // @formatter:on
    }

    @Test
    public void number() {
        // @formatter:off
        parse("{",
        "  '0': 0,",
        "  '1': 1,",
        "  '2': 2,",
        "  '3': 3,",
        "  '4': 4,",
        "  '5': 5,",
        "  '6': 6,",
        "  '7': 7,",
        "  '8': 8,",
        "  '9': 9,",
        "  'minus': -12345,",
        "  'minusZero': -0,",
        "  'fraction': 0.12345,",
        "  'exponetSmall': 1.2e+3,",
        "  'exponetLarge': 1.2E+3,",
        "  'exponetMinus': 1.2e-3,",
        "  'exponetNone': 1.2e3",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidNoQuotedName() {
        // @formatter:off
        parse("{",
        "  invalid: 'name'",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidNaN() {
        // @formatter:off
        parse("{",
        "  'NaN': NaN",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidPlus() {
        // @formatter:off
        parse("{",
        "  'plus': +1",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidZeroPrefix() {
        // @formatter:off
        parse("{",
        "  'zeroPrefix': 012",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidMinusZeroPrefix() {
        // @formatter:off
        parse("{",
        "  'zeroPrefix': -012",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidFraction() {
        // @formatter:off
        parse("{",
        "  'fraction-abbr': .1",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidExponent() {
        // @formatter:off
        parse("{",
        "  'invalid': 1e*1",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidMinusOnly() {
        // @formatter:off
        parse("{",
        "  'minus': -",
        "}");
        // @formatter:on
    }

    @ExpectThrow(IllegalStateException.class)
    public void invalidMinus() {
        // @formatter:off
        parse("{",
        "  'minus': -a",
        "}");
        // @formatter:on
    }

    @ExpectThrow(NumberFormatException.class)
    public void invalidUnicode1() {
        // @formatter:off
        parse("{",
        "  'invalid': '\\u000'",
        "}");
        // @formatter:on
    }

    @ExpectThrow(NumberFormatException.class)
    public void invalidUnicode2() {
        // @formatter:off
        parse("{",
        "  'invalid': '\\u000G'",
        "}");
        // @formatter:on
    }

    @ExpectThrow(NumberFormatException.class)
    public void invalidUnicode3() {
        // @formatter:off
        parse("{",
        "  'invalid': '\\u000-'",
        "}");
        // @formatter:on
    }

    private void parse(String... texts) {
        StringJoiner joiner = new StringJoiner("\r\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }

        try {
            Constructor<?> c = Class.forName("kiss.JSON").getDeclaredConstructor(Reader.class);
            c.setAccessible(true);
            c.newInstance(new StringReader(joiner.toString()));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
