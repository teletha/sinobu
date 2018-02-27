/*
 * Copyright (C) 2018 Nameless Production Committee
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

import antibug.benchmark.Benchmark;
import antibug.benchmark.Benchmark.Code;
import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2017/04/02 16:14:24
 */
public class InstantiationBenchmark {

    @Rule
    @ClassRule
    public static final Benchmark benchmark = new Benchmark();

    public void instantiate() {
        benchmark.measure(new Code() {

            @Override
            public Object measure() throws Throwable {
                return I.make(Person.class);
            }
        });
    }
}
