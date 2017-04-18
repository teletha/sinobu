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

import java.util.stream.BaseStream;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/06 8:47:03
 */
public class StartWithTest extends SignalTester {

    @Test
    public void value() throws Exception {
        monitor(() -> signal(1, 2).startWith(0));
        assert main.value(0, 1, 2);
        assert main.isCompleted();

        monitor(() -> signal(1, 2).startWith(3, 4));
        assert main.value(3, 4, 1, 2);
        assert main.isCompleted();

        monitor(() -> signal(1, 2).startWith(3).startWith(4, 5));
        assert main.value(4, 5, 3, 1, 2);
        assert main.isCompleted();
    }

    @Test
    public void valueNull() throws Exception {
        monitor(() -> signal("1", "2").startWith((String) null));
        assert main.value(null, "1", "2");
        assert main.isCompleted();

        monitor(() -> signal("1", "2").startWith((String[]) null));
        assert main.value("1", "2");
        assert main.isCompleted();
    }

    @Test
    public void iterable() throws Exception {
        monitor(() -> signal(1, 2).startWith(list(-1, 0)));
        assert main.value(-1, 0, 1, 2);
        assert main.isCompleted();
    }

    @Test
    public void iterableError() throws Exception {
        monitor(() -> signal(1, 2).startWith(errorIterable()));
        assert main.value();
        assert main.isError();
    }

    @Test
    public void iterableNull() throws Exception {
        monitor(() -> signal(1, 2).startWith((Iterable) null));
        assert main.value(1, 2);
    }

    @Test
    public void stream() throws Exception {
        monitor(() -> signal(1, 2).startWith(stream(-1, 0)));
        assert main.value(-1, 0, 1, 2);
    }

    @Test
    public void streamNull() throws Exception {
        monitor(() -> signal(1, 2).startWith((BaseStream) null));
        assert main.value(1, 2);
    }
}
