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

/**
 * @version 2018/03/11 12:07:59
 */
public class SwitchMapTest extends SignalTester {

    @Test
    public void value() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void complete() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void errorInSignal() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void errorInFunction() {
        monitor(() -> signal(1, 2).switchMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void otherComplete() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v).take(1)));

        assert main.emit(10, 20).value(10, 20);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void otherError() {
        monitor(Integer.class, signal -> signal.switchMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test(expected = NullPointerException.class)
    public void rejectNull() {
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
