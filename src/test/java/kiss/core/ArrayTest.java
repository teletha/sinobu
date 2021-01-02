/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import kiss.I;

class ArrayTest {

    @Test
    void concat() {
        assertArrayEquals(new String[] {"a", "b", "c", "d", "e"}, I.array(new String[] {"a", "b", "c"}, "d", "e"));
    }

    @Test
    void baseNull() {
        assertArrayEquals(new String[] {"a", "b"}, I.array(null, "a", "b"));
    }

    @Test
    void appendNull() {
        assertArrayEquals(new String[] {"a", "b"}, I.array(new String[] {"a", "b"}, (String[]) null));
    }

    @Test
    void bothNull() {
        assertNull(I.array(null, (String[]) null));
    }
}