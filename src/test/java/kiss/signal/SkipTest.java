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

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/09/09 10:54:17
 */
public class SkipTest extends SignalTester {

    @Test
    public void skip() {
        monitor(int.class, signal -> signal.skip(value -> value % 2 == 0));

        assert main.emit(1, 2, 3, 4).value(1, 3);
        assert main.isNotCompleted();
    }

    @Test
    public void skipNull() {
        monitor(int.class, signal -> signal.skip((Predicate) null));

        assert main.emit(1, 2, 3, 4).value(1, 2, 3, 4);
        assert main.isNotCompleted();
    }

    @Test
    public void skipWithPrevious() {
        monitor(() -> signal(10, 11, 20, 21).skip(0, (prev, now) -> now - prev > 5));

        assert main.value(11, 21);
        assert main.isCompleted();
    }

    @Test
    public void skipWithPreviousNull() {
        monitor(() -> signal(10, 11, 20, 21).skip(0, (BiPredicate) null));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test
    public void skipByCount() throws Exception {
        monitor(int.class, signal -> signal.skip(2));

        assert main.emit(1, 2, 3, 4).value(3, 4);
        assert main.isNotCompleted();
    }

    @Test
    public void skipByTime() {
        monitor(signal -> signal.skipUntil(30, ms));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        await(30);
        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
    }

    @Test
    public void skipBySignal() {
        monitor(signal -> signal.skip(other.signal()));

        assert main.emit(1, 2).value(1, 2);
        other.emit(true);
        assert main.emit(1, 2).value();
        other.emit(false);
        assert main.emit(1, 2).value(1, 2);

        assert other.isNotDisposed();
        main.dispose();
        assert main.isNotCompleted();
        assert other.isDisposed();
    }

    @Test
    public void skipAt() throws Exception {
        monitor(() -> signal(0, 1, 2, 3, 4, 5).skipAt(index -> 3 < index));
        assert main.value(0, 1, 2, 3);

        monitor(() -> signal(0, 1, 2, 3, 4, 5).skipAt(index -> index % 2 == 0));
        assert main.value(1, 3, 5);
    }

    @Test
    public void skipUntilSignal() {
        monitor(signal -> signal.skipUntil(other.signal()));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        other.emit("start");
        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert other.isCompleted();
    }

    @Test
    public void skipUntilValueCondition() {
        monitor(int.class, signal -> signal.skipUntil(value -> value == 3));

        assert main.emit(1, 2).value();
        assert main.emit(3, 4).value(3, 4);
        assert main.isNotCompleted();
    }

    @Test
    public void skipWhileValueCondition() {
        monitor(int.class, signal -> signal.skipWhile(value -> value != 3));

        assert main.emit(1, 2).value();
        assert main.emit(3, 4).value(3, 4);
        assert main.isNotCompleted();
    }
}
