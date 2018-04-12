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
 * @version 2018/03/01 12:02:57
 */
class DelayTest extends SignalTester {

    @Test
    void delay() throws Exception {
        monitor(signal -> signal.delay(30, ms));

        assert main.emit("delay").value();
        assert await().value("delay");

        assert main.emit("one", "more").value();
        assert await().value("one", "more");
    }

    @Test
    void delayNegative() throws Exception {
        monitor(signal -> signal.delay(-10, ms));

        assert main.emit("delay").value("delay");
    }

    @Test
    void delayByCount1() throws Exception {
        monitor(signal -> signal.delay(1));

        assert main.emit("1").value();
        assert main.emit("2").value("1");
        assert main.emit("3").value("2");
    }

    @Test
    void delayByCount2() throws Exception {
        monitor(signal -> signal.delay(2));

        assert main.emit("1").value();
        assert main.emit("2").value();
        assert main.emit("3").value("1");
        assert main.emit("4").value("2");
    }
}
