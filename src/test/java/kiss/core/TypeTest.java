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
    void allPrimitiveTypes() {
        assert I.type("boolean") == boolean.class;
        assert I.type("byte") == byte.class;
        assert I.type("char") == char.class;
        assert I.type("short") == short.class;
        assert I.type("int") == int.class;
        assert I.type("long") == long.class;
        assert I.type("float") == float.class;
        assert I.type("double") == double.class;
        assert I.type("void") == void.class;
    }

    @Test
    void primitiveArray() {
        assert I.type("[Z") == boolean[].class;
        assert I.type("[B") == byte[].class;
        assert I.type("[C") == char[].class;
        assert I.type("[S") == short[].class;
        assert I.type("[I") == int[].class;
        assert I.type("[J") == long[].class;
        assert I.type("[F") == float[].class;
        assert I.type("[D") == double[].class;
    }

    @Test
    void wrapperClassNames() {
        assert I.type("java.lang.Integer") == Integer.class;
        assert I.type("java.lang.Boolean") == Boolean.class;
        assert I.type("java.lang.Character") == Character.class;
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
    void userDefinedClass() {
        assert I.type("kiss.core.TypeTest") == TypeTest.class;
    }

    @Test
    void userDefinedArrayClass() {
        assert I.type("[Lkiss.core.TypeTest;") == TypeTest[].class;
    }

    @Test
    void multiDimensionalArray() {
        assert I.type("[[I") == int[][].class;
        assert I.type("[[Ljava.lang.String;") == String[][].class;
    }

    @Test
    void invalid() {
        Assertions.assertThrows(NullPointerException.class, () -> I.type(null));
    }
}