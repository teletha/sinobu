/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/06 15:12:36
 */
public class DelayTest extends SignalTester {

    @Test
    public void delay() throws Exception {
        monitor(signal -> signal.delay(10, ms));

        assert emit("delay").value();
        assert await().value("delay");

        assert emit("one", "more").value();
        assert await().value("one", "more");
    }

    @Test
    public void delayNegative() throws Exception {
        monitor(signal -> signal.delay(-10, ms));

        assert emit("delay").value("delay");
    }
}
