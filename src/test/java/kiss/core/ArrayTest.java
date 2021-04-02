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
     * @see I#bundle(Class, java.util.Collection)
     * @see I#wiseBC(Runnable)
     * @see I#write(Object, Appendable)
     */
    @Test
    void concat() {
        assert Arrays.equals(new String[] {"a", "b", "c", "d", "e"}, I.array(new String[] {"a", "b", "c"}, "d", "e"));
    }

    /**
     * @see I#array(Object[], Object...)
     */
    @Test
    void baseNull() {
        assert Arrays.equals(new String[] {"a", "b"}, I.array(null, "a", "b"));
    }

    /**
     * @see I#array(Object[], Object...)
     */
    @Test
    void appendNull() {
        assert Arrays.equals(new String[] {"a", "b"}, I.array(new String[] {"a", "b"}, (String[]) null));
    }

    @Test
    void bothNull() {
        assert I.array(null, (String[]) null) == null;
    }
}