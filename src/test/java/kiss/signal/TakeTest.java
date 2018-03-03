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

import kiss.I;

/**
 * @version 2018/03/03 7:05:51
 */
public class TakeTest extends SignalTester {

    @Test
    public void take() {
        monitor(int.class, signal -> signal.take(value -> value % 2 == 0));

        assert main.emit(1, 2, 3, 4).value(2, 4);
        assert main.isNotCompleted();
    }

    @Test
    public void takeValues() {
        monitor(int.class, signal -> signal.take(2, 3));

        assert main.emit(1, 2, 3, 4).value(2, 3);
        assert main.isNotCompleted();
    }

    @Test
    public void takeCollection() {
        monitor(int.class, signal -> signal.take(I.set(2, 3)));

        assert main.emit(1, 2, 3, 4).value(2, 3);
        assert main.isNotCompleted();
    }

    @Test
    public void takeNull() {
        monitor(int.class, signal -> signal.take((Predicate) null));

        assert main.emit(1, 2, 3, 4).value(1, 2, 3, 4);
        assert main.isNotCompleted();
    }

    @Test
    public void takeWithPrevious() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (prev, now) -> now - prev > 5));

        assert main.value(10, 20);
        assert main.isCompleted();
    }

    @Test
    public void takeWithPreviousNull() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (BiPredicate) null));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test
    public void takeByCount() throws Exception {
        monitor(int.class, signal -> signal.take(2));

        assert main.emit(1, 2, 3, 4).value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void takeBySignal() {
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
    public void takeAt() throws Exception {
        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> 3 < index));
        assert main.value(5, 6);

        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> index % 2 == 0));
        assert main.value(1, 3, 5);
    }

    @Test
    public void takeUntilByTime() {
        monitor(signal -> signal.takeUntil(30, ms));

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        await(30);
        assert main.emit(1, 2).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void takeUntilOtherSignal() {
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
    public void takeUntilOtherSignalIsComplete() {
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
    public void takeUntilOtherSignalIsError() {
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
    public void takeUntilOtherSignalWithRepeat() {
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
    public void takeUntilValueCondition() {
        monitor(int.class, signal -> signal.takeUntil(value -> value == 3));
        assert main.emit(1, 2, 3, 4, 5).value(1, 2, 3);
        assert main.isCompleted();
        assert main.isDisposed();

        // error
        monitor(int.class, signal -> signal.takeUntil(value -> value == 3));
        assert main.emit(Error.class, 1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        // complete
        monitor(int.class, signal -> signal.takeUntil(value -> value == 3));
        assert main.emit(Complete, 1, 2).value();
        assert main.isCompleted();
        assert main.isNotDisposed();
    }

    @Test
    public void takeWhileValueCondition() {
        monitor(int.class, signal -> signal.takeWhile(value -> value != 3));
        assert main.emit(1, 2, 3, 4, 5).value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();

        // error
        monitor(int.class, signal -> signal.takeWhile(value -> value != 3));
        assert main.emit(Error.class, 1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        // complete
        monitor(int.class, signal -> signal.takeWhile(value -> value != 3));
        assert main.emit(Complete, 1, 2).value();
        assert main.isCompleted();
        assert main.isNotDisposed();
    }
}
