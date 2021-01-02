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

import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;

class IsNotPredicateTest extends SignalTester {

    @Test
    void value() {
        monitor(Integer.class, Boolean.class, signal -> signal.isNot(v -> v % 2 == 0));

        assert main.emit(1, 2, 3).value(true, false, true);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void error() {
        monitor(Integer.class, Boolean.class, signal -> signal.isNot(v -> v % 2 == 0));

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(Integer.class, Boolean.class, signal -> signal.isNot(v -> v % 2 == 0));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void nill() {
        Assertions.assertThrows(NullPointerException.class, () -> I.signal(1, 2, 3).isNot((Predicate) null));
    }

    @Test
    void valuePredicate() {
        monitor(Integer.class, Boolean.class, signal -> signal.is(v -> v % 2 == 0));

        assert main.emit(1, 2, 3).value(false, true, false);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }
}