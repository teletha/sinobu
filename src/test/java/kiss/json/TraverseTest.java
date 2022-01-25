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

import java.util.List;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;
import kiss.sample.bean.Person;

class TraverseTest {

    @Test
    void integer() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'age': 20",
        "}");
        // @formatter:on

        List<Integer> values = json.find(int.class, "age");
        assert values.size() == 1;
        assert values.get(0) == 20;
    }

    @Test
    void string() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'name': 'Jill'",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "name");
        assert values.size() == 1;
        assert values.get(0).equals("Jill");
    }

    @Test
    void nullValue() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'name': null",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "name");
        assert values.size() == 0;
    }

    @Test
    void nestObject() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'city': {",
        "    'id': 'NY'",
        "  }",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "city", "id");
        assert values.size() == 1;
        assert values.get(0).equals("NY");
    }

    @Test
    void arrayAll() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    'Jill',",
        "    'Bell',",
        "    'Alice'",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "names", "*");
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Alice");
    }

    @Test
    void arrayIndex() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    'Jill',",
        "    'Bell',",
        "    'Alice'",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "names", "1");
        assert values.size() == 1;
        assert values.get(0).equals("Bell");
    }

    @Test
    void arrayObject() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    {'name': 'Jill'},",
        "    {'name': 'Bell'},",
        "    {'name': 'Alice'}",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "names", "*", "name");
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Alice");
    }

    @Test
    void arrayIndexObject() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    {'name': 'Jill'},",
        "    {'name': 'Bell'},",
        "    {'name': 'Alice'}",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find(String.class, "names", "1", "name");
        assert values.size() == 1;
        assert values.get(0).equals("Bell");
    }

    @Test
    void arrayModel() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    {'firstName': 'Jill'},",
        "    {'firstName': 'Bell'},",
        "    {'firstName': 'Alice'}",
        "  ]",
        "}");
        // @formatter:on

        List<Person> values = json.find(Person.class, "names", "*");
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Jill");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Alice");
    }

    @Test
    void wildcard() {
        // @formatter:off
        JSON json = json(
        "[",
        "    {'firstName': 'Jill'},",
        "    {'firstName': 'Bell'},",
        "    {'firstName': 'Alice'}",
        "]");
        // @formatter:on

        List<Person> values = json.find(Person.class, "*");
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Jill");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Alice");
    }

    @Test
    void wildcardReversed() {
        // @formatter:off
        JSON json = json(
        "[",
        "    {'firstName': 'Jill'},",
        "    {'firstName': 'Bell'},",
        "    {'firstName': 'Alice'}",
        "]");
        // @formatter:on

        List<Person> values = json.find(Person.class, "$");
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Alice");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Jill");
    }

    @Test
    void emptyPath() {
        // @formatter:off
        JSON json = json(
        "{",
        "  'age': 20",
        "}");
        // @formatter:on

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