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
 * @version 2018/03/02 12:41:30
 */
public class AnyTest extends SignalTester {

    @Test
    public void any() {
        monitor(Integer.class, Boolean.class, signal -> signal.any(v -> v % 2 == 0));

        assert main.emit(1, 3, 5).value(false, false, false);
        assert main.emit(2, 4, 6).value(true, true, true);
        assert main.emit(1, 3, 5).value(true, true, true);
        assert main.isNotCompleted();
    }

    @Test
    public void acceptNull() {
        monitor(Integer.class, Boolean.class, signal -> signal.any(null));
        assert main.emit(1, 3, 5).value(true, true, true);
        assert main.isNotCompleted();
    }
}
