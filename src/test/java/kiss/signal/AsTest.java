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
 * @version 2018/02/28 19:25:20
 */
public class AsTest extends SignalTester {

    @Test
    public void as() {
        monitor(signal -> signal.as(Integer.class));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value();
        assert main.emit(-1.1D).value();
        assert main.emit(20L).value();
        assert main.emit("5000").value();
    }
}
