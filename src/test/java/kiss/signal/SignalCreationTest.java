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

import java.util.Enumeration;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/03/11 12:37:33
 */
public class SignalCreationTest extends SignalTester {

    @Test
    public void single() {
        monitor(() -> signal(1));

        assert main.value(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void multi() {
        monitor(() -> signal(1, 2, 3));

        assert main.value(1, 2, 3);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void empty() {
        monitor(() -> Signal.EMPTY);

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void never() {
        monitor(() -> Signal.NEVER);

        assert main.value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void singleNull() {
        monitor(() -> signal((String) null));

        assert main.value((String) null);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void multiNull() {
        monitor(() -> signal(null, null, null));

        assert main.value(null, null, null);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void arrayNull() {
        monitor(() -> signal((String[]) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void iterable() {
        monitor(() -> signal(list(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void iterableNull() {
        monitor(() -> signal((Iterable) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void enumeration() {
        monitor(1, () -> signal(enume(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void enumerationNull() {
        monitor(() -> signal((Enumeration) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void interval() {
        monitor(() -> I.signal(0, 25, ms).take(2));

        assert await(10).value(0L);
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        assert await(30).value(1L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void delay() {
        monitor(() -> I.signal(20, ms));

        assert await(10).value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        assert await(20).value(0L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void range() {
        monitor(() -> I.signalRange(0, 5));

        assert main.value(0, 1, 2, 3, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void rangeWithStep() {
        monitor(() -> I.signalRange(0, 3, 2));
        assert main.value(0, 2, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void rangeLong() {
        monitor(() -> I.signalRange(0L, 5L));

        assert main.value(0L, 1L, 2L, 3L, 4L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void rangeLongWithStep() {
        monitor(() -> I.signalRange(0L, 3L, 2L));
        assert main.value(0L, 2L, 4L);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
