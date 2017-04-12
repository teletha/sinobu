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

import java.util.function.BooleanSupplier;

import org.junit.Test;

/**
 * @version 2017/04/03 11:04:09
 */
public class RepeatTest extends SignalTestBase {

    @Test
    public void repeat() throws Exception {
        monitor(signal -> signal.skip(1).take(1).repeat(6));

        assert emit(1, 2, 3, 4, 5, 6).value(2, 4, 6);
    }

    @Test
    public void repeatIf() throws Exception {
        monitor(() -> signal(1).effect(log1).repeatIf(() -> log1.size() < 3));
        assert log1.value(1, 1, 1);
        assert result.value(1, 1, 1);
        assert result.completed();
    }

    @Test
    public void repeatIfNull() throws Exception {
        monitor(() -> signal(1).effect(log1).repeatIf((BooleanSupplier) null));
        assert log1.value(1);
        assert result.value(1);
        assert result.completed();
    }

    public void repeatUntil() throws Exception {
        monitor(signal -> signal.delay(10, ms).take(1).repeat(2));

        assert emit(10, 20).value();
        assert await().value(10, 10);
    }
}
