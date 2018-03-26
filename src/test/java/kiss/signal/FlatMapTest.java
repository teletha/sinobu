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

import java.util.Arrays;

import org.junit.Test;

/**
 * @version 2018/03/26 11:17:41
 */
public class FlatMapTest extends SignalTester {

    @Test
    public void value() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void complete() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void error() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void errorInFunction() {
        monitor(() -> signal(1, 2).flatMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void innerComplete() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1, v + 2).take(2)));

        assert main.emit(10, 20, 30).value(10, 11, 20, 21, 30, 31);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void innerError() {
        monitor(Integer.class, signal -> signal.flatMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test(expected = NullPointerException.class)
    public void rejectNull() {
        monitor(() -> signal(1, 2).flatMap(null));
    }

    @Test
    public void delayAndInterval() {
        monitor(Integer.class, signal -> signal.flatMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(20, 40, 60, 21, 41, 61);
    }

    @Test
    public void detail() {
        monitor(String.class, signal -> signal.flatMap(x -> x.equals("start other") ? other.signal() : another.signal()));

        assert main.emit("start other").size(0);
        assert other.emit("other is connected").size(1);
        assert another.emit("another is not connected yet").size(0);

        assert main.emit("start another").size(0);
        assert another.emit("another is connected").size(1);
        assert other.emit("other is also connected").size(1);

        assert main.isNotDisposed();
        assert other.isNotDisposed();
        assert another.isNotDisposed();

        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
        assert another.isDisposed();
    }

    @Test
    public void enumeration() {
        monitor(() -> signal(10, 20).flatEnum(v -> enume(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void enumerationNull() {
        monitor(() -> signal(1, 2).flatEnum(null));
    }

    @Test
    public void array() {
        monitor(String.class, signal -> signal.flatArray(v -> v.split("")));

        assert main.emit("TEST").value("T", "E", "S", "T");
    }

    @Test(expected = NullPointerException.class)
    public void arrayNull() {
        monitor(String.class, signal -> signal.flatArray(null));
    }

    @Test
    public void iterable() {
        monitor(String.class, signal -> signal.flatIterable(v -> Arrays.asList(v.split(""))));

        assert main.emit("TEST").value("T", "E", "S", "T");
    }

    @Test(expected = NullPointerException.class)
    public void iterableNull() {
        monitor(String.class, signal -> signal.flatIterable(null));
    }
}
