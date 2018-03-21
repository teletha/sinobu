/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.Test;

/**
 * @version 2018/03/11 15:10:49
 */
public class ScanTest extends SignalTester {

    @Test
    public void scan() {
        monitor(signal -> signal.scan(10, (accumulated, value) -> accumulated + value));

        assert main.emit(1).value(11); // 10 + 1
        assert main.emit(2).value(13); // 11 + 2
        assert main.emit(3).value(16); // 13 + 3
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    public void error() {
        monitor(signal -> signal.scan(10, errorBiFunction()));

        assert main.emit(1).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void complete() {
        monitor(signal -> signal.scan(10, (accumulated, value) -> accumulated + value));

        assert main.emit(1, Complete).value(11);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}