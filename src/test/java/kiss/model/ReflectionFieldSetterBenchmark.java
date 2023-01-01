/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionFieldSetterBenchmark {

    private static final MethodHandle staticSetter;

    static {
        try {
            staticSetter = MethodHandles.lookup().findSetter(ReflectionFieldSetterBenchmark.class, "one", int.class);
        } catch (NoSuchFieldException e) {
            throw I.quiet(e);
        } catch (IllegalAccessException e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark();
        ReflectionFieldSetterBenchmark base = new ReflectionFieldSetterBenchmark();

        MethodHandle mh = MethodHandles.lookup().findSetter(ReflectionFieldSetterBenchmark.class, "one", int.class);
        MethodType type = mh.type();
        type = type.wrap().changeReturnType(void.class);

        MethodType factoryType = MethodType.methodType(BiConsumer.class, MethodHandle.class);
        MethodType interfaceMethodType = type.erase();
        MethodHandle impl = MethodHandles.exactInvoker(mh.type());
        CallSite site = LambdaMetafactory.metafactory(MethodHandles.lookup(), "accept", factoryType, interfaceMethodType, impl, type);
        BiConsumer<Object, Object> functionSetter = (BiConsumer<Object, Object>) site.getTarget().invokeExact(mh);
        benchmark.measure("BiConsumerMH", () -> {
            functionSetter.accept(base, Integer.valueOf(1));
            return "one";
        });

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

        benchmark.measure("MH", () -> {
            try {
                mh.invoke(base, Integer.valueOf(1));
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        VarHandle vh = MethodHandles.lookup().findVarHandle(ReflectionFieldSetterBenchmark.class, "one", int.class);
        benchmark.measure("VH", () -> {
            try {
                vh.set(base, 1);
                return "one";
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        // BiIntConsumer lambda = createSetter(setter);
        // benchmark.measure("LambdaMeta", () -> {
        // try {
        // lambda.accept(base, 1);
        // return "one";
        // } catch (Throwable e) {
        // throw I.quiet(e);
        // }
        // });

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

    public int one = 0;

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