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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AnyTest extends SignalTester {

    @Test
    void OK() {
        monitor(Integer.class, Boolean.class, signal -> signal.any(v -> v % 2 == 0));

        assert main.emit(1, 3, 4, 5, 6, 7).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void NG() {
        monitor(Integer.class, Boolean.class, signal -> signal.any(v -> v % 2 == 0));

        assert main.emit(1, 3, 5, 7, Complete).value(false);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void acceptNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(Integer.class, Boolean.class, signal -> signal.any(null));
        });
    }

    @Test
    void empty() {
        monitor(Integer.class, Boolean.class, signal -> signal.any(v -> v % 2 == 0));

        assert main.emit(Complete).value(false);
        assert main.isCompleted();
        assert main.isDisposed();
        assert main.isNotError();
    }

    @Test
    void error() {
        monitor(Integer.class, Boolean.class, signal -> signal.any(v -> v % 2 == 0));

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}