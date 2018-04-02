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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/04/02 11:31:51
 */
class SignalCreationTest extends SignalTester {

    @Test
    void single() {
        monitor(() -> signal(1));

        assert main.value(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void multi() {
        monitor(() -> signal(1, 2, 3));

        assert main.value(1, 2, 3);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void empty() {
        monitor(() -> Signal.EMPTY);

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void never() {
        monitor(() -> Signal.NEVER);

        assert main.value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void singleNull() {
        monitor(() -> signal((String) null));

        assert main.value((String) null);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void multiNull() {
        monitor(() -> signal(null, null, null));

        assert main.value(null, null, null);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void arrayNull() {
        monitor(() -> signal((String[]) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void iterable() {
        monitor(() -> signal(list(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void iterableNull() {
        monitor(() -> signal((Iterable) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void enumeration() {
        monitor(1, () -> signal(enume(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void enumerationNull() {
        monitor(() -> signal((Enumeration) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void interval() {
        monitor(() -> I.signal(0, 25, ms).take(2));

        assert await(10).value(0L);
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        assert await(30).value(1L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void delay() {
        monitor(() -> I.signal(20, ms));

        assert await(10).value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        assert await(20).value(0L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void future() {
        monitor(() -> I.signal((Future) CompletableFuture.completedFuture("ok")));

        assert await().value("ok");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void futureError() {
        monitor(() -> I.signal((Future) CompletableFuture.failedFuture(new Error())));

        assert await().value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void futureDelay() {
        monitor(() -> I.signal((Future) CompletableFuture.supplyAsync(() -> "ok", CompletableFuture.delayedExecutor(10, ms))));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        assert await(30).value("ok");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void completableFuture() {
        monitor(() -> I.signal(CompletableFuture.completedFuture("ok")));

        assert await().value("ok");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void completableFutureError() {
        monitor(() -> I.signal(CompletableFuture.failedFuture(new Error())));

        assert await().value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void completableFutureDelay() {
        monitor(() -> I.signal(CompletableFuture.supplyAsync(() -> "ok", CompletableFuture.delayedExecutor(10, ms))));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        assert await(30).value("ok");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void range() {
        monitor(() -> I.signalRange(0, 5));

        assert main.value(0, 1, 2, 3, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void rangeWithStep() {
        monitor(() -> I.signalRange(0, 3, 2));
        assert main.value(0, 2, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void rangeLong() {
        monitor(() -> I.signalRange(0L, 5L));

        assert main.value(0L, 1L, 2L, 3L, 4L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void rangeLongWithStep() {
        monitor(() -> I.signalRange(0L, 3L, 2L));
        assert main.value(0L, 2L, 4L);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
