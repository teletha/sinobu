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

import org.junit.jupiter.api.Test;

class SkipCompleteTest extends SignalTester {

    @Test
    void complete() {
        monitor(signal -> signal.skipComplete());

        assert main.emit(Complete, 1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.skipComplete());

        assert main.emit(Error, 1, 2).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void value() {
        monitor(signal -> signal.skipComplete());

        assert main.emit(1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }
}