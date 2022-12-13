/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package doc;

import org.junit.jupiter.api.Test;

import kiss.I;

class MustacheTest {

    String template = "She is {name}, {age} years old.";

    record Person(String name, int age) {
    }

    @Test
    void use() {
        Person data = new Person("Takina Inoue", 16);

        assert I.express(template, data).equals("She is Takina Inoue, 16 years old.");
    }
}
