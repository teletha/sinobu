/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import kiss.I;
import kiss.json.JSONMappingBenchmark.Person;

public class Builder {

    public static final MethodHandle GET_NAME = createGetter(Person.class, "name", String.class);

    /**
     * @return
     */
    public static MethodHandle createGetter(Class target, String name, Class type) {
        return null;
    }

    /**
     * @return
     */
    public static MethodHandle fieldSetter(Class target, String name, Class type) {
        try {
            return MethodHandles.privateLookupIn(target, MethodHandles.lookup()).findSetter(target, name, type);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * @return
     */
    public static VarHandle vhSetter(Class target, String name, Class type) {
        try {
            return MethodHandles.privateLookupIn(target, MethodHandles.lookup()).findVarHandle(target, name, type);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }
}
