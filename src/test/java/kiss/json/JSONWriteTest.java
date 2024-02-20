/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import org.junit.jupiter.api.Test;

import kiss.I;

public class JSONWriteTest {

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
