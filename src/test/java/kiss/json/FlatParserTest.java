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

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kiss.I;

public class FlatParserTest {

    @Test
    void empty() {
        Map parsed = parse("""
                {}
                """);
        assert parsed.isEmpty();
    }

    @Test
    void object() {
        Map parsed = parse("""
                {
                    "key": 1
                }
                """);

        assert parsed.get("key").equals("1");
    }

    @Test
    void objectMulti() {
        Map parsed = parse("""
                {
                    "key1": 1,
                    "key2": 2
                }
                """);

        assert parsed.get("key1").equals("1");
        assert parsed.get("key2").equals("2");
    }

    @Test
    void string() {
        Map parsed = parse("""
                {
                    "key": "text"
                }
                """);

        assert parsed.get("key").equals("text");
    }

    @Test
    void primitiveTrue() {
        Map parsed = parse("""
                {
                    "key": true
                }
                """);

        assert parsed.get("key").equals(true);
    }

    @Test
    void primitiveFalse() {
        Map parsed = parse("""
                {
                    "key": false
                }
                """);

        assert parsed.get("key").equals(false);
    }

    private Map parse(String json) {
        try {
            FlatParser parser = new FlatParser(json);
            return parser.root;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}