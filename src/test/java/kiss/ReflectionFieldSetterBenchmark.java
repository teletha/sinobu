/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import antibug.profiler.Benchmark;

public class ReflectionFieldSetterBenchmark {

    private static final MethodHandle staticSetter;

    static {
        try {
            staticSetter = MethodHandles.lookup().findSetter(ReflectionFieldSetterBenchmark.class, "one", String.class);
        } catch (NoSuchFieldException e) {
            throw I.quiet(e);
        } catch (IllegalAccessException e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark();
        ReflectionFieldSetterBenchmark base = new ReflectionFieldSetterBenchmark();

        WiseBiFunction h = (WiseBiFunction) AutoHandle.bypass(ReflectionFieldSetterBenchmark.class, "one", String.class);
        benchmark.measure("GeneratedM", () -> {
            try {
                return h.apply(base, "1");
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        Model mode = Model.of(ReflectionFieldSetterBenchmark.class);
        Property field = mode.property("one");
        Property method = mode.property("two");
        benchmark.measure("SinobuF", () -> {
            try {
                mode.set(base, field, "1");
                return base;
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("SinobuM", () -> {
            try {
                mode.set(base, method, "1");
                return base;
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        Field setter = ReflectionFieldSetterBenchmark.class.getDeclaredField("one");
        setter.setAccessible(true);
        benchmark.measure("Reflection", () -> {
            try {
                setter.set(base, "1");
                return base;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("StaticMH", () -> {
            try {
                staticSetter.invokeExact(base, "1");
                return base;
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                base.one = "1";
                return base;
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    public String one;

    private int two;

    public final int getTwo() {
        return two;
    }

    public final void setTwo(int two) {
        this.two = two;
    }
}