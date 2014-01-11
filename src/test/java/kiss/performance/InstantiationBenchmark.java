/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.performance;

import kiss.I;
import kiss.sample.bean.Person;

import org.junit.Rule;
import org.junit.Test;

import antibug.benchmark.Benchmark;
import antibug.benchmark.Benchmark.Code;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 8:36:15
 */
public class InstantiationBenchmark {

    @Rule
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
