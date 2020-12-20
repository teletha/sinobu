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

class DistinctTest extends SignalTester {

    @Test
    void distinct() {
        monitor(signal -> signal.distinct());

        assert main.emit("A", "B", "C", "C", "B", "A", "Z").value("A", "B", "C", "Z");
    }

    @Test
    void withRepeat() {
        monitor(signal -> signal.distinct().take(3).repeat());

        assert main.emit("A", "A", "B", "B", "C").value("A", "B", "C");
        assert main.emit("A", "A", "B", "B", "C").value("A", "B", "C");
        assert main.emit("A", "A", "B", "B", "C").value("A", "B", "C");
    }

    @Test
    void acceptNull() {
        monitor(signal -> signal.distinct());

        assert main.emit("A", null, null, "C").value("A", null, "C");
    }

    @Test
    void keySelector() {
        monitor(String.class, signal -> signal.distinct(v -> v.length()));

        assert main.emit("A", "BB", "CCC", "CC", null, "BBBB", "AA", "ZZZZ", null).value("A", "BB", "CCC", null, "BBBB");
    }
}