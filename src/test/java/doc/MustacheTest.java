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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import kiss.I;

public class MustacheTest {

    @Test
    public void usage() {
        record Person(String name, int age) {
        }

        Person context = new Person("Takina Inoue", 16);
        String evaluated = I.express("She is {name}, {age} years old.", context);

        assert evaluated.equals("She is Takina Inoue, 16 years old.");
    }

    @Nested
    public class Template {

        public String template = "<h1>{title}</h1>";

        record Data(String title) {
        }

        @Test
        public void template() {
            Data data = new Data("Hello Mustache");
            String evaluated = I.express(template, data);
            assert evaluated.equals("<h1>Hello Mustache</h1>");
        }
    }
}
