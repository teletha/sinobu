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

/**
 * @version 2018/02/27 13:35:47
 */
class TimeoutTest extends SignalTester {

    @Test
    void timeout() throws Exception {
        monitor(signal -> signal.timeout(30, ms));

        assert main.emit("success").value("success");
        assert main.isNotError();
        await(15);
        assert main.emit("success").value("success");
        assert main.isNotError();
        await(15);
        assert main.emit("success").value("success");
        assert main.isNotError();
        await(15);
        assert main.emit("success").value("success");
        assert main.isNotError();
        await(15);
        assert main.emit("success").value("success");
        assert main.isNotError();
        await(15);
        assert main.emit("success").value("success");
        assert main.isNotError();
        await(15);
        assert main.emit("success").value("success");
        assert main.isNotError();
        assert main.isNotDisposed();
        await(50);
        assert main.emit("error").value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void delayNegative() throws Exception {
        monitor(signal -> signal.timeout(-10, ms));

        assert main.emit("success").value("success");
    }
}
