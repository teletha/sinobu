/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import antibug.profiler.Benchmark;
import kiss.I;
import kiss.WiseBiConsumer;

public class ReflectionSetterBenchmark {

    private static final MethodHandle staticSetter;

    static {
        try {
            staticSetter = MethodHandles.lookup()
                    .findVirtual(ReflectionSetterBenchmark.class, "setOne", MethodType.methodType(void.class, String.class));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark();
        ReflectionSetterBenchmark base = new ReflectionSetterBenchmark();

        Method setter = ReflectionSetterBenchmark.class.getMethod("setOne", String.class);
        benchmark.measure("Reflection", () -> {
            try {
                setter.invoke(base, "one");
                return "one";
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("StaticMH", () -> {
            try {
                staticSetter.invokeExact(base, "one");
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        WiseBiConsumer setterFunction = Model.createSetter(setter);
        benchmark.measure("LambdaMetaFactory", () -> {
            try {
                setterFunction.accept(base, "one");
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                base.setOne("one");
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    private String one = "text";

    /**
     * Get the one property of this {@link ReflectionSetterBenchmark}.
     * 
     * @return The one property.
     */
    public final String getOne() {
        return one;
    }

    /**
     * Set the one property of this {@link ReflectionSetterBenchmark}.
     * 
     * @param one The one value to set.
     */
    public final void setOne(String one) {
        this.one = one;
    }
}