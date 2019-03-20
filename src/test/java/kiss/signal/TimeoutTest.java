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

class TimeoutTest extends SignalTester {

    @Test
    void timeout() {
        monitor(signal -> signal.timeout(50, ms, scheduler));

        assert main.emit("success").value("success");
        assert main.isNotError();
        assert main.isNotDisposed();

        chronus.mark().elapse(10, ms);
        assert main.emit("success").value("success");
        assert main.isNotError();
        assert main.isNotDisposed();

        chronus.mark().elapse(10, ms);
        assert main.emit("success").value("success");
        assert main.isNotError();
        assert main.isNotDisposed();

        chronus.mark().elapse(10, ms);
        assert main.emit("success").value("success");
        assert main.isNotError();
        assert main.isNotDisposed();

        chronus.mark().elapse(10, ms);
        assert main.emit("success").value("success");
        assert main.isNotError();
        assert main.isNotDisposed();

        chronus.await();
        assert main.emit("error").value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void ignoreZeroTime() {
        monitor(signal -> signal.timeout(0, ms));

        assert main.emit("ignore zero time").value("ignore zero time");
    }

    @Test
    void ignoreNegativeTime() {
        monitor(signal -> signal.timeout(-10, ms));

        assert main.emit("ignore negative time").value("ignore negative time");
    }

    @Test
    void ignoreNullTimeUnit() {
        monitor(signal -> signal.timeout(40, null));

        assert main.emit("ignore null time unit").value("ignore null time unit");
    }
}
