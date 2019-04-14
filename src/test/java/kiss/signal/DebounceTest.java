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

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class DebounceTest extends SignalTester {

    @Test
    void debounce() {
        monitor(signal -> signal.debounce(50, ms, scheduler));

        assert main.emit("only", "last event", "will be", "accepted").value();
        scheduler.await();
        assert main.value("accepted");
    }

    @RepeatedTest(100)
    void interval() {
        monitor(signal -> signal.interval(5, ms, scheduler).debounce(100, ms, scheduler));

        assert main.emit("only", "last event", "will be", "accepted").value();
        scheduler.await();
        assert main.value("accepted");
    }

    @Test
    void withRepeat() {
        monitor(signal -> signal.debounce(10, ms, scheduler).skip(1).take(1).repeat());

        assert main.emit("A", "B").value();
        scheduler.await();
        assert main.emit("C", "D").value();
        scheduler.await();
        assert main.emit("E", "F").value("D");
        scheduler.await();
        assert main.emit("G", "H").value();
        scheduler.await();
        assert main.emit("I", "J").value("H");
    }

    @Test
    void error() {
        monitor(signal -> signal.debounce(10, ms));

        assert main.emit("dispose by error", Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void zeroTime() {
        monitor(signal -> signal.debounce(0, ms));

        assert main.emit("zero time", "makes", "no effect").value("zero time", "makes", "no effect");
    }

    @Test
    void negativeTime() {
        monitor(signal -> signal.debounce(-30, ms));

        assert main.emit("negative time", "makes", "no effect").value("negative time", "makes", "no effect");
    }

    @Test
    void unitNull() {
        monitor(signal -> signal.debounce(10, null));

        assert main.emit("null unit", "makes", "no effect").value("null unit", "makes", "no effect");
    }
}
