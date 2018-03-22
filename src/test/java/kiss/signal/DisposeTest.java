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
 * @version 2018/03/22 15:55:34
 */
public class DisposeTest extends SignalTester {

    @Test
    public void disposeOnComplete() {
        monitor(signal -> signal);

        assert main.emit(Complete, "This value and next error will be ignored", Error).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void disposeOnError() {
        monitor(signal -> signal);

        assert main.emit(Error, "This value and next complete will be ignored", Complete).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}
