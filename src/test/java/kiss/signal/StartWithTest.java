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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/06/26 18:32:38
 */
class StartWithTest extends SignalTester {

    @Test
    void value() {
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
    void valueNull() {
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
    void iterable() {
        monitor(() -> signal(1, 2).startWith(list(-1, 0)));
        assert main.value(-1, 0, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void iterableError() {
        monitor(() -> signal(1, 2).startWith(errorIterable()));
        assert main.value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void iterableNull() {
        monitor(() -> signal(1, 2).startWith((Iterable) null));
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void enumerable() {
        monitor(1, () -> signal(3, 4).startWith(enume(0, 1, 2)));
        assert main.value(0, 1, 2, 3, 4);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void enumerableError() {
        monitor(() -> signal(1, 2).startWith(errorEnumeration()));
        assert main.value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void enumerableNull() {
        monitor(() -> signal(1, 2).startWith((Enumeration) null));
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void signal() {
        monitor(signal -> signal.startWith(other.signal()));

        assert main.emit("other is not completed, so this value will ignored").size(0);
        assert other.emit("other is ", Complete).size(1);
        assert main.emit("main can signal").size(1);
        assert other.isCompleted();
        assert other.isDisposed();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void signalError() {
        monitor(signal -> signal.startWith(other.signal()));

        assert main.emit("other is not completed, so this value will ignored").size(0);
        assert other.emit("other is ", Error).size(1);
        assert main.emit("main can't signal").size(0);
        assert other.isNotCompleted();
        assert other.isError();
        assert other.isDisposed();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void withNull() {
        monitor(() -> signal("1", "2").startWithNull());
        assert main.value(null, "1", "2");
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
