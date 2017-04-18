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
import kiss.SignalTester;

/**
 * @version 2017/04/01 21:41:59
 */
public class ConcatTest extends SignalTester {

    @Test
    public void signal() throws Exception {
        monitor(() -> signal(1, 2).concat(signal(3, 4)));

        assert main.value(1, 2, 3, 4);
        assert main.isCompleted();
    }

    @Test
    public void signalArray() throws Exception {
        monitor(() -> signal(1, 2).concat(signal(3, 4), signal(5, 6)));

        assert main.value(1, 2, 3, 4, 5, 6);
        assert main.isCompleted();
    }

    @Test
    public void signalNull() throws Exception {
        monitor(() -> signal(1, 2).concat((Signal) null));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void signalArrayNull() throws Exception {
        monitor(() -> signal(1, 2).concat((Signal[]) null));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void quitInTheMiddle() throws Exception {
        monitor(() -> signal(1, 2).concat(signal(3, 4)).effect(log1).take(2));

        assert log1.value(1, 2);
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
