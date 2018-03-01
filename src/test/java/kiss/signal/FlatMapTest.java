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

import java.util.Arrays;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2018/03/01 12:03:07
 */
public class FlatMapTest extends SignalTester {

    @Test
    public void flatMap() {
        monitor(() -> signal(10, 20).flatMap(v -> signal(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNull() {
        monitor(() -> signal(1, 2).flatMap(null));
    }

    @Test
    public void delayAndInterval() {
        monitor(Integer.class, signal -> signal.flatMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(20, 40, 60, 21, 41, 61);
    }

    @Test
    public void detail() {
        Subject<String, String> emitA = new Subject();
        Subject<String, String> emitB = new Subject();
        Subject<Integer, String> subject = new Subject<>(signal -> signal.flatMap(x -> x == 1 ? emitA.signal() : emitB.signal()));

        subject.emit(1); // connect to emitA
        assert subject.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("1A");
        assert subject.retrieve() == "1A";
        emitB.emit("1B"); // emitB has no relation yet
        assert subject.retrieve() == null;

        subject.emit(2); // connect to emitB
        assert subject.retrieve() == null; // emitB doesn't emit value yet
        emitB.emit("2B");
        assert subject.retrieve() == "2B";
        emitA.emit("2A");
        assert subject.retrieve() == "2A";

        // test disposing
        subject.dispose();
        emitA.emit("Disposed");
        assert subject.retrieve() == null;
        emitB.emit("Disposed");
        assert subject.retrieve() == null;
    }

    @Test
    public void enumeration() {
        monitor(() -> signal(10, 20).flatEnum(v -> enume(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void enumerationNull() {
        monitor(() -> signal(1, 2).flatEnum(null));
    }

    @Test
    public void array() {
        monitor(String.class, signal -> signal.flatArray(v -> v.split("")));

        assert main.emit("TEST").value("T", "E", "S", "T");
    }

    @Test(expected = NullPointerException.class)
    public void arrayNull() {
        monitor(String.class, signal -> signal.flatArray(null));
    }

    @Test
    public void iterable() {
        monitor(String.class, signal -> signal.flatIterable(v -> Arrays.asList(v.split(""))));

        assert main.emit("TEST").value("T", "E", "S", "T");
    }

    @Test(expected = NullPointerException.class)
    public void iterableNull() {
        monitor(String.class, signal -> signal.flatIterable(null));
    }

    @Test
    public void throwError() {
        monitor(() -> signal(1, 2).flatMap(errorFunction()));

        assert main.value();
        assert main.isError();
    }
}
