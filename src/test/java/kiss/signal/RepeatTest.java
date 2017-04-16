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

import java.util.function.BooleanSupplier;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/03 11:04:09
 */
public class RepeatTest extends SignalTester {

    @Test
    public void repeat() throws Exception {
        monitor(signal -> signal.repeat(2));

        assert emit("ok", Complete).value("ok");
        assert result.isNotCompleted();
        assert emit("success to repeat", Complete).value("success to repeat");
        assert result.isCompleted();
        assert emit("fail to repeat", Complete).value();
    }

    @Test
    public void repeatThenMerge() {
        monitor(signal -> signal.repeat().merge(other.signal()));

        // from main
        assert emit("skip", "take", Complete).value("skip", "take");
        assert emit("skip", "take", Complete).value("skip", "take");

        // from other
        assert other.emit("external").value("external");

        assert result.isNotCompleted();

        // dispose
        dispose();
        assert emit("main is disposed so this value will be ignored").value();
        assert other.emit("other is disposed so this value will be ignored").value();

        assert result.isNotCompleted();
    }

    @Test
    public void repeatIf() throws Exception {
        monitor(() -> signal(1).effect(log1).repeatIf(() -> log1.size() < 3));
        assert log1.value(1, 1, 1);
        assert result.value(1, 1, 1);
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

        assert emit(10, 20).value();
        assert await().value(10, 10);
    }
}
