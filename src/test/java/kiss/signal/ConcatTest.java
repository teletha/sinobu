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

import kiss.Signal;

/**
 * @version 2018/06/22 9:11:51
 */
class ConcatTest extends SignalTester {

    @Test
    void signal() {
        monitor(() -> signal(1, 2).concat(signal(3, 4)));

        assert main.value(1, 2, 3, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void signalArray() {
        monitor(() -> signal(1, 2).concat(signal(3, 4), signal(5, 6)));

        assert main.value(1, 2, 3, 4, 5, 6);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void signalNull() {
        monitor(() -> signal(1, 2).concat((Signal) null));

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void signalArrayNull() {
        monitor(() -> signal(1, 2).concat((Signal[]) null));

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void quitInTheMiddle() {
        monitor(() -> signal(1, 2).concat(signal(3, 4)).effect(log1).take(2));

        assert log1.value(1, 2);
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.concat(other.signal()));

        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();

        main.emit(Complete);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();

        other.emit(Complete);
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isDisposed();
    }

    @Test
    void disposeWhileMainIsActive() {
        monitor(signal -> signal.concat(other.signal()));

        assert main.isNotDisposed();
        main.dispose();
        assert main.isDisposed();
    }

    @Test
    void disposeWhileOtherIsActive() {
        monitor(signal -> signal.concat(other.signal()));

        assert main.isNotDisposed();
        main.emit(Complete);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();

        main.dispose();
        assert main.isNotCompleted();
        assert other.isNotCompleted();
        assert main.isDisposed();
        assert other.isDisposed();
    }
}