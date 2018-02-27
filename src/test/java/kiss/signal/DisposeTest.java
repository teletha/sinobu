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
 * @version 2017/04/15 11:39:04
 */
public class DisposeTest extends SignalTester {

    @Test
    public void disposeMerge() {
        monitor(() -> signal(1).merge(signal(10, 20)).effect(log1).take(2));

        assert log1.value(1, 10);
        assert main.value(1, 10);
        assert main.isCompleted();
    }
}
