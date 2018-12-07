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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signaling;
import kiss.Variable;
import kiss.WiseFunction;

/**
 * @version 2018/12/07 16:21:11
 */
class ObserveTest extends SignalTester {

    class Host {
        Variable<Integer> var = Variable.empty();

        Host(int value) {
            var.set(value);
        }
    }

    @Test
    void value() {
        monitor(1, Host.class, Integer.class, signal -> signal.observe(v -> v.var));

        Host host1 = new Host(10);
        assert main.emit(host1).value(10);
        host1.var.set(20);
        assert main.value(20);

        Host host2 = new Host(30);
        assert main.emit(host2).value(30);
        host2.var.set(40);
        assert main.value(40);

        // no-relation
        host1.var.set(50);
        assert main.value();

        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(Complete);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorInFunction() {
        monitor(() -> signal(1, 2).switchMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void innerComplete() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v).take(1)));

        assert main.emit(10, 20).value(10, 20);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void innerError() {
        monitor(Integer.class, signal -> signal.switchMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void rejectNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(() -> signal(1, 2).switchMap((WiseFunction) null));
        });
    }

    @Test
    void delayAndInterval() {
        monitor(Integer.class, signal -> signal.switchMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(20, 21);
    }

    @Test
    void detail() {
        monitor(String.class, signal -> signal.switchMap(x -> x.equals("start other") ? other.signal() : another.signal()));

        assert main.emit("start other").size(0);
        assert other.emit("other is connected").size(1);
        assert another.emit("another is not connected yet").size(0);

        assert main.emit("start another").size(0);
        assert another.emit("another is connected").size(1);
        assert other.emit("other is disconnected").size(0);
        assert other.isDisposed();
        assert another.isNotDisposed();

        assert main.emit("start other").size(0);
        assert other.emit("other is connected again").size(1);
        assert another.emit("another is disconnected").size(0);
        assert other.isNotDisposed();
        assert another.isDisposed();

        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
        assert another.isDisposed();
    }

    @Test
    void fromFinitToInfinit() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).switchMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.accept("ok");
        assert main.value("ok");
    }

    @Test
    void fromFinitToInfinitWithComplete() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).switchMap(s -> s.expose));

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

        monitor(() -> I.signal(signaling).switchMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.error(new IllegalAccessError());
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }
}
