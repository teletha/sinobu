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
package ezbean.benchmark;

import java.util.concurrent.Callable;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 8:35:18
 */
public abstract class AbstractMicroBenchmarkTest {

    public void benchmark(Callable task) {
        MicroBenchmark benchmark = new MicroBenchmark(task, 30, 10);
        benchmark.execute();
    }
}
