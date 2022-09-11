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

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionFieldBenchmark {

    private static final MethodHandle constantMH;

    static {
        try {
            constantMH = MethodHandles.lookup().findGetter(ReflectionFieldBenchmark.class, "one", String.class);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark().novisualize();
        ReflectionFieldBenchmark base = new ReflectionFieldBenchmark();

        Field field = ReflectionFieldBenchmark.class.getField("one");
        benchmark.measure("Reflection", () -> {
            try {
                return field.get(base);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        MethodHandle directMH = MethodHandles.lookup().findGetter(ReflectionFieldBenchmark.class, "one", String.class);
        benchmark.measure("MethodHandle", () -> {
            try {
                return (String) directMH.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("ConstantedMethodHandle", () -> {
            try {
                return (String) constantMH.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        ConstantCallSite callsite = new ConstantCallSite(directMH);
        MethodHandle callsited = callsite.dynamicInvoker();
        benchmark.measure("CallSitedMethodHandle", () -> {
            try {
                return (String) callsited.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                return base.one;
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    public String one = "text";
}