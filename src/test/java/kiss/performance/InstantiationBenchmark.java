/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.performance;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.benchmark.Benchmark;
import antibug.benchmark.Benchmark.Code;
import kiss.I;
import kiss.sample.bean.Person;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 8:36:15
 */
public class InstantiationBenchmark {

    @Rule
    @ClassRule
    public static final Benchmark benchmark = new Benchmark();

    @Test
    public void instantiate() {
        benchmark.measure(new Code() {

            @Override
            public Object measure() throws Throwable {
                return I.make(Person.class);
            }
        });
    }
}
