/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;

public class JSONParseShortBenchmark {

    private static final String json = """
            {
                "version": 2,
                "type": "update"
            }
            """;

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark().visualize();

        benchmark.measure("Sinobu", () -> {
            return I.json(json);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson2.JSON.parseObject(json, JSONObject.class);
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