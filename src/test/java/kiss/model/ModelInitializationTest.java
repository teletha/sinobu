/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import antibug.powerassert.PowerAssertOff;
import kiss.I;

/**
 * @version 2018/08/14 23:26:32
 */
class ModelInitializationTest {

    // don't remove @PowerAssertOff annotation because this test checks whether the model instance is
    // singleton or not. PowerAssert invokes same test with the cached model instance when error was
    // occured.
    @PowerAssertOff
    @Test
    void multiThreads() throws InterruptedException {
        List<Future<Model<Some>>> results = new ArrayList();
        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++) {
            results.add(pool.submit(() -> Model.of(Some.class)));
        }
        Thread.sleep(500);
        assert 2 <= results.size();
        assert I.signal(results).map(Future::get).toSet().size() == 1;
    }

    /**
     * @version 2018/08/14 23:27:01
     */
    @SuppressWarnings("unused")
    private static class Some {

        public String text;

        public int value;
    }
}