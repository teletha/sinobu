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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionFieldSetterBenchmark {

    private static final MethodHandle staticSetter;

    static {
        try {
            staticSetter = MethodHandles.lookup().findSetter(ReflectionFieldSetterBenchmark.class, "one", int.class);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

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

        MethodHandle unreflect = MethodHandles.lookup().unreflectSetter(setter);

        MethodType type = unreflect.type().wrap().changeReturnType(void.class);
        CallSite site = LambdaMetafactory.metafactory(MethodHandles.lookup(), "accept", MethodType
                .methodType(BiIntConsumer.class, MethodHandle.class), type.erase(), MethodHandles.exactInvoker(unreflect.type()), type);
        BiIntConsumer bi = (BiIntConsumer) site.getTarget().invokeExact(unreflect);
        benchmark.measure("LambdaMeta", () -> {
            bi.accept(base, 1);
            return "one";
        });

        // WiseBiConsumer setterFunction = Model.createSetter(setter);
        // benchmark.measure("LambdaMetaFactory", () -> {
        // try {
        // setterFunction.accept(base, "one");
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

    private int one = 0;

    public interface BiIntConsumer<T> {
        void accept(T one, int two);
    }
}