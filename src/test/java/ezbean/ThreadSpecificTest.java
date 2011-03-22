/*
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
package ezbean;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.junit.Test;

import ezunit.MultiThreadTestCase;

/**
 * @version 2011/03/22 16:28:30
 */
public class ThreadSpecificTest extends MultiThreadTestCase {

    @Test
    public void resolve1() {
        ThreadSpecificClass instance1 = I.make(ThreadSpecificClass.class);
        assert instance1 != null;

        ThreadSpecificClass instance2 = I.make(ThreadSpecificClass.class);
        assert instance2 != null;
        assert instance1 == instance2;
    }

    @Test
    public void resolve2() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        // container
        Future<ThreadSpecificClass>[] futures = new Future[2];

        // start
        for (int i = 0; i < 2; i++) {
            futures[i] = executor.submit(new Callable<ThreadSpecificClass>() {

                /**
                 * @see java.util.concurrent.Callable#call()
                 */
                public ThreadSpecificClass call() throws Exception {
                    ThreadSpecificClass instance = I.make(ThreadSpecificClass.class);

                    countDownLatch.countDown();

                    return instance;
                }
            });
        }

        // await all
        countDownLatch.await();
        assert futures[0] != null;
        assert futures[1] != null;

        ThreadSpecificClass instance1 = futures[0].get();
        assert instance1 != null;

        ThreadSpecificClass instance2 = futures[1].get();
        assert instance2 != null;
        assert instance1 != instance2;
    }

    /**
     * @version 2011/03/22 16:29:27
     */
    @Manageable(lifestyle = ThreadSpecific.class)
    private static class ThreadSpecificClass {
    }
}
