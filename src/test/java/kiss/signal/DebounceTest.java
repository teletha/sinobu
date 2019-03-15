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

class DebounceTest extends SignalTester {

    @Test
    void debounce() {
        monitor(signal -> signal.debounce(50, ms, scheduler));

        assert main.emit("only", "last event", "will be", "accepted").value();
        scheduler.await();
        assert main.value("accepted");
    }

    @Test
    void interval() {
        monitor(signal -> signal.interval(10, ms, scheduler).debounce(50, ms, scheduler));

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
}
