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

import org.junit.Test;

/**
 * @version 2018/02/28 19:25:20
 */
public class DiffTest extends SignalTester {

    @Test
    public void diff() {
        monitor(signal -> signal.diff());

        assert main.emit("A").value("A");
        assert main.emit("B").value("B");
        assert main.emit("B").value();
        assert main.emit("A").value("A");
        assert main.emit("A").value();
        assert main.emit("B").value("B");
    }

    @Test
    public void Null() {
        monitor(signal -> signal.diff());

        assert main.emit("A").value("A");
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value();
        assert main.emit("A").value("A");
    }
}
