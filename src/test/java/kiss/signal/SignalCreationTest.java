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

import java.util.Enumeration;
import java.util.stream.Stream;

import org.junit.Test;

import kiss.I;
import kiss.SignalTester;

/**
 * @version 2017/04/02 1:53:26
 */
public class SignalCreationTest extends SignalTester {

    @Test
    public void single() throws Exception {
        monitor(() -> signal(1));

        assert result.value(1);
        assert result.completed();
    }

    @Test
    public void multi() throws Exception {
        monitor(() -> signal(1, 2, 3));

        assert result.value(1, 2, 3);
        assert result.completed();
    }

    @Test
    public void empty() throws Exception {
        monitor(() -> signal());

        assert result.value();
        assert result.completed();
    }

    @Test
    public void singleNull() throws Exception {
        monitor(() -> signal((String) null));

        assert result.value((String) null);
        assert result.completed();
    }

    @Test
    public void multiNull() throws Exception {
        monitor(() -> signal(null, null, null));

        assert result.value(null, null, null);
        assert result.completed();
    }

    @Test
    public void arrayNull() throws Exception {
        monitor(() -> signal((String[]) null));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void iterable() throws Exception {
        monitor(() -> signal(list(1, 2)));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void iterableNull() throws Exception {
        monitor(() -> signal((Iterable) null));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void enumeration() throws Exception {
        monitor(() -> signal(enume(1, 2)));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void enumerationNull() throws Exception {
        monitor(() -> signal((Enumeration) null));

        assert result.value();
        assert result.completed();
    }

    @Test
    public void stream() throws Exception {
        monitor(() -> signal(stream(1, 2)));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void streamNull() throws Exception {
        monitor(() -> signal((Stream) null));

        assert result.value();
        assert result.completed();
    }
}
