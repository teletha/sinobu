/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.jdk;

import java.util.function.Function;

import antibug.profiler.Benchmark;
import kiss.I;
import kiss.WiseFunction;

public class WiseBenchmark {

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark();

        Function<String, String> function = (value) -> value.toLowerCase();
        benchmark.measure("normal", () -> {
            return function.apply("ok").hashCode();
        });

        WiseFunction<String, String> wised = I.wiseF(function);
        benchmark.measure("wised", () -> {
            return wised.apply("ok").hashCode();
        });

        benchmark.perform();
    }
}
