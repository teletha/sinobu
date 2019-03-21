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

import org.junit.jupiter.api.Test;

import kiss.I;

class RecurseTest extends SignalTester {

    @Test
    void recurse() {
        monitor(Integer.class, signal -> signal.recurse(v -> v + 1).take(5));

        assert main.emit(1).value(1, 2, 3, 4, 5);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void recurseDontThrowStackOverflowError() {
        assert I.signal(1).recurse(v -> v + 1).take(1234567).to().v == 1234567;
    }

    @Test
    void recurseAsynchronus() {
        monitor(Integer.class, signal -> signal.recurse(v -> v + 1, scheduler.in(5, ms)).take(5));

        assert main.emit(1).value();
        scheduler.await();
        assert main.value(1, 2, 3, 4, 5);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void recurseMap() {
        monitor(Integer.class, signal -> signal.recurseMap(s -> s.map(v -> v + 1)).take(5));

        assert main.emit(1).value(1, 2, 3, 4, 5);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void recurseMapAsynchronus() {
        monitor(Integer.class, signal -> signal.recurseMap(s -> s.map(v -> v + 1), scheduler.in(5, ms)).take(5));

        assert main.emit(1).value();
        scheduler.await();
        assert main.value(1, 2, 3, 4, 5);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void recurseMapAsynchronuslyWithAsynchronusComputation() {
        monitor(Integer.class, signal -> signal.recurseMap(s -> s.map(v -> v + 1).delay(10, ms, scheduler), scheduler.in(5, ms))
                .take(5));

        assert main.emit(1).value();
        scheduler.await();
        assert main.value(1, 2, 3, 4, 5);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
