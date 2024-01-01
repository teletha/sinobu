/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;

class TypeTest {

    @Test
    void primitive() {
        assert I.type("int") == int.class;
    }

    @Test
    void primitiveArray() {
        assert I.type("[I") == int[].class;
    }

    @Test
    void system() {
        assert I.type("java.lang.String") == String.class;
    }

    @Test
    void systemArray() {
        assert I.type("[Ljava.lang.String;") == String[].class;
    }

    @Test
    void invalid() {
        Assertions.assertThrows(NullPointerException.class, () -> I.type(null));
    }
}