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

import kiss.Signal;

/**
 * @version 2017/04/02 3:06:33
 */
public class RangeTest extends SignalTestBase {

    @Test
    public void range() throws Exception {
        monitor(() -> Signal.range(1, 3));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void rangeSame() throws Exception {
        monitor(() -> Signal.range(1, 1));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void rangeDesccendingOrder() throws Exception {
        monitor(() -> Signal.range(3, 1));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void step() throws Exception {
        monitor(() -> Signal.range(1, 2, 5));

        assert result.value(1, 3);
        assert result.completed();
    }

    @Test
    public void stepSame() throws Exception {
        monitor(() -> Signal.range(1, 2, 1));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void stepDesccendingOrder() throws Exception {
        monitor(() -> Signal.range(5, 2, 1));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void stepNegative() throws Exception {
        monitor(() -> Signal.range(5, -2, 1));

        assert result.value(5, 3);
        assert result.completed();
    }

    @Test(expected = IllegalArgumentException.class)
    public void stepZero() throws Exception {
        monitor(() -> Signal.range(5, 0, 1));
    }
}
