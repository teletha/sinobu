/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.Arrays;
import java.util.Collections;

import kiss.I;

import org.junit.Test;

/**
 * @version 2013/09/27 15:02:27
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
        assert I.join(null, Arrays.asList("a", "b")).equals("anullb");
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
