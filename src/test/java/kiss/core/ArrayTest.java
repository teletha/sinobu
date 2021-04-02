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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import kiss.I;

class ArrayTest {

    /**
     * @see I#array(Object[], Object...)
     */
    @Test
    void concatTwoArrays() {
        String[] head = {"a", "b", "c"};
        String[] tail = {"d", "e"};

        assert Arrays.equals(I.array(head, tail), new String[] {"a", "b", "c", "d", "e"});
    }

    @Test
    void firstNull() {
        assert Arrays.equals(I.array(null, "a", "b"), new String[] {"a", "b"});
    }

    @Test
    void secondNull() {
        assert Arrays.equals(I.array(new String[] {"a", "b"}, (String[]) null), new String[] {"a", "b"});
    }

    @Test
    void bothNull() {
        assert I.array(null, (String[]) null) == null;
    }
}