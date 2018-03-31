/*
 * Copyright (C) 2018 Nameless Production Committee
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

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2016/10/12 16:47:07
 */
public class JoinTest {

    @Test
    public void items() throws Exception {
        assert I.join(" ", Arrays.asList("a", "b")).equals("a b");
    }

    @Test
    public void single() throws Exception {
        assert I.join(" ", Arrays.asList("a")).equals("a");
    }

    @Test
    public void nullItems() throws Exception {
        I.join(null, (Iterable) null).equals("");
    }

    @Test
    public void nullSeparator() throws Exception {
        assert I.join(null, Arrays.asList("a", "b")).equals("ab");
    }

    @Test
    public void emptyItems() throws Exception {
        assert I.join("", Collections.EMPTY_LIST).equals("");
    }

    @Test
    public void emptySeparator() throws Exception {
        assert I.join("", Arrays.asList("a", "b")).equals("ab");
    }
}
