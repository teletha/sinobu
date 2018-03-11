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
 * @version 2018/03/11 16:03:18
 */
public class ThrottleTest extends SignalTester {

    @Test
    public void throttle() {
        monitor(1, signal -> signal.throttle(30, ms));

        assert main.emit("success").value("success");
        await(10);
        assert main.emit("fail in 10 ms").value();
        await(10);
        assert main.emit("fail in 20 ms").value();
        await(20);
        assert main.emit("success").value("success");
        await(15);
        assert main.emit("fail in 15 ms").value();
        await(20);
        assert main.emit("success").value("success");
    }
}
