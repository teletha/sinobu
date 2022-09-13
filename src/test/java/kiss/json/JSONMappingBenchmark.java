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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;

public class JSONMappingBenchmark {

    private static final String json = """
            {
                "member": [
                    {
                        "name": "Ada",
                        "age": 20
                    },{
                        "name": "BrainCrash",
                        "age": 21
                    },{
                        "name": "COBOL",
                        "age": 22
                    },{
                        "name": "Delphi",
                        "age": 23
                    }
                ]
            }
            """;

    public static void main(String[] args) throws IOException {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("New parser", () -> {
            return new FastParser().parse(new FastStringReader(json), Group.class);
        });

        benchmark.measure("Sinobu", () -> {
            return I.json(json).as(Group.class);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson.JSON.parseObject(json, Group.class);
        });

        Gson gson = new Gson();
        benchmark.measure("Gson", () -> {
            return gson.fromJson(json, Group.class);
        });

        ObjectMapper mapper = new ObjectMapper();
        benchmark.measure("Jackson", () -> {
            return mapper.readValue(json, Group.class);
        });

        benchmark.perform();
    }

    /**
     * 
     */
    public static class Person {
        public String name;

        public int age;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Person [name=" + name + ", age=" + age + "]";
        }
    }

    /**
     * 
     */
    public static class Group {
        public List<Person> member = new ArrayList();
    }

    public static class FastStringReader implements ToIntFunction<char[]> {

        private final String text;

        private final int length;

        private int next;

        public FastStringReader(String text) {
            this.text = text;
            this.length = text.length();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int applyAsInt(char[] value) {
            if (length <= next) return -1;
            int size = Math.min(length - next, value.length);
            text.getChars(next, next + size, value, 0);
            next += size;
            return size;
        }
    }
}