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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KeyMapTest extends SignalTester {

    @Test
    void keyMap() {
        monitor(Integer.class, signal -> signal.keyMap(v -> {
            if (v == 1) {
                return other.signal();
            } else {
                return another.signal();
            }
        }).map(String::valueOf));

        // first line
        assert main.emit(1).value();
        other.emit("a");
        assert main.value("{1=a}");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // second line
        assert main.emit(2).value();
        another.emit("A");
        assert main.value("{1=a, 2=A}");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // first line again
        other.emit("b");
        assert main.value("{1=b, 2=A}");

        // second line again
        another.emit("B");
        assert main.value("{1=b, 2=B}");
    }

    @Test
    void error() {
        monitor(Integer.class, signal -> signal.keyMap(v -> {
            if (v == 1) {
                return other.signal();
            } else {
                return another.signal();
            }
        }).map(String::valueOf));

        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(Integer.class, signal -> signal.keyMap(v -> {
            if (v == 1) {
                return other.signal();
            } else {
                return another.signal();
            }
        }).map(String::valueOf));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void nullMapper() {
        Assertions.assertThrows(NullPointerException.class, () -> monitor(signal -> signal.keyMap(null)));
    }
}