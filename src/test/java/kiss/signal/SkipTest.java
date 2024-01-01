/*
 * Copyright (C) 2024 The SINOBU Development Team
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
    void skipByNoValue() {
        monitor(signal -> signal.skip());

        assert main.emit(1, 2, 3).value(1, 2, 3);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void skipByNullValue() {
        monitor(signal -> signal.skip((Object[]) null));

        assert main.emit(1, 2, 3).value(1, 2, 3);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void skipAt() {
        monitor(() -> signal(0, 1, 2, 3, 4, 5).skipAt(index -> 3 < index));
        assert main.value(0, 1, 2, 3);

        monitor(() -> signal(0, 1, 2, 3, 4, 5).skipAt(index -> index % 2 == 0));
        assert main.value(1, 3, 5);
    }

    @Test
    void skipUntilTime() {
        monitor(signal -> signal.skipUntil(I.schedule(30, ms, scheduler)));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        scheduler.await();
        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
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
    void skipUntilValueCondition() {
        monitor(int.class, signal -> signal.skipUntil(value -> 3 <= value));

        assert main.emit(1, 2).value();
        assert main.emit(3, 4).value(3, 4);
        assert main.emit(1, 2).value(1, 2);
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
        monitor(int.class, signal -> signal.skipWhile(value -> value == 3));

        assert main.emit(3, 3).value();
        assert main.emit(3, 1, 2, 3).value(1, 2, 3);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void skipIf() {
        monitor(1, int.class, signal -> signal.skipIf(value -> value % 2 == 0 ? I.signal() : I.signal(10)));
        assert main.emit(1, 2, 3, 4, 5, Complete).value(2, 4);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();

        // error
        monitor(1, int.class, signal -> signal.skipIf(value -> value % 2 == 0 ? I.signal() : I.signal(10)));
        assert main.emit(Error.class, 1, 2).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();

        // complete
        monitor(1, int.class, signal -> signal.skipIf(value -> value % 2 == 0 ? I.signal() : I.signal(10)));
        assert main.emit(Complete, 1, 2).value();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }
}