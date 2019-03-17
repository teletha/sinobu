/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

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
        monitor(() -> Signal.empty());

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void never() {
        monitor(() -> Signal.never());

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
    void supplier() {
        monitor(() -> signal(() -> 1));

        assert main.value(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void supplierNull() {
        monitor(() -> signal((Supplier) null));

        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void interval() {
        monitor(() -> I.signal(0, 100, ms, scheduler).take(2));

        scheduler.mark().elapse(50, ms);
        assert main.value(0L);
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        scheduler.await();
        assert main.value(1L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void future() {
        monitor(() -> I.signal(CompletableFuture.completedFuture("ok")));

        assert main.value("ok");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void futureError() {
        monitor(() -> I.signal(CompletableFuture.failedFuture(new Error())));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void range() {
        monitor(() -> I.signalRange(0L, 5L));

        assert main.value(0L, 1L, 2L, 3L, 4L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void rangeWithStep() {
        monitor(() -> I.signalRange(0L, 3L, 2L));
        assert main.value(0L, 2L, 4L);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void constructWithObserverCollection() {
        List<Observer<String>> observers = new ArrayList();

        List<String> results = new ArrayList();
        Disposable disposer = new Signal<String>(observers).map(String::toUpperCase).to(results::add);

        observers.forEach(e -> e.accept("one"));
        assert results.get(0).equals("ONE");
        observers.forEach(e -> e.accept("two"));
        assert results.get(1).equals("TWO");

        // dispose
        disposer.dispose();

        observers.forEach(e -> e.accept("Disposed signal doesn't propagate event."));
        assert results.size() == 2;
    }
}
