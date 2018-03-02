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
 * @version 2018/03/02 13:36:05
 */
public class IsErrorTest extends SignalTester {

    @Test
    public void isError() {
        monitor(Object.class, Boolean.class, signal -> signal.isError());

        assert main.emit("A", "B", "C").value();
        assert main.emit(Error.class).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
