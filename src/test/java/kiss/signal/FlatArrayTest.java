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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FlatArrayTest extends SignalTester {

    @Test
    void value() {
        monitor(String.class, signal -> signal.flatArray(v -> v.split("")));

        assert main.emit("TEST").value("T", "E", "S", "T");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void error() {
        monitor(String.class, signal -> signal.flatArray(v -> v.split("")));

        assert main.emit(Error, "TEST").value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorInFunction() {
        monitor(String.class, signal -> signal.flatArray(errorFunction()));

        assert main.emit("TEST").value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(String.class, signal -> signal.flatArray(v -> v.split("")));

        assert main.emit(Complete, "TEST").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void rejectNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(String.class, signal -> signal.flatArray(null));
        });
    }
}