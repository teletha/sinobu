/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/03/23 17:17:56
 */
public class SkipErrorTest extends SignalTester {

    @Test
    public void error() {
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
    public void errorSpecifiec() {
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
    public void acceptNull() {
        monitor(signal -> signal.skipError(null));

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
