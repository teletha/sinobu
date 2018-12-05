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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signaling;

/**
 * @version 2018/09/28 13:30:50
 */
class FlatMapTest extends SignalTester {

    @Test
    void value() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorInFunction() {
        monitor(() -> signal(1, 2).flatMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void innerComplete() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1, v + 2).take(2)));

        assert main.emit(10, 20, 30).value(10, 11, 20, 21, 30, 31);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void innerError() {
        monitor(Integer.class, signal -> signal.flatMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void rejectNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(() -> signal(1, 2).flatMap(null));
        });
    }

    @Test
    void delayAndInterval() {
        monitor(Integer.class, signal -> signal.flatMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(20, 40, 60, 21, 41, 61);
    }

    @Test
    void detail() {
        monitor(String.class, signal -> signal.flatMap(x -> x.equals("start other") ? other.signal() : another.signal()));

        assert main.emit("start other").size(0);
        assert other.emit("other is connected").size(1);
        assert another.emit("another is not connected yet").size(0);

        assert main.emit("start another").size(0);
        assert another.emit("another is connected").size(1);
        assert other.emit("other is also connected").size(1);

        assert main.isNotDisposed();
        assert other.isNotDisposed();
        assert another.isNotDisposed();

        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
        assert another.isDisposed();
    }

    @Test
    void enumeration() {
        monitor(() -> signal(10, 20).flatEnum(v -> enume(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test
    void enumerationNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(() -> signal(1, 2).flatEnum(null));
        });
    }

    @Test
    void array() {
        monitor(String.class, signal -> signal.flatArray(v -> v.split("")));

        assert main.emit("TEST").value("T", "E", "S", "T");
    }

    @Test
    void arrayNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(String.class, signal -> signal.flatArray(null));
        });
    }

    @Test
    void iterable() {
        monitor(String.class, signal -> signal.flatIterable(v -> Arrays.asList(v.split(""))));

        assert main.emit("TEST").value("T", "E", "S", "T");
    }

    @Test
    void iterableNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(String.class, signal -> signal.flatIterable(null));
        });
    }

    @Test
    void fromFinitToInfinit() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).flatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.accept("ok");
        assert main.value("ok");
    }

    @Test
    void fromFinitToInfinitWithComplete() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).flatMap(s -> s.expose));

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

        monitor(() -> I.signal(signaling).flatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.error(new IllegalAccessError());
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    class Host {
        Signaling<String> signal = new Signaling();
    }

    @Test
    void fromInfinitToInfinit() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host = new Host();
        assert main.emit(host).value();

        host.signal.accept("1");
        assert main.value("1");
        host.signal.accept("2");
        assert main.value("2");

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void fromInfinitToInfinitWithComplete() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host = new Host();
        assert main.emit(host).value();

        host.signal.complete();
        host.signal.accept("This value will be ignored");
        assert main.value();

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void fromInfinitToInfinitWithSourceComplete() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host = new Host();
        assert main.emit(host, Complete).value();

        host.signal.accept("This value will be accepted");
        assert main.value("This value will be accepted");

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        host.signal.complete();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void fromInfinitToInfinitWithError() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host = new Host();
        assert main.emit(host).value();

        host.signal.error(new java.lang.Error());
        host.signal.accept("This value will be ignored");
        assert main.value();

        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }
}
