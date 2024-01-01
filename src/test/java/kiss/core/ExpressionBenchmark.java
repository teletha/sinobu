/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.io.StringReader;
import java.io.StringWriter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.samskivert.mustache.Template;

import antibug.profiler.Benchmark;
import kiss.I;
import kiss.sample.bean.Person;

public class ExpressionBenchmark {

    private static final String text = "{firstName} is {age} years old.";

    private static final String text2 = "{{firstName}} is {{age}} years old.";

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark().visualize();

        Person person = new Person();
        person.setAge(20);
        person.setFirstName("Nana");

        benchmark.measure("sinobu", () -> {
            return I.express(text, person);
        });

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(text2), "test");
        benchmark.measure("mustache.java", () -> {
            return mustache.execute(new StringWriter(), person).toString();
        });

        Template template = com.samskivert.mustache.Mustache.compiler().compile(text2);
        benchmark.measure("jmustache", () -> {
            return template.execute(person);
        });

        benchmark.perform();
    }
}