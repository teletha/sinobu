/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/06 2:37:23
 */
public class FlatMapTest extends SignalTester {

    @Test
    public void flatMap() throws Exception {
        monitor(() -> signal(10, 20).flatMap(v -> signal(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test
    public void flatMapNull() throws Exception {
        monitor(() -> signal(1, 2).flatMap(null));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void throwError() throws Exception {
        monitor(() -> signal(1, 2).flatMap(errorFunction()));

        assert main.value();
        assert main.isError();
    }
}
