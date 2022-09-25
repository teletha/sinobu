/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

class MethodHandleTest {

    @Test
    void field() throws Throwable {
        MethodHandle setter = MethodHandles.lookup().unreflectSetter(Naming.class.getDeclaredField("name"));
        MethodHandle getter = MethodHandles.lookup().unreflectGetter(Naming.class.getDeclaredField("name"));

        Naming object = new Naming();
        assert object.name == null;

        setter.invoke(object, "test");
        assert object.name.equals("test");
        assert getter.invoke(object).equals("test");
    }

    @Test
    void zeroInt() throws Throwable {
        MethodHandle mh = MethodHandles.zero(int.class);
        int value = (int) mh.invokeExact();
        assert value == 0;
    }

    @Test
    void zeroBoolean() throws Throwable {
        MethodHandle mh = MethodHandles.zero(boolean.class);
        boolean value = (boolean) mh.invokeExact();
        assert value == false;
    }

    @Test
    void zeroFloat() throws Throwable {
        MethodHandle mh = MethodHandles.zero(float.class);
        float value = (float) mh.invokeExact();
        assert value == 0f;
    }

    @Test
    void zeroFloatAsObject() throws Throwable {
        MethodHandle mh = MethodHandles.zero(float.class).asType(MethodType.methodType(Object.class));
        Object value = mh.invokeExact();
        assert ((float) value) == 0f;
    }

    private static class Naming {

        public String name;
    }
}