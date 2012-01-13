/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.performance;

import hub.AbstractMicroBenchmarkTest;
import hub.BenchmarkCode;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 8:36:15
 */
public class InstantiationBenchmark extends AbstractMicroBenchmarkTest {

    @Test
    public void instantiate() {
        benchmark(new BenchmarkCode() {

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Throwable {
                return I.make(Person.class);
            }
        });
    }
}
