/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.StringJoiner;

import org.junit.Test;

import kiss.I;

/**
 * @version 2017/04/20 12:31:27
 */
public class ParserTest {

    @Test
    public void empty() throws Exception {
        parse("{}");
        parse("{ }");
        parse("{\t}");
        parse("{\r}");
        parse("{\n}");
        parse(" { } ");
    }

    @Test
    public void space() throws Exception {
        // @formatter:off
        parse("  {  ",
        "   ' s p a c e '  :   null    ",
        "  }  ");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidEndBrace() throws Exception {
        parse("{");
    }

    @Test(expected = IllegalStateException.class)
    public void invalidStartBrace() throws Exception {
        parse("}");
    }

    @Test(expected = IllegalStateException.class)
    public void invalidNoSeparator() throws Exception {
        // @formatter:off
        parse("{",
        "  'true': true",
        "  'false': false",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidTailSeparator() throws Exception {
        // @formatter:off
        parse("{",
        "  'true': true,",
        "  'false': false,",
        "}");
        // @formatter:on
    }

    @Test
    public void primitives() throws Exception {
        // @formatter:off
        parse("{",
        "  'true': true,",
        "  'false': false,",
        "  'null': null",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidPrimitives() throws Exception {
        // @formatter:off
        parse("{",
        "  'name': undefined",
        "}");
        // @formatter:on 
    }

    @Test
    public void array() throws Exception {
        // @formatter:off
        parse("{",
        "  'value': ['a', true, false, null, 1, -1, 0.2, 3e+1],",
        "  'space': [' ',''   ,\t'with tab'],",
        "  'empty': []",
        "}");
        // @formatter:on
    }

    @Test
    public void object() throws Exception {
        // @formatter:off
        parse("{",
        "  'value': {'a': 1, 'b': false, 'c': null},",
        "  'space': { ' ' : ' ' },",
        "  'empty': {}",
        "}");
        // @formatter:on
    }

    @Test
    public void string() throws Exception {
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
    public void escapedQuote1() throws Exception {
        // @formatter:off
        parse("{",
        "  'valid': '\\\"'", // \"
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void escapedQuote2() throws Exception {
        // @formatter:off
        parse("{",
        "  'invalid': '\\\\\"'", // \\"
        "}");
        // @formatter:on
    }

    @Test
    public void escapedQuote3() throws Exception {
        // @formatter:off
        parse("{",
        "  'valid': '\\\\\\\"'", // \\\"
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void escapedQuote4() throws Exception {
        // @formatter:off
        parse("{",
        "  'invalid': '\\\\\\\\\"'", // \\\\"
        "}");
        // @formatter:on
    }

    @Test
    public void escapedQuote5() throws Exception {
        // @formatter:off
        parse("{",
        "  'valid': '\\\\\\\\\\\"'", // \\\\\"
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void escapedQuote6() throws Exception {
        // @formatter:off
        parse("{",
        "  'invalid': '\\\\\\\\\\\\\"'", // \\\\\\"
        "}");
        // @formatter:on
    }

    @Test
    public void number() throws Exception {
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

    @Test(expected = IllegalStateException.class)
    public void invalidNaN() throws Exception {
        // @formatter:off
        parse("{",
        "  'NaN': NaN",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidPlus() throws Exception {
        // @formatter:off
        parse("{",
        "  'plus': +1",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidZeroPrefix() throws Exception {
        // @formatter:off
        parse("{",
        "  'zeroPrefix': 012",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidMinusZeroPrefix() throws Exception {
        // @formatter:off
        parse("{",
        "  'zeroPrefix': -012",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidFraction() throws Exception {
        // @formatter:off
        parse("{",
        "  'fraction-abbr': .1",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidExponent() throws Exception {
        // @formatter:off
        parse("{",
        "  'invalid': 1e*1",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidMinusOnly() throws Exception {
        // @formatter:off
        parse("{",
        "  'minus': -",
        "}");
        // @formatter:on
    }

    @Test(expected = IllegalStateException.class)
    public void invalidMinus() throws Exception {
        // @formatter:off
        parse("{",
        "  'minus': -a",
        "}");
        // @formatter:on
    }

    @Test(expected = NumberFormatException.class)
    public void invalidUnicode1() throws Exception {
        // @formatter:off
        parse("{",
        "  'invalid': '\\u000'",
        "}");
        // @formatter:on
    }

    @Test(expected = NumberFormatException.class)
    public void invalidUnicode2() throws Exception {
        // @formatter:off
        parse("{",
        "  'invalid': '\\u000G'",
        "}");
        // @formatter:on
    }

    @Test(expected = NumberFormatException.class)
    public void invalidUnicode3() throws Exception {
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
