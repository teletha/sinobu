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
 * @version 2018/03/02 12:09:21
 */
public class CountTest extends SignalTester {

    @Test
    public void count() {
        monitor(Long.class, signal -> signal.count());

        assert main.emit(10).value(1L);
        assert main.emit(20, 30, 40, 50).value(2L, 3L, 4L, 5L);
        assert main.emit(Complete).isCompleted();
    }
}
