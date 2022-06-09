/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

import kiss.WiseFunction;
import kiss.Ⅱ;

class PairTest extends SignalTester {

    private final WiseFunction<Ⅱ<String, String>, String> composer = v -> v.ⅰ + v.ⅱ;

    @Test
    void pair() {
        monitor(String.class, signal -> signal.pair().map(composer));

        assert main.emit("A").value("nullA");
        assert main.emit("B").value("AB");
        assert main.emit("C").value("BC");
        assert main.emit("D").value("CD");
        assert main.emit("E", "F", "G").value("DE", "EF", "FG");
    }

    @Test
    void initialValue() {
        monitor(String.class, signal -> signal.pair("Z").map(composer));

        assert main.emit("A").value("ZA");
        assert main.emit("B").value("AB");
        assert main.emit("C").value("BC");
        assert main.emit("D").value("CD");
        assert main.emit("E", "F", "G").value("DE", "EF", "FG");
    }

    @Test
    void complete() {
        monitor(String.class, signal -> signal.pair().map(composer));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(String.class, signal -> signal.pair().map(composer));

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}