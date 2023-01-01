/*
 * Copyright (C) 2023 The SINOBU Development Team
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

import org.junit.jupiter.api.Test;

import antibug.Chronus;
import kiss.I;

class FisrtTest extends SignalTester {

    @Test
    void first() {
        monitor(signal -> signal.first());

        assert main.emit(1, 2, 3).value(1);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.first());

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.first());

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void stopSourceSignaleImmediately() {
        List<Integer> countingItemsOnSourceSignal = new ArrayList();

        assert I.signal(1, 2, 3).effect(countingItemsOnSourceSignal::add).first().to().exact() == 1;
        assert countingItemsOnSourceSignal.size() == 1;
    }

    @Test
    void dontStopFollowingSignalImmediately() {
        List<Integer> countingItemsOnSourceSignal = new ArrayList();

        List<Integer> result = I.signal(1, 2, 3)
                .effect(countingItemsOnSourceSignal::add)
                .first()
                .flatMap(v -> I.signal(v * 10, v * 100))
                .toList();

        assert result.size() == 2;
        assert result.get(0) == 10;
        assert result.get(1) == 100;
        assert countingItemsOnSourceSignal.size() == 1;
    }

    Chronus chronus = new Chronus();

    @Test
    void dontStopFollowingAsyncSignalImmediately() {
        List<Integer> countingItemsOnSourceSignal = new ArrayList();

        List<Integer> result = I.signal(1, 2, 3)
                .effect(countingItemsOnSourceSignal::add)
                .first()
                .flatMap(v -> I.signal(v * 10, v * 100).delay(20, ms, chronus))
                .toList();

        chronus.await();
        assert result.size() == 2;
        assert result.get(0) == 10;
        assert result.get(1) == 100;
        assert countingItemsOnSourceSignal.size() == 1;
    }
}