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

import java.util.Random;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;
import net.bytebuddy.utility.RandomString;

public class JSONParseHugeBenchmark {

    private static final String json;

    static {
        String EOL = "\r\n";
        Random random = new Random(50);
        int size = 15000;

        StringBuilder builder = new StringBuilder();
        builder.append("{ \"persons\":[").append(EOL);
        for (int i = 0; i < size; i++) {
            builder.append("  {").append(EOL);
            builder.append("    \"name\" : \"").append(RandomString.make(15)).append("\",").append(EOL);
            builder.append("    \"age\" : ").append(random.nextInt(100)).append(",").append(EOL);
            builder.append("    \"point\" : ").append(random.nextFloat(0.3f)).append(",").append(EOL);
            builder.append("    \"active\" : ").append(random.nextBoolean()).append(EOL);
            builder.append("  }");

            if (i + 1 != size) {
                builder.append(",").append(EOL);
            }
        }
        builder.append("]").append(EOL).append("}");

        json = builder.toString();
    }

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