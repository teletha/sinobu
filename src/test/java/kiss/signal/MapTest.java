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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;
import kiss.I;

/**
 * @version 2018/08/01 11:51:08
 */
class MapTest extends SignalTester {

    @Test
    void map() {
        monitor(() -> signal(1, 2).map(v -> v * 2));

        assert main.value(2, 4);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void mapErrorInFunction() {
        monitor(() -> signal(1, 2).map(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapError() {
        monitor(int.class, signal -> signal.map(v -> v * 2));

        assert main.emit(1, Error).value(2);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapComplete() {
        monitor(int.class, signal -> signal.map(v -> v * 2));

        assert main.emit(1, Complete).value(2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @ExpectThrow(NullPointerException.class)
    void mapNullConverter() {
        I.signal().map(null);
    }

    @Test
    void mapWithContext() {
        monitor(int.class, signal -> signal.map(AtomicInteger::new, (context, value) -> context.getAndIncrement() + value));

        assert main.emit(1).value(1);
        assert main.emit(2).value(3);
        assert main.emit(3).value(5);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void mapWithContextErrorInFunction() {
        monitor(int.class, signal -> signal.map(AtomicInteger::new, errorBiFunction()));

        assert main.emit(1).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapWithContextError() {
        monitor(int.class, signal -> signal.map(AtomicInteger::new, (context, value) -> context.getAndIncrement() + value));

        assert main.emit(1, Error).value(1);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapWithContextComplete() {
        monitor(int.class, signal -> signal.map(AtomicInteger::new, (context, value) -> context.getAndIncrement() + value));

        assert main.emit(1, Complete).value(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @ExpectThrow(NullPointerException.class)
    void mapWithContextNullConverter() {
        I.signal().map(AtomicInteger::new, null);
    }

    @Test
    void mapWithNullContext() {
        monitor(int.class, signal -> signal.map(null, (context, value) -> value));

        assert main.emit(1).value(1);
        assert main.emit(2).value(2);
        assert main.emit(3).value(3);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void mapTo() {
        monitor(signal -> signal.mapTo("ZZZ"));

        assert main.emit("A").value("ZZZ");
        assert main.emit("B").value("ZZZ");
        assert main.emit("C").value("ZZZ");
        assert main.emit((String) null).value("ZZZ");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void mapToNull() {
        monitor(signal -> signal.mapTo(null));

        assert main.emit("A").value((String) null);
        assert main.emit("B").value((String) null);
        assert main.emit("C").value((String) null);
        assert main.emit((String) null).value((String) null);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void mapToError() {
        monitor(signal -> signal.mapTo("ZZZ"));

        assert main.emit("A", Error).value("ZZZ");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapToComplete() {
        monitor(signal -> signal.mapTo("ZZZ"));

        assert main.emit("A", Complete).value("ZZZ");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void maps() {
        monitor(int.class, signal -> signal.maps((prev, now) -> prev + now));

        assert main.emit(1).value();
        assert main.emit(2).value(3);
        assert main.emit(3).value(5);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void mapsErrorInFunction() {
        monitor(int.class, signal -> signal.maps(errorBiFunction()));

        assert main.emit(1, 2).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapsError() {
        monitor(int.class, signal -> signal.maps((prev, now) -> prev + now));

        assert main.emit(1, 2, Error).value(3);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapsComplete() {
        monitor(int.class, signal -> signal.maps((prev, now) -> prev + now));

        assert main.emit(1, 2, Complete).value(3);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @ExpectThrow(NullPointerException.class)
    void mapsNull() {
        monitor(int.class, signal -> signal.maps(null));
    }

    @Test
    void mapsWithInitial() {
        monitor(signal -> signal.maps(1, (prev, now) -> prev + now));

        assert main.emit(1).value(2);
        assert main.emit(2).value(3);
        assert main.emit(3).value(5);
    }

    @Test
    void mapsWithInitialErrorInFunction() {
        monitor(signal -> signal.maps(1, errorBiFunction()));

        assert main.emit(1).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapsWithInitialError() {
        monitor(signal -> signal.maps(1, (prev, now) -> prev + now));

        assert main.emit(1, Error).value(2);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapsWithInitialComplete() {
        monitor(signal -> signal.maps(1, (prev, now) -> prev + now));

        assert main.emit(1, Complete).value(2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
