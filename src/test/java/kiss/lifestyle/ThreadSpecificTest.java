/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

class ThreadSpecificTest {

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    void resolve1() {
        ThreadSpecificClass instance1 = I.make(ThreadSpecificClass.class);
        assert instance1 != null;

        ThreadSpecificClass instance2 = I.make(ThreadSpecificClass.class);
        assert instance2 != null;
        assert instance1 == instance2;
    }

    @Test
    void resolve2() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        // container
        Future<ThreadSpecificClass>[] futures = new Future[2];

        // start
        for (int i = 0; i < 2; i++) {
            futures[i] = executor.submit(new Callable<ThreadSpecificClass>() {

                /**
                 * @see java.util.concurrent.Callable#call()
                 */
                @Override
                public ThreadSpecificClass call() throws Exception {
                    try {
                        ThreadSpecificClass instance = I.make(ThreadSpecificClass.class);

                        countDownLatch.countDown();

                        return instance;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw I.quiet(e);
                    }
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
