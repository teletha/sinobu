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

import org.junit.jupiter.api.Test;

class ThrottleTest extends SignalTester {

    @Test
    void throttle() {
        monitor(1, signal -> signal.throttle(50, ms));

        assert main.emit("success", "skip", "skip").value("success");
        scheduler.mark().elapse(10, ms).within(50, ms, () -> {
            assert main.emit("skip in 10 ms").value();
        });
        scheduler.elapse(20, ms).within(50, ms, () -> {
            assert main.emit("skip in 20 ms").value();
        });
        scheduler.elapse(50, ms);
        assert main.emit("success").value("success");
        scheduler.mark().elapse(10, ms).within(50, ms, () -> {
            assert main.emit("skip in 10 ms").value();
        });
        scheduler.elapse(50, ms);
        assert main.emit("success").value("success");
    }

    @Test
    void error() {
        monitor(signal -> signal.throttle(10, ms));

        assert main.emit("dispose", "by", Error).value("dispose");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void zeroTime() {
        monitor(signal -> signal.throttle(0, ms));

        assert main.emit("zero time", "makes", "no effect").value("zero time", "makes", "no effect");
    }

    @Test
    void negativeTime() {
        monitor(signal -> signal.throttle(-30, ms));

        assert main.emit("negative time", "makes", "no effect").value("negative time", "makes", "no effect");
    }

    @Test
    void unitNull() {
        monitor(signal -> signal.throttle(10, null));

        assert main.emit("null unit", "makes", "no effect").value("null unit", "makes", "no effect");
    }
}