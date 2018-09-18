/*
 * Copyright (C) 2018 Nameless Production Committee
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
 * @version 2018/09/18 19:04:38
 */
class WaitTest extends SignalTester {

    @Test
    void waiting() {
        monitor(signal -> signal.wait(30, ms));

        assert main.emit("delay").value("delay");
    }
}
