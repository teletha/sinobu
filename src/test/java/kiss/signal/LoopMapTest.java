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

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/12/09 15:44:11
 */
class LoopMapTest extends SignalTester {

    @Test
    void value() {
        monitor(Integer.class, signal -> signal.loopMap(v -> I.signal(v + 1).take(i -> i < 10)));

        assert main.emit(1).value(10);
    }
}
