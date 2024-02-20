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

        String one = I.write(person);
        String other = normalizeLineFeed("""
                {
                    "age": 23,
                    "name": "Joe"
                }
                """);

        for (int i = 0; i < one.length(); i++) {
            if (one.charAt(i) != other.charAt(i)) {
                System.out.println(i + "  " + one.charAt(i) + "@" + other.charAt(i));
            }
        }

        assert I.write(person).equals(normalizeLineFeed("""
                {
                    "age": 23,
                    "name": "Joe"
                }
                """));
    }

    private String normalizeLineFeed(String text) {
        return text.replaceAll("\r\n", "\n");
    }
}
