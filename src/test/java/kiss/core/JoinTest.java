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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;

class JoinTest {

    @Test
    void items() {
        assert I.join(" ", List.of("a", "b")).equals("a b");
    }

    @Test
    void single() {
        assert I.join(" ", List.of("a")).equals("a");
    }

    @Test
    void array() {
        assert I.join(" ", "a", "b").equals("a b");
    }

    @Test
    void nullArray() {
        assert I.join(" ", (Object[]) null).equals("");
    }

    @Test
    void nullItems() {
        I.join(null, (Iterable) null).equals("");
    }

    @Test
    void nullSeparator() {
        assert I.join(null, Arrays.asList("a", "b")).equals("ab");
    }

    @Test
    void emptyItems() {
        assert I.join("", Collections.EMPTY_LIST).equals("");
    }

    @Test
    void emptySeparator() {
        assert I.join("", Arrays.asList("a", "b")).equals("ab");
    }
}