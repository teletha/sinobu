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

import org.junit.Test;

/**
 * @version 2018/03/26 11:17:26
 */
public class ConcatMapTest extends SignalTester {

    @Test
    public void value() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void complete() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void error() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void errorInFunction() {
        monitor(() -> signal(1, 2).concatMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void innerComplete() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v).take(1)));

        assert main.emit(10, 20).value(10, 20);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void innerError() {
        monitor(Integer.class, signal -> signal.concatMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test(expected = NullPointerException.class)
    public void rejectNull() {
        monitor(() -> signal(1, 2).concatMap(null));
    }

    @Test
    public void delayAndInterval() {
        monitor(Integer.class, signal -> signal.concatMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(60, 61, 40, 41, 20, 21);
    }
}
