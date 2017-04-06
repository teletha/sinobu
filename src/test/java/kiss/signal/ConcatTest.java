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

import static java.util.concurrent.TimeUnit.*;

import org.junit.Ignore;
import org.junit.Test;

import kiss.Signal;

/**
 * @version 2017/04/01 21:41:59
 */
public class ConcatTest extends SignalTestBase {

    @Test
    public void signal() throws Exception {
        monitor(() -> signal(1, 2).concat(signal(3, 4)));

        assert result.value(1, 2, 3, 4);
        assert result.completed();
    }

    @Test
    public void signalArray() throws Exception {
        monitor(() -> signal(1, 2).concat(signal(3, 4), signal(5, 6)));

        assert result.value(1, 2, 3, 4, 5, 6);
        assert result.completed();
    }

    @Test
    public void signalNull() throws Exception {
        monitor(() -> signal(1, 2).concat((Signal) null));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void signalArrayNull() throws Exception {
        monitor(() -> signal(1, 2).concat((Signal[]) null));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void quitInTheMiddle() throws Exception {
        monitor(() -> signal(1, 2).concat(signal(3, 4)).effect(log1).take(2));

        assert log1.value(1, 2);
        assert result.value(1, 2);
        assert result.completed();
        assert disposer.isDisposed();
    }

    @Test
    @Ignore
    public void map() throws Exception {
        monitor(() -> signal(10, 20).concatMap(v -> signal(v + 1, v + 2).interval(50, MILLISECONDS)));

        assert result.value(11, 12, 21, 22);
    }
}
