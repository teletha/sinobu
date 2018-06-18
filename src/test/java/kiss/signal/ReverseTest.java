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

/**
 * @version 2018/06/18 19:03:12
 */
class ReverseTest extends SignalTester {

    @Test
    void reverse() {
        monitor(signal -> signal.reverse());

        assert main.emit("A", "B", "C").value();
        assert main.isNotCompleted();
        assert main.emit(Complete).value("C", "B", "A");
        assert main.isCompleted();
    }
}
