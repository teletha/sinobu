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
import java.lang.reflect.Method;

import antibug.profiler.Benchmark;
import antibug.profiler.Inspection;

public class ReflectionGetterBenchmark {

    private static final MethodHandle StaticHandle;

    static {
        try {
            StaticHandle = MethodHandles.publicLookup().unreflect(Target.class.getMethod("getter"));
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark().visualize(Inspection.CallPerTime);
        Target base = new Target();

        Method method = Target.class.getMethod("getter");
        benchmark.measure("Reflection", () -> {
            try {
                return method.invoke(base);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        MethodHandle handle = MethodHandles.publicLookup().unreflect(method);
        benchmark.measure("MH", () -> {
            try {
                return handle.invoke(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("MH#invokeExact", () -> {
            try {
                return (String) handle.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("Static MH", () -> {
            try {
                return StaticHandle.invoke(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("Static MH#invokeExact", () -> {
            try {
                return (String) StaticHandle.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        WiseFunction function = Model.createGetter(method);
        benchmark.measure("LambdaMetaFactory", () -> {
            try {
                return function.apply(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                return base.getter();
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    public static class Target {
        public String getter() {
            return "text";
        }
    }
}