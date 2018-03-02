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

import kiss.SignalTester;

/**
 * @version 2018/03/02 13:55:14
 */
public class ContainsTest extends SignalTester {

    @Test
    public void contains() {
        monitor(String.class, Boolean.class, signal -> signal.contains("OK"));

        assert main.emit("A", "B", "C").value(false, false, false);
        assert main.emit("OK", "NG", "WOW").value(true, true, true);
        assert main.isNotCompleted();
    }
}
