/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;

public class JSONMappingBenchmark {

    private static final String json = """
            {
                "name": "Alexandra",
                "age": 20
            }
            """;

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Sinobu", () -> {
            return I.json(json).as(Person.class);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson.JSON.parseObject(json, Person.class);
        });

        Gson gson = new Gson();
        benchmark.measure("Gson", () -> {
            return gson.fromJson(json, Person.class);
        });

        ObjectMapper mapper = new ObjectMapper();
        benchmark.measure("Jackson", () -> {
            return mapper.readValue(json, Person.class);
        });

        benchmark.perform();
    }

    /**
     * 
     */
    public static class Person {
        public String name;

        public int age;
    }
}