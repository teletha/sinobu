/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class PlugTest extends SignalTester {

    @Test
    void chain() {
        monitor(signal -> signal.plug(s -> s.take(3)));

        assert main.emit(10).value(10);
        assert main.emit(20).value(20);
        assert main.emit(30).value(30);
        assert main.emit(40).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}