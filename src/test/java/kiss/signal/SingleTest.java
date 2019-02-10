/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/07/20 10:10:56
 */
class SingleTest extends SignalTester {

    @Test
    void single() {
        monitor(signal -> signal.single());

        assert main.emit(1, Complete).value(1);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void tooMany() {
        monitor(signal -> signal.single());

        assert main.emit(1, 2, 3, 4).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void zero() {
        monitor(signal -> signal.single());

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
