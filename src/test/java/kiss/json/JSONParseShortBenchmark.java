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

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;
import kiss.JSON;

public class JSONParseShortBenchmark {

    private static final String json = """
            {
                "version": 2,
                "type": "update"
            }
            """;

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Flat Parser", () -> {
            return new FlatParser(json);
        });

        benchmark.measure("New Parser", () -> {
            return new FastParser().parse(json, JSON.class);
        });

        benchmark.measure("Sinobu", () -> {
            return I.json(json);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson.JSON.parseObject(json, JSONObject.class);
        });

        Gson gson = new Gson();
        benchmark.measure("Gson", () -> {
            return gson.fromJson(json, JSONObject.class);
        });

        ObjectMapper mapper = new ObjectMapper();
        benchmark.measure("Jackson", () -> {
            return mapper.readTree(json);
        });

        benchmark.perform();
    }
}