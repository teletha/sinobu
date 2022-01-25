/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class CountTest extends SignalTester {

    @Test
    void count() {
        monitor(Long.class, signal -> signal.count());

        assert main.emit(10).value(1L);
        assert main.emit(20, 30, 40, 50).value(2L, 3L, 4L, 5L);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(Long.class, signal -> signal.count());

        assert main.emit(20, Complete).value(1L);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(Long.class, signal -> signal.count());

        assert main.emit(30, Error).value(1L);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}