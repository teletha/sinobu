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

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/03/03 19:54:42
 */
class SkipTest extends SignalTester {

    @Test
    void skip() {
        monitor(int.class, signal -> signal.skip(value -> value % 2 == 0));

        assert main.emit(1, 2, 3, 4).value(1, 3);
        assert main.isNotCompleted();
    }

    @Test
    void skipNull() {
        monitor(int.class, signal -> signal.skip((Predicate) null));

        assert main.emit(1, 2, 3, 4).value(1, 2, 3, 4);
        assert main.isNotCompleted();
    }

    @Test
    void skipWithPrevious() {
        monitor(() -> signal(10, 11, 20, 21).skip(0, (prev, now) -> now - prev > 5));

        assert main.value(11, 21);
        assert main.isCompleted();
    }

    @Test
    void skipWithPreviousNull() {
        monitor(() -> signal(10, 11, 20, 21).skip(0, (BiPredicate) null));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test
    void skipByCount() {
        monitor(int.class, signal -> signal.skip(2));

        assert main.emit(1, 2, 3, 4).value(3, 4);
        assert main.isNotCompleted();
    }

    @Test
    void skipByTime() {
        monitor(signal -> signal.skipUntil(30, ms));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        scheduler.mark().elapse(30, ms);
        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
    }

    @Test
    void skipByTimeWithInitialDelay() {
        monitor(signal -> signal.skipUntil(30, ms));

        scheduler.mark().elapse(30, ms);
        assert main.emit(1, 2).value(1, 2);
    }

    @Test
    void skipBySignal() {
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
    void skipByValue() {
        monitor(signal -> signal.skip(1, 3));

        assert main.emit(1, 2, 3).value(2);
        assert main.emit(1, 2, 3).value(2);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void skipByCollection() {
        monitor(signal -> signal.skip(I.set(1, 3)));

        assert main.emit(1, 2, 3).value(2);
        assert main.emit(1, 2, 3).value(2);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void skipAll() {
        monitor(signal -> signal.skipAll());
        assert main.emit(1, 2, 3).value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        monitor(signal -> signal.skipAll());
        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();

        monitor(signal -> signal.skipAll());
        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void skipAt() {
        monitor(() -> signal(0, 1, 2, 3, 4, 5).skipAt(index -> 3 < index));
        assert main.value(0, 1, 2, 3);

        monitor(() -> signal(0, 1, 2, 3, 4, 5).skipAt(index -> index % 2 == 0));
        assert main.value(1, 3, 5);
    }

    @Test
    void skipUntilOtherSignal() {
        monitor(signal -> signal.skipUntil(other.signal()));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotDisposed();

        other.emit("START");
        assert main.emit(1, 2, 3).value(1, 2, 3);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isDisposed();
    }

    @Test
    void skipUntilOtherSignalIsErrored() {
        monitor(signal -> signal.skipUntil(other.signal()));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotDisposed();

        other.emit(Error.class);
        assert main.emit(1, 2, 3).value(1, 2, 3);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isDisposed();
    }

    @Test
    void skipUntilOtherSignalIsCompleted() {
        monitor(signal -> signal.skipUntil(other.signal()));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotDisposed();

        other.emit(Complete);
        assert main.emit(1, 2, 3).value(1, 2, 3);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isDisposed();
    }

    @Test
    void skipUntilOtherSignalWithRepeat() {
        monitor(signal -> signal.skipUntil(other.signal()).take(1).repeat());

        assert main.emit(1, 2).value();
        other.emit("START");
        assert main.emit(1, 2, 3).value(1);
        other.emit("START");
        assert main.emit(1, 2, 3).value(1);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();
    }

    @Test
    void skipUntilValue() {
        monitor(signal -> signal.skipUntil(4));

        assert main.emit(2, 3, 4, 5).value(4, 5);
        assert main.emit(2, 3, 4, 5).value(2, 3, 4, 5);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void skipUntilValueNull() {
        monitor(signal -> signal.skipUntil((Object) null));

        assert main.emit(2, 3, null, 4, 5).value(null, 4, 5);
        assert main.emit(2, 3, 4, null).value(2, 3, 4, null);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void skipUntilValueWithRepeat() {
        monitor(signal -> signal.skipUntil(3).take(2).repeat());

        assert main.emit(2, 3, 4, 5, 6).value(3, 4);
        assert main.emit(2, 3, 4, 5, 6).value(3, 4);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void skipUntilValueCondition() {
        monitor(int.class, signal -> signal.skipUntil(value -> value == 3));

        assert main.emit(1, 2).value();
        assert main.emit(3, 4).value(3, 4);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void skipUntilValueConditionWithRepeat() {
        monitor(Integer.class, signal -> signal.skipUntil(value -> value % 3 == 0).take(2).repeat());

        assert main.emit(2, 3, 4, 5).value(3, 4);
        assert main.emit(2, 3, 4, 5).value(3, 4);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void skipWhileValueCondition() {
        monitor(int.class, signal -> signal.skipWhile(value -> value != 3));

        assert main.emit(1, 2).value();
        assert main.emit(3, 4).value(3, 4);
        assert main.isNotCompleted();
    }
}
