/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.util.List;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;
import kiss.sample.bean.Person;

class FindTest {

    @Test
    void integer() {
        JSON json = json("""
                {
                    "age": 20
                }
                """);

        List<Integer> values = json.find(int.class, "age");
        assert values.size() == 1;
        assert values.get(0) == 20;
    }

    @Test
    void integerFromString() {
        JSON json = json("""
                {
                    "age": "20"
                }
                """);

        List<Integer> values = json.find(int.class, "age");
        assert values.size() == 1;
        assert values.get(0) == 20;
    }

    @Test
    void decimalFloat() {
        JSON json = json("""
                {
                    "size": 1.3
                }
                """);

        List<Float> values = json.find(float.class, "size");
        assert values.size() == 1;
        assert values.get(0) == 1.3f;
    }

    @Test
    void decimalFloatFromString() {
        JSON json = json("""
                {
                    "size": "1.3"
                }
                """);

        List<Float> values = json.find(float.class, "size");
        assert values.size() == 1;
        assert values.get(0) == 1.3f;
    }

    @Test
    void decimalDouble() {
        JSON json = json("""
                {
                    "size": 1.3
                }
                """);

        List<Double> values = json.find(double.class, "size");
        assert values.size() == 1;
        assert values.get(0) == 1.3d;
    }

    @Test
    void decimalDoubleFromString() {
        JSON json = json("""
                {
                    "size": "1.3"
                }
                """);

        List<Double> values = json.find(double.class, "size");
        assert values.size() == 1;
        assert values.get(0) == 1.3d;
    }

    @Test
    void string() {
        JSON json = json("""
                {
                    "name": "Jill"
                }
                """);

        List<String> values = json.find(String.class, "name");
        assert values.size() == 1;
        assert values.get(0).equals("Jill");
    }

    @Test
    void nullValue() {
        JSON json = json("""
                {
                    "name": null
                }
                """);

        List<String> values = json.find(String.class, "name");
        assert values.size() == 0;
    }

    @Test
    void nestObject() {
        JSON json = json("""
                {
                    "city": {
                        "id": "NY"
                    }
                }
                """);

        List<String> values = json.find(String.class, "city", "id");
        assert values.size() == 1;
        assert values.get(0).equals("NY");
    }

    @Test
    void arrayAll() {
        JSON json = json("""
                {
                    "names": [
                        "Jill",
                        "Bell",
                        "Alice"
                    ]
                }
                """);

        List<String> values = json.find(String.class, "names", "*");
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Alice");
    }

    @Test
    void arrayIndex() {
        JSON json = json("""
                {
                    "names": [
                        "Jill",
                        "Bell",
                        "Alice"
                    ]
                }
                """);

        List<String> values = json.find(String.class, "names", "1");
        assert values.size() == 1;
        assert values.get(0).equals("Bell");
    }

    @Test
    void arrayObject() {
        JSON json = json("""
                {
                    "names": [
                        { "name" : "Jill" },
                        { "name" : "Bell" },
                        { "name" : "Alice" }
                    ]
                }
                """);

        List<String> values = json.find(String.class, "names", "*", "name");
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Alice");
    }

    @Test
    void arrayIndexObject() {
        JSON json = json("""
                {
                    "names": [
                        { "name" : "Jill" },
                        { "name" : "Bell" },
                        { "name" : "Alice" }
                    ]
                }
                """);

        List<String> values = json.find(String.class, "names", "1", "name");
        assert values.size() == 1;
        assert values.get(0).equals("Bell");
    }

    @Test
    void arrayModel() {
        JSON json = json("""
                {
                    "names": [
                        { "firstName" : "Jill" },
                        { "firstName" : "Bell" },
                        { "firstName" : "Alice" }
                    ]
                }
                """);

        List<Person> values = json.find(Person.class, "names", "*");
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Jill");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Alice");
    }

    @Test
    void wildcard() {
        JSON json = json("""
                [
                        { "firstName" : "Jill" },
                        { "firstName" : "Bell" },
                        { "firstName" : "Alice" }
                ]
                """);

        List<Person> values = json.find(Person.class, "*");
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Jill");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Alice");
    }

    @Test
    void wildcardReversed() {
        JSON json = json("""
                [
                        { "firstName" : "Jill" },
                        { "firstName" : "Bell" },
                        { "firstName" : "Alice" }
                ]
                """);

        List<Person> values = json.find(Person.class, "$");
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Alice");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Jill");
    }

    @Test
    void wildcardAndReversed() {
        JSON json = json("""
                [
                    [
                        {'firstName': 'Jill'},
                        {'firstName': 'Bell'}
                    ],
                    [
                        {'firstName': 'Alice'},
                        {'firstName': 'Kayle'}
                    ]
                ]
                """);

        List<Person> values = json.find(Person.class, "*", "$");
        assert values.size() == 4;
        assert values.get(0).getFirstName().equals("Kayle");
        assert values.get(1).getFirstName().equals("Alice");
        assert values.get(2).getFirstName().equals("Bell");
        assert values.get(3).getFirstName().equals("Jill");
    }

    @Test
    void wildcardAttribute() {
        JSON json = json("""
                {
                    '1': 'Jill',
                    '2': 'Bell',
                    '3': 'Holo'
                }
                """);

        List<String> values = json.find(String.class, "*");
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Holo");
    }

    @Test
    void wildcardMixed() {
        JSON json = json("""
                {
                     "data": {
                          "delete": [
                             {
                                   "test": "3001.00"
                             }
                          ],
                          "update": [
                             {
                                   "test": "2999.00"
                             }
                          ],
                          "insert": [
                             {
                                   "test": "2998.00"
                             }
                          ],
                          "illegal": 0
                     }
                }
                """);

        List<JSON> values = json.find("data", "*", "*");
        assert values.size() == 4;
    }

    @Test
    void emptyPath() {
        JSON json = json("""
                {
                    "age": 20
                }
                """);

        List<Integer> values = json.get("age").find(int.class, new String[0]);
        assert values.size() == 1;
        assert values.get(0) == 20;
    }

    /**
     * <p>
     * Write JSON.
     * </p>
     * 
     * @param texts
     * @return
     */
    private static JSON json(String... texts) {
        StringJoiner joiner = new StringJoiner("\r\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }
        return I.json(joiner.toString());
    }
}