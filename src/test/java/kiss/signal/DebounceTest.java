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

        assert main.emit("A").value();
        scheduler.mark().elapse(20, ms).within(60, ms, () -> {
            assert main.emit("B").value();
        });
        scheduler.elapse(40, ms).within(80, ms, () -> {
            assert main.emit("C").value();
        });
        scheduler.elapse(60, ms).within(100, ms, () -> {
            assert main.emit("D").value();
        });
        assert main.emit("E").value();
        scheduler.await();
        assert main.value("E");

        assert main.emit("F", "G", "H").value();
        scheduler.await();
        assert main.value("H");
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
