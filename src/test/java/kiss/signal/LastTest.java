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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/03/02 16:28:04
 */
class LastTest extends SignalTester {

    @Test
    void last() {
        monitor(signal -> signal.last());

        assert main.emit(1, 2, 3, Complete).value(3);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.last());

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.last());

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void lastWithDefault() {
        monitor(signal -> signal.last("Default"));

        assert main.emit(1, 2, 3, Complete).value(3);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void completeWithDefault() {
        monitor(signal -> signal.last("Default"));

        assert main.emit(Complete).value("Default");
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void errorWithDefault() {
        monitor(signal -> signal.last("Default"));

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }
}