/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.Test;

import kiss.I;

/**
 * @version 2017/03/05 11:18:14
 */
public class TypeTest {

    @Test
    public void primitive() {
        assert I.type("int") == int.class;
    }

    @Test
    public void primitiveArray() {
        assert I.type("[I") == int[].class;
    }

    @Test
    public void system() {
        assert I.type("java.lang.String") == String.class;
    }

    @Test
    public void systemArray() {
        assert I.type("[Ljava.lang.String;") == String[].class;
    }
}
