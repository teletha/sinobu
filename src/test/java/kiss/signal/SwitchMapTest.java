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

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2018/03/01 12:03:07
 */
public class SwitchMapTest extends SignalTester {

    @Test
    public void switchMap() {
        monitor(() -> signal(10, 20).switchMap(v -> signal(v, v + 1)));

        assert main.value(10, 11, 20, 21);
        assert main.isCompleted();
    }

    @Test(expected = NullPointerException.class)
    public void switchMapNull() {
        monitor(() -> signal(1, 2).switchMap(null));
    }

    @Test
    public void delayAndInterval() {
        monitor(Integer.class, signal -> signal.switchMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(20, 21);
    }

    @Test
    public void detail() {
        Subject<String, String> emitA = new Subject();
        Subject<String, String> emitB = new Subject();
        Subject<Integer, String> subject = new Subject<>(signal -> signal.switchMap(x -> x == 1 ? emitA.signal() : emitB.signal()));

        subject.emit(1); // connect to emitA
        assert subject.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("1A");
        assert subject.retrieve() == "1A";
        emitB.emit("1B"); // emitB has no relation yet
        assert subject.retrieve() == null;

        subject.emit(2); // connect to emitB and disconnect from emitA
        assert subject.retrieve() == null; // emitB doesn't emit value yet
        emitB.emit("2B");
        assert subject.retrieve() == "2B";
        emitA.emit("2A");
        assert subject.retrieve() == null;

        subject.emit(1); // reconnect to emitA and disconnect from emitB
        assert subject.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("3A");
        assert subject.retrieve() == "3A";
        emitB.emit("3B");
        assert subject.retrieve() == null;

        // test disposing
        subject.dispose();
        emitA.emit("Disposed");
        assert subject.retrieve() == null;
    }
}
