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

class WaitTest extends SignalTester {

    @Test
    void waitForTerminate() {
        monitor(signal -> signal.startWith(1, 2).delay(20, ms).take(2).waitForTerminate());

        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void waitForTerminateByError() {
        monitor(signal -> signal.startWith(1, 2).map(errorFunction()).delay(20, ms).waitForTerminate());

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void waitForTerminateByDispose() {
        monitor(signal -> signal.startWith(1).delay(20, ms).effectOnce(main::dispose).waitForTerminate());

        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}