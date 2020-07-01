/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/09/28 13:30:02
 */
class AllTest extends SignalTester {

    @Test
    void OK() {
        monitor(Integer.class, Boolean.class, signal -> signal.all(v -> v % 2 == 0));

        assert main.emit(2, 4, 6, 8, Complete).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void NG() {
        monitor(Integer.class, Boolean.class, signal -> signal.all(v -> v % 2 == 0));

        assert main.emit(2, 4, 5, 6).value(false);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void acceptNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(Integer.class, Boolean.class, signal -> signal.all(null));
        });
    }

    @Test
    void empty() {
        monitor(Integer.class, Boolean.class, signal -> signal.all(v -> v % 2 == 0));

        assert main.emit(Complete).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}