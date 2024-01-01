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

import org.junit.jupiter.api.Test;

class AsTest extends SignalTester {

    @Test
    void as() {
        monitor(signal -> signal.as(Integer.class));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value();
        assert main.emit(-1.1D).value();
        assert main.emit(20L).value();
        assert main.emit("5000").value();
        assert main.emit((Integer) null).value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void multipleTypes() {
        monitor(signal -> signal.as(Integer.class, Float.class, Long.class));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value();
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value();
        assert main.emit((Integer) null).value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void primitive() {
        monitor(signal -> signal.as(double.class));

        assert main.emit(10).value();
        assert main.emit(2.1F).value();
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value();
        assert main.emit("5000").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void sparseTypes() {
        monitor(signal -> signal.as(new Class[] {null, Number.class, null}));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void nullType() {
        monitor(signal -> signal.as(new Class[] {null, null}));

        assert main.emit(10).value();
        assert main.emit(2.1F).value();
        assert main.emit(-1.1D).value();
        assert main.emit(20L).value();
        assert main.emit("5000").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void nullTypes() {
        monitor(signal -> signal.as((Class[]) null));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value("5000");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void emptyTypes() {
        monitor(signal -> signal.as(new Class[0]));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value("5000");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void noTypes() {
        monitor(signal -> signal.as());

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value("5000");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.as(Integer.class));

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.as(Integer.class));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}