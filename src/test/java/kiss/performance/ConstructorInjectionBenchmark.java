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

import org.junit.Rule;
import org.junit.Test;

import antibug.benchmark.Benchmark;
import antibug.benchmark.Benchmark.Code;

/**
 * @version 2014/07/12 14:53:42
 */
public class ConstructorInjectionBenchmark {

    @Rule
    public static final Benchmark benchmark = new Benchmark();

    @Test
    public void instantiate() {
        benchmark.measure(new Code() {

            @Override
            public Object measure() throws Throwable {
                return I.make(ConstructorInjection.class);
            }
        });
    }

    /**
     * @version 2012/01/31 16:16:46
     */
    @SuppressWarnings("unused")
    private static class ConstructorInjection {

        /** The dependency, */
        private Injected1 injected1;

        /** The dependency, */
        private Injected2 injected2;

        /**
         * Create ConstructorInjection instance.
         */
        public ConstructorInjection(Injected1 injected1, Injected2 injected2) {
            this.injected1 = injected1;
            this.injected2 = injected2;
        }
    }

    /**
     * @version 2012/01/31 16:16:51
     */
    private static class Injected1 {
    }

    /**
     * @version 2012/01/31 16:16:54
     */
    private static class Injected2 {
    }
}
