/**
 * Copyright (C) 2011 Nameless Production Committee.
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

import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.Person;
import ezunit.AbstractMicroBenchmarkTest;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 8:36:15
 */
public class InstantiationBenchmark extends AbstractMicroBenchmarkTest {

    @Test
    public void instantiate() {
        benchmark(new Callable() {

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Exception {
                return I.make(Person.class);
            }
        });
    }
}
