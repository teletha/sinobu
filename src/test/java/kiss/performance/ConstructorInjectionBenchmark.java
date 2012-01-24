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


import org.junit.BeforeClass;
import org.junit.Test;

import testament.AbstractMicroBenchmarkTest;
import testament.BenchmarkCode;

import kiss.I;
import kiss.model.ClassUtil;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 9:44:55
 */
public class ConstructorInjectionBenchmark extends AbstractMicroBenchmarkTest {

    @BeforeClass
    public static void initialize() {
        I.load(ClassUtil.getArchive(InstantiationBenchmark.class));
    }

    @Test
    public void instantiate() {
        benchmark(new BenchmarkCode() {

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Throwable {
                return I.make(ConstructorInjection.class);
            }
        });
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/11/05 9:56:32
     */
    @SuppressWarnings("unused")
    private static class ConstructorInjection {

        /** The dependency, */
        private Injected1 injected1;

        /** The dependency, */
        private Injected2 injected2;

        /**
         * Create ConstructorInjection instance.
         * 
         * @param injected1
         * @param injected2
         */
        public ConstructorInjection(Injected1 injected1, Injected2 injected2) {
            this.injected1 = injected1;
            this.injected2 = injected2;
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/11/05 9:56:05
     */
    private static class Injected1 {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/11/05 9:56:01
     */
    private static class Injected2 {
    }
}
