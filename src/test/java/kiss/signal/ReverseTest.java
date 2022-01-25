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

class ReverseTest extends SignalTester {

    @Test
    void reverse() {
        monitor(signal -> signal.reverse());

        assert main.emit("A", "B", "C").value();
        assert main.isNotCompleted();
        assert main.emit(Complete).value("C", "B", "A");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void acceptNull() {
        monitor(signal -> signal.reverse());

        assert main.emit("A", null, "C").value();
        assert main.isNotCompleted();
        assert main.emit(Complete).value("C", null, "A");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.reverse());

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.reverse());

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}