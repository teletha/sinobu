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

import org.junit.Test;

/**
 * @version 2018/03/11 13:26:35
 */
public class StartWithTest extends SignalTester {

    @Test
    public void value() {
        monitor(() -> signal(1, 2).startWith(0));
        assert main.value(0, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();

        monitor(() -> signal(1, 2).startWith(3, 4));
        assert main.value(3, 4, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();

        monitor(() -> signal(1, 2).startWith(3).startWith(4, 5));
        assert main.value(4, 5, 3, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void valueNull() {
        monitor(() -> signal("1", "2").startWith((String) null));
        assert main.value(null, "1", "2");
        assert main.isCompleted();
        assert main.isDisposed();

        monitor(() -> signal("1", "2").startWith((String[]) null));
        assert main.value("1", "2");
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void iterable() {
        monitor(() -> signal(1, 2).startWith(list(-1, 0)));
        assert main.value(-1, 0, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void iterableError() {
        monitor(() -> signal(1, 2).startWith(errorIterable()));
        assert main.value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    public void iterableNull() {
        monitor(() -> signal(1, 2).startWith((Iterable) null));
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void enumerable() {
        monitor(1, () -> signal(3, 4).startWith(enume(0, 1, 2)));
        assert main.value(0, 1, 2, 3, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    public void enumerableError() {
        monitor(() -> signal(1, 2).startWith(errorEnumeration()));
        assert main.value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    public void enumerableNull() {
        monitor(() -> signal(1, 2).startWith((Enumeration) null));
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
