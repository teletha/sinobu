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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                        "age": 20,
                        "active": true
                    },{
                        "name": "BrainCrash",
                        "age": 21,
                        "active": false
                    },{
                        "name": "COBOL",
                        "age": 22,
                        "active": true
                    },{
                        "name": "Delphi",
                        "age": 23,
                        "active": false
                    }
                ]
            }
            """;

    public static void main(String[] args) throws IOException {
        Benchmark benchmark = new Benchmark().visualize().trial(5);
        // benchmark = benchmark.duration(20, TimeUnit.SECONDS);

        benchmark.measure("Sinobu", () -> {
            return I.json(json, Group.class);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson2.JSON.parseObject(json, Group.class);
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

        public boolean active;
    }

    /**
     * 
     */
    public static class Group {
        public List<Person> member = new ArrayList();
    }

    /**
     * 
     */
    public static class MethodPerson {
        private String name;

        private int age;

        /**
         * Get the name property of this {@link JSONMappingBenchmark.MethodPerson}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link JSONMappingBenchmark.MethodPerson}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Get the age property of this {@link JSONMappingBenchmark.MethodPerson}.
         * 
         * @return The age property.
         */
        public int getAge() {
            return age;
        }

        /**
         * Set the age property of this {@link JSONMappingBenchmark.MethodPerson}.
         * 
         * @param age The age value to set.
         */
        public void setAge(int age) {
            this.age = age;
        }
    }

    /**
     * 
     */
    public static class MethodGroup {
        private List<MethodPerson> member = new ArrayList();

        /**
         * Get the member property of this {@link JSONMappingBenchmark.MethodGroup}.
         * 
         * @return The member property.
         */
        public List<MethodPerson> getMember() {
            return member;
        }

        /**
         * Set the member property of this {@link JSONMappingBenchmark.MethodGroup}.
         * 
         * @param member The member value to set.
         */
        public void setMember(List<MethodPerson> member) {
            this.member = member;
        }
    }

}