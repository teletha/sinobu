/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/09/13 10:29:24
 */
public class WrapTest {

    @Test
    public void wrapPrimitives() {
        assert Integer.class == I.wrap(int.class);
        assert Long.class == I.wrap(long.class);
        assert Float.class == I.wrap(float.class);
        assert Double.class == I.wrap(double.class);
        assert Boolean.class == I.wrap(boolean.class);
        assert Byte.class == I.wrap(byte.class);
        assert Short.class == I.wrap(short.class);
        assert Character.class == I.wrap(char.class);
        assert String.class == I.wrap(String.class);
    }

    @Test
    public void wrapClass() {
        assert String.class == I.wrap(String.class);
    }

    @Test
    public void wrapNull() {
        assert Object.class == I.wrap(null);
    }
}
