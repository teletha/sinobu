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

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;

public class JSONWriteTest {

    @Test
    public void write() {
        JSON json = new JSON();
        json.set("key", "value");

        assert json.get(String.class, "key").equals("value");
    }

    @Test
    public void testName() {
        record Person(String name, int age) {
        }

        Person person = new Person("Joe", 23);
        assertText(I.write(person), """
                {
                    "age": 23,
                    "name": "Joe"
                }
                """);
    }

    private void assertText(String one, String other) {
        one = one.strip().replaceAll("\\t", "    ");
        other = other.strip().replaceAll("\\t", "    ");
        assert one.equals(other);
    }
}