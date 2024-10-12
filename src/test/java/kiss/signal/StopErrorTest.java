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

import java.io.IOError;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class StopErrorTest extends SignalTester {

    @Test
    void stop() {
        monitor(signal -> signal.stopError());

        assert main.emit(Error.class, 1, 2).value();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.stopError());

        assert main.emit(Complete, 1, 2).value();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void value() {
        monitor(signal -> signal.stopError());

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void matchSingleType() {
        monitor(signal -> signal.stopError(Exception.class));

        main.emit(Exception.class);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void noMatchSingleType() {
        monitor(signal -> signal.stopError(Error.class));

        main.emit(Exception.class);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void matchMultipleTypes() {
        monitor(signal -> signal.stopError(Exception.class, IOError.class));

        main.emit(Exception.class, IOException.class);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void noMatchMultipleTypes() {
        monitor(signal -> signal.stopError(Exception.class, IOError.class));

        main.emit(LinkageError.class);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void nullMeansAnyType() {
        monitor(signal -> signal.stopError((Class[]) null));

        main.emit(Exception.class, IOException.class);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}