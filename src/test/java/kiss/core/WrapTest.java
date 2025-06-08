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

class WrapTest {

    @Test
    void wrapPrimitives() {
        assert Integer.class == I.wrap(int.class);
        assert Long.class == I.wrap(long.class);
        assert Float.class == I.wrap(float.class);
        assert Double.class == I.wrap(double.class);
        assert Boolean.class == I.wrap(boolean.class);
        assert Byte.class == I.wrap(byte.class);
        assert Short.class == I.wrap(short.class);
        assert Character.class == I.wrap(char.class);
        assert Void.class == I.wrap(void.class);
        assert String.class == I.wrap(String.class);
    }

    @Test
    void wrapNonPrimitives() {
        assert String.class == I.wrap(String.class);
        assert Object.class == I.wrap(Object.class);
        assert Enum.class == I.wrap(Enum.class);
        assert Class.class == I.wrap(Class.class);
    }

    @Test
    void wrapAlreadyWrapped() {
        assert Integer.class == I.wrap(Integer.class);
        assert Boolean.class == I.wrap(Boolean.class);
        assert Void.class == I.wrap(Void.class);
    }

    @Test
    void wrapArrayClass() {
        assert int[].class == I.wrap(int[].class);
        assert long[].class == I.wrap(long[].class);
        assert boolean[].class == I.wrap(boolean[].class);
        assert String[].class == I.wrap(String[].class);
        assert String[][].class == I.wrap(String[][].class);
    }

    @Test
    void wrapNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.wrap(null));
    }
}