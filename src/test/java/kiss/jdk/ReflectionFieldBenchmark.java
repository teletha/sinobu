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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
        Benchmark benchmark = new Benchmark().novisualize().trial(3);
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

        Function func = createGetter(MethodHandles.lookup(), field);
        benchmark.measure("LambdaMeta", () -> {
            try {
                return func.apply(base);
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

    public static <C, V> Function<C, V> createGetter(MethodHandles.Lookup lookup, Field field) throws Throwable {
        MethodType type = constantMH.type();
        final CallSite site = LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(Function.class, MethodHandle.class), type
                .generic(), MethodHandles.exactInvoker(constantMH.type()), type);
        return (Function<C, V>) site.getTarget().invokeExact(constantMH);
    }

    public static <C, V> BiConsumer<C, V> createSetter(MethodHandles.Lookup lookup, Field field) throws Throwable {
        final MethodHandle setter = lookup.unreflectSetter(field);
        MethodType type = setter.type();
        if (field.getType().isPrimitive()) type = type.wrap().changeReturnType(void.class);
        final CallSite site = LambdaMetafactory.metafactory(lookup, "accept", MethodType
                .methodType(BiConsumer.class, MethodHandle.class), type.erase(), MethodHandles.exactInvoker(setter.type()), type);
        return (BiConsumer<C, V>) site.getTarget().invokeExact(setter);
    }

    public String one = "text";
}