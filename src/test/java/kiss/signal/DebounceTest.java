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

import static java.util.concurrent.TimeUnit.*;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/04/02 9:37:09
 */
class DebounceTest extends SignalTester {

    @Test
    void debounce() {
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
        await(60); // 60ms elapsed
        assert main.value("E");

        assert main.emit("F", "G", "H").value();
        await(60); // 60ms elapsed
        assert main.value("H");
    }

    @Test
    void withRepeat() {
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
