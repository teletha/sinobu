/*
 * Copyright (C) 2016 Nameless Production Committee
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
 * @version 2016/05/04 2:04:59
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
        assert null == I.wrap(null);
    }
}
