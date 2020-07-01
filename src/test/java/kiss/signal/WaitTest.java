/*
 * Copyright (C) 2020 Nameless Production Committee
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
    void waiting() {
        monitor(signal -> signal.wait(30, ms));

        assert main.emit("delay").value("delay");
    }

    @Test
    void error() {
        monitor(signal -> signal.wait(10, ms));

        assert main.emit("dispose", "by", Error).value("dispose", "by");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.wait(10, ms));

        assert main.emit("dispose", "by", Complete).value("dispose", "by");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void zeroTime() {
        monitor(signal -> signal.wait(0, ms));

        assert main.emit("zero time", "makes", "no effect").value("zero time", "makes", "no effect");
    }

    @Test
    void negativeTime() {
        monitor(signal -> signal.wait(-30, ms));

        assert main.emit("negative time", "makes", "no effect").value("negative time", "makes", "no effect");
    }

    @Test
    void unitNull() {
        monitor(signal -> signal.wait(10, null));

        assert main.emit("null unit", "makes", "no effect").value("null unit", "makes", "no effect");
    }
}