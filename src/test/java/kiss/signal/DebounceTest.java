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

import static java.util.concurrent.TimeUnit.*;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/03/02 9:47:18
 */
public class DebounceTest extends SignalTester {

    @Test
    public void debounce() {
        monitor(signal -> signal.debounce(30, MILLISECONDS));

        assert main.emit("A").value();
        await(10);
        assert main.emit("B").value();
        await(10);
        assert main.emit("C").value();
        await(10);
        assert main.emit("D").value();
        await(10);
        assert main.emit("E").value();
        await(10); // 10ms elapsed
        assert main.value();
        await(10); // 20ms elapsed
        assert main.value();
        await(15); // 35ms elapsed
        assert main.value("E");

        assert main.emit("F", "G", "H").value();
        await(50);
        assert main.value("H");
    }

    @Test
    public void withRepeat() {
        monitor(signal -> signal.debounce(10, MILLISECONDS).skip(1).take(1).repeat());

        assert main.emit("A", "B").value();
        await(15);
        assert main.emit("C", "D").value();
        await(15);
        assert main.emit("E", "F").value("D");
        await(15);
        assert main.emit("G", "H").value();
        await(15);
        assert main.emit("I", "J").value("H");
    }
}
