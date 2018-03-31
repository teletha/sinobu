/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/03/11 15:44:40
 */
public class SampleTest extends SignalTester {

    @Test
    public void value() {
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
    public void complete() {
        monitor(signal -> signal.sample(other.signal()));

        assert main.emit(1, 2, Complete).value();
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    public void error() {
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
    public void otherComplete() {
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
    public void otherError() {
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
