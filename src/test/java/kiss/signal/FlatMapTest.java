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
 * @version 2017/04/24 12:22:18
 */
public class FlatMapTest extends SignalTester {

    @Test
    public void flatMap() throws Exception {
        monitor(() -> signal(10, 20).flatMap(v -> signal(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNull() throws Exception {
        monitor(() -> signal(1, 2).flatMap(null));
    }

    @Test
    public void enumeration() throws Exception {
        monitor(() -> signal(10, 20).flatEnum(v -> enume(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void enumerationNull() throws Exception {
        monitor(() -> signal(1, 2).flatEnum(null));
    }

    @Test
    public void throwError() throws Exception {
        monitor(() -> signal(1, 2).flatMap(errorFunction()));

        assert main.value();
        assert main.isError();
    }
}
