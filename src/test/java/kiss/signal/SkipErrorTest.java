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

import java.io.IOError;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class SkipErrorTest extends SignalTester {

    @Test
    void skip() {
        monitor(signal -> signal.skipError());

        assert main.emit(Error.class, 1, 2).value(1, 2);
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(RuntimeException.class, 3, 4).value(3, 4);
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Exception.class, 5, 6).value(5, 6);
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.skipError());

        assert main.emit(Complete, 1, 2).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void value() {
        monitor(signal -> signal.skipError());

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void specifySingleType() {
        monitor(signal -> signal.skipError(Exception.class));

        main.emit(Exception.class, IOException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(RuntimeException.class, IllegalAccessException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(Error.class);
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void specifyMultipleTypes() {
        monitor(signal -> signal.skipError(Exception.class, IOError.class));

        main.emit(Exception.class, IOException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(RuntimeException.class, IllegalAccessException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(IOError.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(Error.class);
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void nullMeansAnyType() {
        monitor(signal -> signal.skipError((Class[]) null));

        main.emit(Exception.class, IOException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(RuntimeException.class, IllegalAccessException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(Error.class);
        assert main.isNotError();
        assert main.isNotDisposed();
    }
}