/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;

class OrTest extends SignalTester {

    @Test
    void value() {
        monitor(signal -> signal.or("otherwise"));

        assert main.emit("some", Complete).value("some");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.or("otherwise"));

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.or("otherwise"));

        assert main.emit(Complete).value("otherwise");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void supplier() {
        monitor(signal -> signal.or(() -> "otherwise"));

        assert main.emit("some", Complete).value("some");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void signal() {
        monitor(signal -> signal.or(I.signal("otherwise")));

        assert main.emit("some", Complete).value("some");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void multiple() {
        monitor(String.class, signal -> signal.or(I.signal()).or("otherwise"));

        assert main.emit(Complete).value("otherwise");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void nullValue() {
        monitor(String.class, signal -> signal.or((String) null));

        assert main.emit(Complete).value((String) null);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void nullSupplier() {
        monitor(String.class, signal -> signal.or((Supplier) null));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void nullSignal() {
        monitor(String.class, signal -> signal.or((Signal) null));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}