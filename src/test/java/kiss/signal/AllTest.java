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

/**
 * @version 2018/03/02 13:19:31
 */
public class AllTest extends SignalTester {

    @Test
    public void OK() {
        monitor(Integer.class, Boolean.class, signal -> signal.all(v -> v % 2 == 0));

        assert main.emit(2, 4, 6, 8, Complete).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void NG() {
        monitor(Integer.class, Boolean.class, signal -> signal.all(v -> v % 2 == 0));

        assert main.emit(2, 4, 5, 6).value(false);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test(expected = NullPointerException.class)
    public void acceptNull() {
        monitor(Integer.class, Boolean.class, signal -> signal.all(null));
    }
}