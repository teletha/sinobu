/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.jupiter.api.Test;

import kiss.I;

class UncaughtExceptionHandlerTest extends SignalTester {

    @Test
    void reduceSequencialSameError() {
        Counter counter = new Counter();
        Thread.setDefaultUncaughtExceptionHandler(counter);

        try {
            I.signal(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map(errorFunction()).to();
        } catch (Throwable x) {
            // ignore
        }
        assert counter.count == 1;
    }

    private static class Counter implements UncaughtExceptionHandler {

        private int count;

        /**
         * {@inheritDoc}
         */
        @Override
        public void uncaughtException(Thread thread, Throwable e) {
            count++;
        }
    }
}