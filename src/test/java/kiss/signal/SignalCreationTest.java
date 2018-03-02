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

import java.util.Enumeration;
import java.util.stream.Stream;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/02 1:53:26
 */
public class SignalCreationTest extends SignalTester {

    @Test
    public void single() throws Exception {
        monitor(() -> signal(1));

        assert main.value(1);
        assert main.isCompleted();
    }

    @Test
    public void multi() throws Exception {
        monitor(() -> signal(1, 2, 3));

        assert main.value(1, 2, 3);
        assert main.isCompleted();
    }

    @Test
    public void empty() throws Exception {
        monitor(() -> signal());

        assert main.value();
        assert main.isCompleted();
    }

    @Test
    public void singleNull() throws Exception {
        monitor(() -> signal((String) null));

        assert main.value((String) null);
        assert main.isCompleted();
    }

    @Test
    public void multiNull() throws Exception {
        monitor(() -> signal(null, null, null));

        assert main.value(null, null, null);
        assert main.isCompleted();
    }

    @Test
    public void arrayNull() throws Exception {
        monitor(() -> signal((String[]) null));

        assert main.value();
        assert main.isCompleted();
    }

    @Test
    public void iterable() throws Exception {
        monitor(() -> signal(list(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void iterableNull() throws Exception {
        monitor(() -> signal((Iterable) null));

        assert main.value();
        assert main.isCompleted();
    }

    @Test
    public void enumeration() throws Exception {
        monitor(() -> signal(enume(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void enumerationNull() throws Exception {
        monitor(() -> signal((Enumeration) null));

        assert main.value();
        assert main.isCompleted();
    }

    @Test
    public void stream() throws Exception {
        monitor(1, () -> signal(stream(1, 2)));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    public void streamNull() throws Exception {
        monitor(() -> signal((Stream) null));

        assert main.value();
        assert main.isCompleted();
    }
}
