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

import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;

import kiss.WiseFunction;

/**
 * @version 2018/07/21 20:22:57
 */
class DiffTest extends SignalTester {

    @Test
    void diff() {
        monitor(signal -> signal.diff());

        assert main.emit("A").value("A");
        assert main.emit("B").value("B");
        assert main.emit("B").value();
        assert main.emit("A").value("A");
        assert main.emit("A").value();
        assert main.emit("B").value("B");
    }

    @Test
    void acceptNull() {
        monitor(signal -> signal.diff());

        assert main.emit("A").value("A");
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value();
        assert main.emit("A").value("A");
    }

    @Test
    void keySelector() {
        monitor(String.class, signal -> signal.diff(String::length));

        assert main.emit("A").value("A");
        assert main.emit("B").value();
        assert main.emit("C").value();
        assert main.emit("AA").value("AA");
        assert main.emit("BB").value();
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value();
    }

    @Test
    void keySelectorNull() {
        monitor(String.class, signal -> signal.diff((WiseFunction) null));

        assert main.emit("A").value("A");
        assert main.emit("B").value("B");
        assert main.emit("C").value("C");
        assert main.emit("AA").value("AA");
        assert main.emit("BB").value("BB");
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value((String) null);
    }

    @Test
    void comparetor() {
        monitor(String.class, signal -> signal.diff((previous, now) -> previous.length() == now.length()));

        assert main.emit("A").value("A");
        assert main.emit("B").value();
        assert main.emit("C").value();
        assert main.emit("AA").value("AA");
        assert main.emit("BB").value();
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value();
    }

    @Test
    void comparetorNull() {
        monitor(String.class, signal -> signal.diff((BiPredicate) null));

        assert main.emit("A").value("A");
        assert main.emit("B").value("B");
        assert main.emit("C").value("C");
        assert main.emit("AA").value("AA");
        assert main.emit("BB").value("BB");
        assert main.emit((String) null).value((String) null);
        assert main.emit((String) null).value((String) null);
    }
}
