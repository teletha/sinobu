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

class ThrottleTest extends SignalTester {

    @Test
    void throttle() {
        monitor(1, signal -> signal.throttle(50, ms));

        assert main.emit("success", "skip", "skip").value("success");
        scheduler.mark().elapseFromMark(10, ms).withinFromMark(50, ms, () -> {
            assert main.emit("skip in 10 ms").value();
        });
        scheduler.elapseFromMark(20, ms).withinFromMark(50, ms, () -> {
            assert main.emit("skip in 20 ms").value();
        });
        scheduler.elapseFromMark(50, ms);
        assert main.emit("success").value("success");
        scheduler.mark().elapseFromMark(10, ms).withinFromMark(50, ms, () -> {
            assert main.emit("skip in 10 ms").value();
        });
        scheduler.elapseFromMark(50, ms);
        assert main.emit("success").value("success");
    }
}
