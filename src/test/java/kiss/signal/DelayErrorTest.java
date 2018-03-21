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
 * @version 2018/03/21 12:17:16
 */
public class DelayErrorTest extends SignalTester {

    @Test
    public void delay() {
        monitor(signal -> signal.delayError());

        assert main.emit("error will be buffered", Error).size(1);
        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        assert main.emit("complete will be replaced by the stored error", Complete).size(1);
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    public void noError() {
        monitor(signal -> signal.delayError());

        assert main.emit("no error", Complete, "this is ignored").size(1);
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
