/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.List;
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
        monitor(() -> I.signal());

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
        monitor(() -> I.schedule(0, 30, ms, false, scheduler)
                .take(4)
                .map(e -> System.currentTimeMillis())
                .buffer(2, 1)
                .map(values -> 30 <= values.get(1) - values.get(0)));

        assert main.isNotCompleted();
        assert main.isNotDisposed();

        scheduler.await(200, ms);
        assert main.value(true, true, true);
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