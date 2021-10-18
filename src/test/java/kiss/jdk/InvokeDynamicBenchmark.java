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

import antibug.profiler.Benchmark;
import kiss.WiseFunction;
import kiss.WiseSupplier;

public class InvokeDynamicBenchmark {

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark();

        WiseFunction<String, String> function = (value) -> value.toLowerCase();
        // benchmark.measure("normal", () -> {
        // return function.apply("ok").hashCode();
        // });

        WiseSupplier<String> binded = function.bind("ok");
        benchmark.measure("binded", () -> {
            return binded.get().hashCode();
        });

        // WiseSupplier<String> bindedDirect = function.bindDirect("ok");
        // benchmark.measure("bindedDirect", () -> {
        // return bindedDirect.get().hashCode();
        // });

        benchmark.perform();
    }
}
