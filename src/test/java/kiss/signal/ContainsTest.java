/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class ContainsTest extends SignalTester {

    @Test
    void OK() {
        monitor(String.class, Boolean.class, signal -> signal.contains("OK"));

        assert main.emit("A", "B", "OK", "C").value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void NG() {
        monitor(String.class, Boolean.class, signal -> signal.contains("OK"));

        assert main.emit("A", "B", "C", Complete).value(false);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
