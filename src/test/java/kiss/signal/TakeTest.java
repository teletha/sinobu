/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.junit.Test;

/**
 * @version 2017/04/07 1:57:35
 */
public class TakeTest extends SignalTestBase {

    @Test
    public void take() {
        monitor(int.class, signal -> signal.take(value -> value % 2 == 0));

        assert emit(1, 2, 3, 4).value(2, 4);
        assert result.isNotCompleted();
    }

    @Test
    public void takeNull() {
        monitor(int.class, signal -> signal.take((Predicate) null));

        assert emit(1, 2, 3, 4).value(1, 2, 3, 4);
        assert result.isNotCompleted();
    }

    @Test
    public void takeWithPrevious() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (prev, now) -> now - prev > 5));

        assert result.value(10, 20);
        assert result.completed();
    }

    @Test
    public void takeWithPreviousNull() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (BiPredicate) null));

        assert result.value(10, 11, 20, 21);
        assert result.completed();
    }

    @Test
    public void takeByCount() throws Exception {
        monitor(int.class, signal -> signal.take(2));

        assert emit(1, 2, 3, 4).value(1, 2);
        assert result.isCompleted();
    }

    @Test
    public void takeByTime() {
        monitor(signal -> signal.take(30, ms));

        assert emit(1, 2).value(1, 2);
        assert result.isNotCompleted();
        await(30);
        assert emit(1, 2).value();
        assert result.isCompleted();
    }

    @Test
    public void takeBySignal() {
        monitor(signal -> signal.take(condition.signal()));

        assert emit(1, 2).value();
        condition.emit(true);
        assert emit(1, 2).value(1, 2);
        condition.emit(false);
        assert emit(1, 2).value();

        assert condition.isNotDisposed();
        dispose();
        assert condition.isDisposed();
    }

    @Test
    public void takeAt() throws Exception {
        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> 3 < index));
        assert result.value(5, 6);

        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> index % 2 == 0));
        assert result.value(1, 3, 5);
    }

    @Test
    public void takeUntil() {
        Subject<String, String> condition = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.takeUntil(condition.signal()));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;

        condition.emit("start");
        assert subject.isCompleted();
        assert condition.isCompleted();
    }
}
