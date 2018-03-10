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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.junit.Test;

/**
 * @version 2017/04/18 20:12:05
 */
public class RepeatTest extends SignalTester {

    @Test
    public void repeat() throws Exception {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat 1", Complete).value("success to repeat 1");
        assert main.isNotCompleted();
        assert main.emit("success to repeat 2", Complete).value("success to repeat 2");
        assert main.isNotCompleted();
        assert main.emit("success to repeat 3", Complete).value("success to repeat 3");
        assert main.isCompleted();
        assert main.emit("fail to repeat", Complete).value();
    }

    @Test
    public void repeatInfinite() {
        monitor(signal -> signal.skip(1).take(1).repeat());

        assert main.emit(1, 2).value(2);
        assert main.emit(3, 4).value(4);
        assert main.emit(5, 6).value(6);
        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    public void disposeRepeat() throws Exception {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat", Complete).value("success to repeat");
        assert main.isNotCompleted();

        main.dispose();
        assert main.emit("fail to repeat", Complete).value();
        assert main.isNotCompleted();
    }

    @Test
    public void repeatThenMerge() {
        monitor(signal -> signal.repeat().merge(other.signal()));

        // from main
        assert main.emit("skip", "take", Complete).value("skip", "take");
        assert main.emit("skip", "take", Complete).value("skip", "take");
        assert main.emit("skip", "take", Complete).value("skip", "take");

        // from other
        assert other.emit("external").value("external");

        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();

        // dispose
        main.dispose();
        assert main.emit("main is disposed so this value will be ignored").value();
        assert other.emit("other is disposed so this value will be ignored").value();

        assert main.isNotCompleted();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isDisposed();
    }

    @Test
    public void repeatIf() throws Exception {
        AtomicBoolean canRepeat = new AtomicBoolean(true);
        monitor(signal -> signal.repeatIf(canRepeat::get));

        assert main.emit(1, Complete).value(1);
        assert main.emit(2, Complete).value(2);
        assert main.emit(3, Complete).value(3);
        assert main.isNotCompleted();

        canRepeat.set(false);
        assert main.emit(1, Complete).value(1);
        assert main.emit(2, Complete).value();
        assert main.emit(3, Complete).value();
        assert main.isCompleted();
    }

    @Test
    public void repeatIfNull() throws Exception {
        monitor(() -> signal(1).effect(log1).repeatIf((BooleanSupplier) null));
        assert log1.value(1);
        assert main.value(1);
        assert main.isCompleted();
    }

    @Test
    public void repeatUntil() throws Exception {
        monitor(signal -> signal.repeatUntil(other.signal()));

        assert main.emit("success to repeat", Complete).value("success to repeat");
        assert main.emit("success to repeat", Complete).value("success to repeat");

        other.emit("never repeat");
        assert main.emit("last message", Complete).value("last message");
        assert main.emit("failt to repeat", Complete).value();
    }
}
