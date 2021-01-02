/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class ToggleTest extends SignalTester {

    @Test
    void trueOrFalse() {
        monitor(String.class, boolean.class, signal -> signal.toggle());

        assert main.emit("first value is true").value(true);
        assert main.emit("second value is false").value(false);
        assert main.emit("true/false value are switched alternately").value(true);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void falseOrTrue() {
        monitor(String.class, boolean.class, signal -> signal.toggle(false));

        assert main.emit("first value is false").value(false);
        assert main.emit("second value is true").value(true);
        assert main.emit("false/true value are switched alternately").value(false);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void value() {
        monitor(signal -> signal.toggle("A", "B"));

        assert main.emit("first value is A").value("A");
        assert main.emit("second value is B").value("B");
        assert main.emit("A/B value are switched alternately").value("A");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void values() {
        monitor(signal -> signal.toggle("A", "B", "C"));

        assert main.emit("first value is A").value("A");
        assert main.emit("second value is B").value("B");
        assert main.emit("third value is C").value("C");
        assert main.emit("A/B/C value are switched alternately").value("A");
        assert main.emit("A/B/C value are switched alternately").value("B");
        assert main.emit("A/B/C value are switched alternately").value("C");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void acceptNull() {
        monitor(signal -> signal.toggle("A", null));

        assert main.emit("first value is A").value("A");
        assert main.emit("second value is null").value((Object) null);
        assert main.emit("A/B value are switched alternately").value("A");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void error() {
        monitor(boolean.class, signal -> signal.toggle());

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(boolean.class, signal -> signal.toggle());

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}