/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class SkipErrorTest extends SignalTester {

    @Test
    void error() {
        monitor(signal -> signal.skipError());

        main.emit(Error.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(RuntimeException.class);
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(Exception.class);
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void errorSpecifiec() {
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
    void acceptNull() {
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