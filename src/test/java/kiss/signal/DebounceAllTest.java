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

import java.util.List;

import org.junit.jupiter.api.Test;

class DebounceAllTest extends SignalTester {

    @Test
    void debounce() {
        monitor(signal -> signal.debounceAll(50, ms, scheduler));

        assert main.emit("only", "last event", "will be", "accepted").value();
        scheduler.await();
        assert main.value(List.of("only", "last event", "will be", "accepted"));
    }

    @Test
    void interval() {
        monitor(signal -> signal.interval(5, ms, scheduler).debounceAll(100, ms, scheduler));

        assert main.emit("only", "last event", "will be", "accepted").value();
        scheduler.await();
        assert main.value(List.of("only", "last event", "will be", "accepted"));
    }

    @Test
    void error() {
        monitor(signal -> signal.debounceAll(10, ms));

        assert main.emit("dispose by error", Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void zeroTime() {
        monitor(signal -> signal.debounceAll(0, ms));

        assert main.emit("zero time", "makes", "no effect").value(List.of("zero time"), List.of("makes"), List.of("no effect"));
    }

    @Test
    void negativeTime() {
        monitor(signal -> signal.debounceAll(-30, ms));

        assert main.emit("negative time", "makes", "no effect").value(List.of("negative time"), List.of("makes"), List.of("no effect"));
    }

    @Test
    void unitNull() {
        monitor(signal -> signal.debounceAll(10, null));

        assert main.emit("null unit", "makes", "no effect").value(List.of("null unit"), List.of("makes"), List.of("no effect"));
    }
}