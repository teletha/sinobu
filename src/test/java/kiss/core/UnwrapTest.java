/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;

class UnwrapTest {

    @Test
    void unwrapWrappers() {
        assert int.class == I.unwrap(Integer.class);
        assert long.class == I.unwrap(Long.class);
        assert float.class == I.unwrap(Float.class);
        assert double.class == I.unwrap(Double.class);
        assert boolean.class == I.unwrap(Boolean.class);
        assert byte.class == I.unwrap(Byte.class);
        assert short.class == I.unwrap(Short.class);
        assert char.class == I.unwrap(Character.class);
        assert void.class == I.unwrap(Void.class);
    }

    @Test
    void unwrapNonWrappers() {
        assert String.class == I.unwrap(String.class);
        assert Object.class == I.unwrap(Object.class);
        assert Enum.class == I.unwrap(Enum.class);
        assert Class.class == I.unwrap(Class.class);
    }

    @Test
    void unwrapAlreadyUnwrapped() {
        assert int.class == I.unwrap(int.class);
        assert boolean.class == I.unwrap(boolean.class);
        assert void.class == I.unwrap(void.class);
    }

    @Test
    void unwrapArrayClass() {
        assert int[].class == I.unwrap(int[].class);
        assert long[].class == I.unwrap(long[].class);
        assert boolean[].class == I.unwrap(boolean[].class);
        assert String[].class == I.unwrap(String[].class);
        assert String[][].class == I.unwrap(String[][].class);
    }

    @Test
    void unwrapNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.unwrap(null));
    }
}