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
import java.util.function.Function;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionMethodBenchmark {

    private static final MethodHandle constantMH;

    static {
        try {
            constantMH = MethodHandles.lookup().findVirtual(ReflectionMethodBenchmark.class, "one", MethodType.methodType(String.class));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark().novisualize();
        ReflectionMethodBenchmark base = new ReflectionMethodBenchmark();

        Method method = ReflectionMethodBenchmark.class.getMethod("one");
        benchmark.measure("Reflection", () -> {
            try {
                return method.invoke(base);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        MethodHandle directMH = MethodHandles.lookup()
                .findVirtual(ReflectionMethodBenchmark.class, "one", MethodType.methodType(String.class));
        benchmark.measure("MH", () -> {
            try {
                return (String) directMH.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("ConstantMH", () -> {
            try {
                return (String) constantMH.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        Function function = Model.create(method, true);
        benchmark.measure("LambdaMetaFactory", () -> {
            try {
                return function.apply(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                return base.one();
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    public String one() {
        return "text";
    }
}