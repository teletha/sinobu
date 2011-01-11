/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.performance;

import java.util.concurrent.Callable;

import org.junit.BeforeClass;
import org.junit.Test;

import ezbean.I;
import ezbean.model.ClassUtil;
import ezunit.AbstractMicroBenchmarkTest;

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
        benchmark(new Callable() {

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Exception {
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
