/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.jdk;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.junit.Test;

/**
 * @version 2011/12/15 18:21:34
 */
public class MethodHandleTest {

    @Test
    public void field() throws Throwable {
        MethodHandle setter = MethodHandles.lookup().unreflectSetter(Tihayahuru.class.getDeclaredField("name"));
        MethodHandle getter = MethodHandles.lookup().unreflectGetter(Tihayahuru.class.getDeclaredField("name"));

        Tihayahuru object = new Tihayahuru();
        assert object.name == null;

        setter.invoke(object, "test");
        assert object.name.equals("test");
        assert getter.invoke(object).equals("test");
    }

    /**
     * @version 2011/12/15 18:22:12
     */
    private static class Tihayahuru {

        public String name;
    }
}
