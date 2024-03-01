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

import kiss.Signal;
import org.junit.jupiter.api.Test;

class IsErredTest extends SignalTester {

    @Test
    void value() {
        monitor(Object.class, Boolean.class, Signal::isErred);

        assert main.emit(1, 2, 3).value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void error() {
        monitor(Object.class, Boolean.class, Signal::isErred);

        assert main.emit(1, 2, 3).value();
        assert main.emit(Error.class).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(Object.class, Boolean.class, Signal::isErred);

        assert main.emit(1, 2, 3).value();
        assert main.emit(Complete).value(false);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}