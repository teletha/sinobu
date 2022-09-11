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

import antibug.profiler.Benchmark;
import kiss.model.Model;
import kiss.model.Property;

public class PropertyAccessBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        Model<PropertyAccessBenchmark> model = Model.of(PropertyAccessBenchmark.class);
        Property property = model.property("name");
        PropertyAccessBenchmark instance = new PropertyAccessBenchmark();

        benchmark.measure("List.copyOf(collection)", () -> {
            return model.get(instance, property);
        });

        benchmark.perform();
    }

    public String name = "OK";
}