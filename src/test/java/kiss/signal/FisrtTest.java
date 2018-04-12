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

/**
 * @version 2018/03/02 16:28:04
 */
class FisrtTest extends SignalTester {

    @Test
    void first() {
        monitor(signal -> signal.first());

        assert main.emit(1, 2, 3).value(1);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.first());

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.first());

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void firstWithDefault() {
        monitor(signal -> signal.first("Default"));

        assert main.emit(1, 2, 3).value(1);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void completeWithDefault() {
        monitor(signal -> signal.first("Default"));

        assert main.emit(Complete).value("Default");
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void errorWithDefault() {
        monitor(signal -> signal.first("Default"));

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}
