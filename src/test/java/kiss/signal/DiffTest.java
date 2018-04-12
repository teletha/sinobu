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
 * @version 2018/02/28 19:25:20
 */
class DiffTest extends SignalTester {

    @Test
    void diff() {
        monitor(signal -> signal.diff());

        assert main.emit("A").value("A");
        assert main.emit("B").value("B");
        assert main.emit("B").value();
        assert main.emit("A").value("A");
        assert main.emit("A").value();
        assert main.emit("B").value("B");
    }

    @Test
    void acceptNull() {
        monitor(signal -> signal.diff());

        assert main.emit("A").value("A");
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value();
        assert main.emit("A").value("A");
    }
}
