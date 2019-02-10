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
import kiss.Signal;

/**
 * @version 2018/03/21 23:17:52
 */
class TakeTest extends SignalTester {

    @Test
    void take() {
        monitor(int.class, signal -> signal.take(value -> value % 2 == 0));

        assert main.emit(1, 2, 3, 4).value(2, 4);
        assert main.isNotCompleted();
    }

    @Test
    void takeValues() {
        monitor(int.class, signal -> signal.take(2, 3));

        assert main.emit(1, 2, 3, 4).value(2, 3);
        assert main.isNotCompleted();
    }

    @Test
    void takeCollection() {
        monitor(int.class, signal -> signal.take(I.set(2, 3)));

        assert main.emit(1, 2, 3, 4).value(2, 3);
        assert main.isNotCompleted();
    }

    @Test
    void takeNull() {
        monitor(int.class, signal -> signal.take((Predicate) null));

        assert main.emit(1, 2, 3, 4).value(1, 2, 3, 4);
        assert main.isNotCompleted();
    }

    @Test
    void takeWithPrevious() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (prev, now) -> now - prev > 5));

        assert main.value(10, 20);
        assert main.isCompleted();
    }

    @Test
    void takeWithPreviousNull() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (BiPredicate) null));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test
    void takeByCount() {
        monitor(int.class, signal -> signal.take(2));

        assert main.emit(1, 2, 3, 4).value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void takeBySignal() {
        monitor(signal -> signal.take(other.signal()));

        assert main.emit(1, 2).value();
        other.emit(true);
        assert main.emit(1, 2).value(1, 2);
        other.emit(false);
        assert main.emit(1, 2).value();
        other.emit(true);
        assert main.emit(1, 2).value(1, 2);

        assert main.isNotDisposed();
        assert other.isNotDisposed();
        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    void takeAt() {
        monitor(() -> signal(0, 1, 2, 3, 4, 5, 6).takeAt(index -> 3 < index));
        assert main.value(4, 5, 6);

        monitor(() -> signal(0, 1, 2, 3, 4, 5, 6).takeAt(index -> index % 2 == 0));
        assert main.value(0, 2, 4, 6);
    }

    @Test
    void takeUntilByValue() {
        monitor(signal -> signal.takeUntil(30));

        assert main.emit(10, 20, 30, 40).value(10, 20, 30);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void takeUntilByNullValue() {
        monitor(signal -> signal.takeUntil((Integer) null));

        assert main.emit(10, 20, null, 40).value(10, 20, null);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void takeUntilByValueWithRepeat() {
        monitor(signal -> signal.skip(1).takeUntil(30).repeat());

        assert main.emit(10, 20, 30).value(20, 30);
        assert main.emit(40, 50, 60).value(50, 60);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void takeUntilByTime() {
        monitor(signal -> signal.takeUntil(30, ms));

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        await(30);
        assert main.emit(1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void takeUntilOtherSignal() {
        monitor(signal -> signal.takeUntil(other.signal()));

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotDisposed();

        other.emit("STOP");
        assert main.emit(1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    void takeUntilOtherSignalIsComplete() {
        monitor(signal -> signal.takeUntil(other.signal()));

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotDisposed();

        other.emit(Complete);
        assert main.emit(1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    void takeUntilOtherSignalIsError() {
        monitor(signal -> signal.takeUntil(other.signal()));

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotDisposed();

        other.emit(Error.class);
        assert main.emit(1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    void takeUntilOtherSignalWithRepeat() {
        monitor(signal -> signal.skip(1).take(1).repeat().takeUntil(other.signal()));

        assert main.emit(1, 2).value(2);
        assert main.emit(3, 4).value(4);
        other.emit("STOP");
        assert main.emit(5, 6).value();
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    void tekeUntilNullSignal() {
        Signal<Integer> signal = I.signal(0);
        assert signal == signal.takeUntil((Signal) null);
    }

    @Test
    void takeUntilAndDispose() {
        monitor(signal -> signal.takeUntil(other.signal()));

        assert main.isNotDisposed();
        assert other.isNotDisposed();
        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    void takeUntilValueCondition() {
        monitor(int.class, signal -> signal.takeUntil(value -> value == 3));
        assert main.emit(1, 2, 3, 4, 5).value(1, 2, 3);
        assert main.isCompleted();
        assert main.isDisposed();

        // error
        monitor(int.class, signal -> signal.takeUntil(value -> value == 3));
        assert main.emit(Error.class, 1, 2).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();

        // complete
        monitor(int.class, signal -> signal.takeUntil(value -> value == 3));
        assert main.emit(Complete, 1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void takeUntilValueConditionWithRepeat() {
        monitor(int.class, signal -> signal.skip(1).takeUntil(value -> value % 3 == 0).repeat());

        assert main.emit(10, 20, 30).value(20, 30);
        assert main.emit(40, 50, 60).value(50, 60);
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void takeWhileValueCondition() {
        monitor(int.class, signal -> signal.takeWhile(value -> value != 3));
        assert main.emit(1, 2, 3, 4, 5).value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();

        // error
        monitor(int.class, signal -> signal.takeWhile(value -> value != 3));
        assert main.emit(Error.class, 1, 2).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();

        // complete
        monitor(int.class, signal -> signal.takeWhile(value -> value != 3));
        assert main.emit(Complete, 1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
