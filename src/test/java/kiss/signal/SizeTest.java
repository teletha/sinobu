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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import kiss.WiseFunction;

class SizeTest extends SignalTester {

    private final WiseFunction<List<String>, String> composer = v -> v.stream().collect(Collectors.joining());

    @Test
    void same() {
        monitor(signal -> signal.size(2).map(composer));

        assert main.emit("A", "B", Complete).value("AB");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void underflow() {
        monitor(signal -> signal.size(5).map(composer));

        assert main.emit("A", "B", Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void overflow() {
        monitor(signal -> signal.size(1).map(composer));

        assert main.emit("A", "B").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void zero() {
        monitor(String.class, List.class, signal -> signal.size(0).as(List.class));

        assert main.emit(Complete).value(new ArrayList());
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void negative() {
        monitor(signal -> signal.size(-2).map(composer));

        assert main.emit("A", "B", Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}