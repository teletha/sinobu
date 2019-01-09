/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class OrTest extends SignalTester {

    @Test
    void or() {
        monitor(signal -> signal.or("at least"));

        assert main.emit("some", Complete).value("some");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();

        monitor(signal -> signal.or("at least"));

        assert main.emit(Complete).value("at least");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.or("at least"));

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}
