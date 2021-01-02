/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class SampleTest extends SignalTester {

    @Test
    void value() {
        monitor(signal -> signal.sample(other.signal()));

        assert main.emit(1, 2, 3).value();
        assert other.emit("NOW").value(3);
        assert main.emit(3, 2, 1).value();
        assert other.emit("NOW").value(1);

        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.sample(other.signal()));

        assert main.emit(1, 2, Complete).value();
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.sample(other.signal()));

        assert main.emit(1, 2, Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void otherComplete() {
        monitor(signal -> signal.sample(other.signal()));

        assert main.emit(1, 2).value();
        assert other.emit(Complete).value();

        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void otherError() {
        monitor(signal -> signal.sample(other.signal()));

        assert main.emit(1, 2).value();
        assert other.emit(Error).value();

        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isError();
        assert other.isDisposed();
    }
}