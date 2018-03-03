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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @version 2018/02/28 14:34:31
 */
public class BufferTest extends SignalTester {

    private final Function<List<String>, String> composer = v -> v.stream().collect(Collectors.joining());

    @Test
    public void bySize() {
        monitor(signal -> signal.buffer(2).map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value("AB");
        assert main.emit("C").value();
        assert main.emit("D").value("CD");
        assert main.emit("E", "F", "G").value("EF");
    }

    @Test
    public void bySizeWithRepeat() {
        monitor(signal -> signal.buffer(2).skip(1).take(1).repeat().map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value();
        assert main.emit("C").value();
        assert main.emit("D").value("CD");
        assert main.emit("E", "F", "G", "H").value("GH");
    }

    @Test
    public void bySizeAndInterval1() {
        monitor(signal -> signal.buffer(2, 1).map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value("AB");
        assert main.emit("C").value("BC");
        assert main.emit("D").value("CD");
        assert main.emit("E", "F").value("DE", "EF");
    }

    @Test
    public void bySizeAndInterval2() {
        monitor(signal -> signal.buffer(2, 3).map(composer));

        assert main.emit("A").value();
        assert main.emit("B").value();
        assert main.emit("C").value("BC");
        assert main.emit("D").value();
        assert main.emit("E", "F").value("EF");
    }

    @Test
    public void byTime() {
        monitor(signal -> signal.buffer(30, MILLISECONDS).map(composer));

        assert main.emit("A", "B").value();
        await(50);
        assert main.value("AB");
        assert main.emit("C", "D", "E").value();
        await(50);
        assert main.value("CDE");
    }

    @Test
    public void bySignal() {
        monitor(signal -> signal.buffer(other.signal()).map(composer));

        assert main.emit("A", "B").value();
        other.emit("OK");
        assert main.value("AB");
        assert main.emit("C", "D", "E").value();
        other.emit("OK");
        assert main.value("CDE");
    }
}
