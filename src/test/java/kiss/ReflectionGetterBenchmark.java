/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.reflect.Method;

import antibug.profiler.Benchmark;

public class ReflectionGetterBenchmark {

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark();
        ReflectionGetterBenchmark base = new ReflectionGetterBenchmark();

        Method method = ReflectionGetterBenchmark.class.getMethod("one");
        benchmark.measure("Reflection", () -> {
            try {
                return method.invoke(base);
            } catch (Exception e) {
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