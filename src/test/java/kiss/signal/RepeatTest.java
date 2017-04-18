/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/03 11:04:09
 */
public class RepeatTest extends SignalTester {

    @Test
    public void repeat() throws Exception {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat 1", Complete).value("success to repeat 1");
        assert result.isNotCompleted();
        assert main.emit("success to repeat 2", Complete).value("success to repeat 2");
        assert result.isNotCompleted();
        assert main.emit("success to repeat 3", Complete).value("success to repeat 3");
        assert result.isCompleted();
        assert main.emit("fail to repeat", Complete).value();
    }

    @Test
    public void disposeRepeat() throws Exception {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat", Complete).value("success to repeat");
        assert result.isNotCompleted();

        dispose();
        assert main.emit("fail to repeat", Complete).value();
        assert result.isNotCompleted();
    }

    @Test
    public void repeatThenMerge() {
        monitor(signal -> signal.repeat().merge(other.signal()));

        // from main
        assert main.emit("skip", "take", Complete).value("skip", "take");
        assert main.emit("skip", "take", Complete).value("skip", "take");

        // from other
        assert other.emit("external").value("external");

        assert result.isNotCompleted();

        // dispose
        dispose();
        assert main.emit("main is disposed so this value will be ignored").value();
        assert other.emit("other is disposed so this value will be ignored").value();

        assert result.isNotCompleted();
    }

    @Test
    public void repeatIf() throws Exception {
        AtomicBoolean canRepeat = new AtomicBoolean(true);
        monitor(signal -> signal.repeatIf(canRepeat::get));

        assert main.emit(1, Complete).value(1);
        assert main.emit(2, Complete).value(2);
        assert main.emit(3, Complete).value(3);
        assert result.isNotCompleted();

        canRepeat.set(false);
        assert main.emit(1, Complete).value(1);
        assert main.emit(2, Complete).value();
        assert main.emit(3, Complete).value();
        assert result.isCompleted();
    }

    @Test
    public void repeatIfNull() throws Exception {
        monitor(() -> signal(1).effect(log1).repeatIf((BooleanSupplier) null));
        assert log1.value(1);
        assert result.value(1);
        assert result.isCompleted();
    }

    public void repeatUntil() throws Exception {
        monitor(signal -> signal.delay(10, ms).take(1).repeat(2));

        assert main.emit(10, 20).value();
        assert await().value(10, 10);
    }
}
