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
package ezbean;

import static org.junit.Assert.*;


import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;


import org.junit.Test;

import ezbean.I;
import ezbean.Manageable;
import ezbean.ThreadSpecific;
import ezunit.MultiThreadTestCase;

/**
 * DOCUMENT.
 * 
 * @version 2008/05/30 22:55:35
 */
public class ThreadSpecificTest extends MultiThreadTestCase {

    /**
     * Test {@link ThreadSpecific}.
     */
    @Test
    public void testResolve1() {
        ThreadSpecificClass instance1 = I.make(ThreadSpecificClass.class);
        assertNotNull(instance1);

        ThreadSpecificClass instance2 = I.make(ThreadSpecificClass.class);
        assertNotNull(instance2);

        assertSame(instance1, instance2);
    }

    /**
     * Test {@link ThreadSpecific}.
     */
    @Test
    public void testResolve2() throws Exception {
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
        assertNotNull(futures[0]);
        assertNotNull(futures[1]);

        ThreadSpecificClass instance1 = futures[0].get();
        assertNotNull(instance1);

        ThreadSpecificClass instance2 = futures[1].get();
        assertNotNull(instance2);

        assertNotSame(instance1, instance2);
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/05/30 22:50:38
     */
    @Manageable(lifestyle = ThreadSpecific.class)
    private static class ThreadSpecificClass {
    }
}
