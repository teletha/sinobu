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
import java.lang.reflect.Constructor;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionConstructorBenchmark {

    private static final MethodHandle constantMH;

    static {
        try {
            constantMH = MethodHandles.lookup().findConstructor(Meta.class, MethodType.methodType(void.class));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] p) throws Throwable {
        Benchmark benchmark = new Benchmark();

        Constructor constructor = Meta.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object[] args = {};

        benchmark.measure("Reflection", () -> {
            try {
                return constructor.newInstance(args);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        // benchmark.measure("ConstantMH", () -> {
        // try {
        // return (Meta) constantMH.invokeExact();
        // } catch (Throwable e) {
        // throw I.quiet(e);
        // }
        // });
        //
        // WiseSupplier function = Model.createConstructor(constructor);
        // benchmark.measure("LambdaMetaFactory", () -> {
        // try {
        // return function.get();
        // } catch (Throwable e) {
        // throw I.quiet(e);
        // }
        // });

        benchmark.measure("I#make", () -> {
            return I.make(Meta.class);
        });

        benchmark.measure("DirectCall", () -> {
            return new Meta();
        });

        benchmark.perform();
    }

    public static class Meta {

        String text;

        private Meta() {
            this.text = "";
        }

        public Meta(String text) {
            this.text = text;
        }
    }
}