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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signaling;
import kiss.WiseFunction;

class ConcatMapTest extends SignalTester {

    @Test
    void value() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorInFunction() {
        monitor(() -> signal(1, 2).concatMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void innerComplete() {
        monitor(Integer.class, signal -> signal.concatMap(v -> signal(v).take(1)));

        assert main.emit(10, 20).value(10, 20);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void innerError() {
        monitor(Integer.class, signal -> signal.concatMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void rejectNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(() -> signal(1, 2).concatMap((WiseFunction) null));
        });
    }

    @Test
    void delayAndInterval() {
        List<Integer> internalProcess = new ArrayList();

        monitor(1, Integer.class, signal -> signal.concatMap(time -> signal(time, time + 1).delay(time, ms, scheduler)
                .interval(50, ms, scheduler)
                .effect(internalProcess::add)));

        main.emit(300, 200, 100);
        scheduler.await();
        assert main.value(300, 301, 200, 201, 100, 101);
        assert internalProcess.get(0) == 300;
        assert internalProcess.get(1) == 301;
        assert internalProcess.get(2) == 200;
        assert internalProcess.get(3) == 201;
        assert internalProcess.get(4) == 100;
        assert internalProcess.get(5) == 101;
    }

    @Test
    void fromFinitToInfinit() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).concatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.accept("ok");
        assert main.value("ok");
    }

    @Test
    void fromFinitToInfinitWithComplete() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).concatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.complete();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void fromFinitToInfinitWithError() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).concatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.error(new IllegalAccessError());
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }
}