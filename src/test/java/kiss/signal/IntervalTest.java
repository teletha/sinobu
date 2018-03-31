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
 * @version 2018/03/01 12:02:57
 */
public class IntervalTest extends SignalTester {

    @Test
    public void interval() throws Exception {
        monitor(signal -> signal.interval(20, ms));

        assert main.emit("A", "B", "C").value("A");
        assert await(25).value("B");
        assert await(25).value("C");
        await(20);
        assert main.emit("D").value("D");
    }

    @Test
    public void complete() throws Exception {
        monitor(signal -> signal.interval(20, ms));

        assert main.emit("A", "B", Complete).value("A");
        assert await(25).value("B");
        assert main.isNotCompleted();
        assert await(25).isCompleted();
    }
}
