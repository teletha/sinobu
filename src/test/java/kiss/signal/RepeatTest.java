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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * @version 2017/04/03 11:04:09
 */
public class RepeatTest extends SignalTestBase {

    @Test
    public void repeatWhen() throws Exception {
        AtomicInteger total = new AtomicInteger();
        // monitor(() -> signal(1).repeat(3));
        monitor(1, () -> signal(1).effect(total::addAndGet).repeatWhen(() -> total.get() < 3));

        assert result.value(1, 1, 1);
    }
}
