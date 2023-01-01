/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import antibug.profiler.Benchmark;

public class CollectionToListBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        ArrayDeque<String> collection = new ArrayDeque();
        collection.add("A");
        collection.add("B");
        collection.add("C");
        collection.add("D");
        collection.add("E");

        benchmark.measure("Array.asList(collection.toArray())", () -> {
            return Arrays.asList(collection.toArray());
        });

        benchmark.measure("new ArrayList(collection)", () -> {
            return new ArrayList(collection);
        });

        benchmark.measure("List.of(collection.toArray())", () -> {
            return List.of(collection.toArray());
        });

        benchmark.measure("List.copyOf(collection)", () -> {
            return List.copyOf(collection);
        });

        benchmark.perform();
    }
}