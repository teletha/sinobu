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

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionFieldSetterBenchmark {

    private static final MethodHandle staticSetter = Builder.fieldSetter(ReflectionFieldSetterBenchmark.class, "one", int.class);

    private static final VarHandle staticVH = Builder.vhSetter(ReflectionFieldSetterBenchmark.class, "one", int.class);

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark();
        ReflectionFieldSetterBenchmark base = new ReflectionFieldSetterBenchmark();

        Field setter = ReflectionFieldSetterBenchmark.class.getDeclaredField("one");
        setter.setAccessible(true);
        benchmark.measure("Reflection", () -> {
            try {
                setter.set(base, 1);
                return "one";
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("StaticMH", () -> {
            try {
                staticSetter.invokeExact(base, 1);
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        MethodHandle mh = MethodHandles.lookup().findSetter(ReflectionFieldSetterBenchmark.class, "one", int.class);
        benchmark.measure("MH", () -> {
            try {
                mh.invokeExact(base, 1);
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("StaticVH", () -> {
            try {
                staticVH.set(base, 1);
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        BiIntConsumer lambda = createSetter(setter);
        benchmark.measure("LambdaMeta", () -> {
            try {
                lambda.accept(base, 1);
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                base.one = 1;
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    private int one = 0;

    public interface BiIntConsumer<T> {
        void accept(T one, int two);
    }

    static BiIntConsumer createSetter(Field field) throws Throwable {
        Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
        MethodHandle mh = lookup.unreflectSetter(field);
        mh = lookup.findSetter(field.getDeclaringClass(), field.getName(), field.getType());

        return (BiIntConsumer) LambdaMetafactory.metafactory(lookup, "accept", MethodType.methodType(BiIntConsumer.class), mh.type()
                .generic()
                .changeReturnType(void.class), mh, mh.type().wrap().changeReturnType(void.class)).dynamicInvoker().invokeExact();
    }
}