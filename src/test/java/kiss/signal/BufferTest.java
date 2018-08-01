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

import static java.util.concurrent.TimeUnit.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import kiss.WiseFunction;

/**
 * @version 2018/07/20 8:38:39
 */
class BufferTest extends SignalTester {

    private final WiseFunction<List<String>, String> composer = v -> v.stream().collect(Collectors.joining());

    @Test
    void size() {
        monitor(signal -> signal.buffer(2).map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value("AB");
        assert main.emit("C").value();
        assert main.emit("D").value("CD");
        assert main.emit("E", "F", "G").value("EF");
    }

    @Test
    void sizeWithRepeat() {
        monitor(signal -> signal.buffer(2).skip(1).take(1).repeat().map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value();
        assert main.emit("C").value();
        assert main.emit("D").value("CD");
        assert main.emit("E", "F", "G", "H").value("GH");
    }

    @Test
    void sizeAndInterval1() {
        monitor(signal -> signal.buffer(2, 1).map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value("AB");
        assert main.emit("C").value("BC");
        assert main.emit("D").value("CD");
        assert main.emit("E", "F").value("DE", "EF");
    }

    @Test
    void sizeAndInterval2() {
        monitor(signal -> signal.buffer(2, 3).map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value();
        assert main.emit("C").value("BC");
        assert main.emit("D").value();
        assert main.emit("E", "F").value("EF");
    }

    @Test
    void time() {
        monitor(signal -> signal.buffer(30, MILLISECONDS).map(composer));

        assert main.emit("A", "B").value();
        await(50);
        assert main.value("AB");
        assert main.emit("C", "D", "E").value();
        await(50);
        assert main.value("CDE");
    }

    @Test
    void signal() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        assert main.emit("A", "B").value();
        other.emit("OK");
        assert main.value("AB");
        assert main.emit("C", "D", "E").value();
        other.emit("OK");
        assert main.value("CDE");
        assert main.emit("F", null, "G").value();
        other.emit("OK");
        assert main.value("FnullG");
    }

    @Test
    void signalErrorFromMain() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        main.emit(Error);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void signalErrorFromOther() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        other.emit(Error);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isError();
        assert other.isDisposed();
    }

    @Test
    void signalCompleteFromMain() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        main.emit(Complete);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void signalCompleteFromMainWithRemainings() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        assert main.emit("A", "B", Complete).value("AB");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void signalCompleteFromOther() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        other.emit(Complete);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void signalCompleteFromOtherWithRemainings() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        assert main.emit("A", "B").value();
        assert other.emit(Complete).value("AB");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void all() {
        monitor(signal -> signal.buffer().map(composer));

        assert main.emit("A", "B", "C", "D").value();
        assert main.emit(Complete).value("ABCD");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void allError() {
        monitor(signal -> signal.buffer().map(composer));

        assert main.emit("A", "B", "C", "D").value();
        assert main.emit(Error).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }
}
