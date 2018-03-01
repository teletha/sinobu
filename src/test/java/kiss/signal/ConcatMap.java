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

import antibug.powerassert.PowerAssertOff;
import kiss.SignalTester;

/**
 * @version 2018/03/01 12:03:07
 */
public class ConcatMap extends SignalTester {

    @Test
    public void concatMap() {
        monitor(() -> signal(10, 20).concatMap(v -> signal(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void concatMapNull() {
        monitor(() -> signal(1, 2).concatMap(null));
    }

    @Test
    @PowerAssertOff
    public void delayAndInterval() {
        monitor(Integer.class, signal -> signal.concatMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(60, 61, 40, 41, 20, 21);
    }
}
