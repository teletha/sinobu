/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.lang.reflect.Array;

import org.junit.jupiter.api.Test;

class GettingPrimitiveDefaultValueTest {

    @Test
    void defaultValue() {
        assert value(int.class) == 0;
        assert value(long.class) == 0L;
        assert value(float.class) == 0F;
        assert value(double.class) == 0D;
        assert value(byte.class) == (byte) 0;
        assert value(short.class) == (short) 0;
        assert value(boolean.class) == false;
        assert value(char.class) == '\0';
    }

    private static <T> T value(Class<T> type) {
        return (T) Array.get(Array.newInstance(type, 1), 0);
    }
}