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

import org.junit.jupiter.api.Test;

import kiss.I;

class ParseLargeTextTest {

    private static int size = 10000;

    @Test
    void string() {
        I.json("""
                {
                    "key": "%s"
                }
                """.formatted("text".repeat(size)));
    }

    @Test
    void key() {
        I.json("""
                {
                    "%s": "value"
                }
                """.formatted("text".repeat(size)));
    }

    @Test
    void whitespace() {
        I.json("""
                { %s
                    "key": "value"
                }
                """.formatted(" ".repeat(size)));
    }
}
