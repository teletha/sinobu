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
 * @version 2017/04/18 20:12:05
 */
public class RetryTest extends SignalTester {

    @Test
    public void retry() throws Exception {
        monitor(signal -> signal.retry(3));

        assert main.emit("error to retry 1", Error).value("error to retry 1");
        assert main.isNotError();
        assert main.emit("error to retry 2", Error).value("error to retry 2");
        assert main.isNotError();
        assert main.emit("error to retry 3", Error).value("error to retry 3");
        assert main.isNotError();
        assert main.emit("fail", Error).value("fail");
        assert main.isError();
    }

    @Test
    public void disposeRetry() throws Exception {
        monitor(signal -> signal.retry(3));

        assert main.emit("success to retry", Error).value("success to retry");
        assert main.isNotError();

        main.dispose();
        assert main.emit("fail to repeat", Error).value();
        assert main.isNotError();
    }

    @Test
    public void retryThenMerge() {
        monitor(signal -> signal.retry(3).merge(other.signal()));

        // from main
        assert main.emit("skip", "take", Error).value("skip", "take");
        assert main.emit("skip", "take", Error).value("skip", "take");
        assert main.emit("skip", "take", Error).value("skip", "take");

        // from other
        assert other.emit("external").value("external");

        assert main.isNotError();

        // dispose
        main.dispose();
        assert main.emit("main is disposed so this value will be ignored").value();
        assert other.emit("other is disposed so this value will be ignored").value();

        assert main.isNotError();
    }

    @Test
    public void retryIf() throws Exception {
        AtomicBoolean canRepeat = new AtomicBoolean(true);
        monitor(signal -> signal.retryIf(canRepeat::get));

        assert main.emit(1, Error).value(1);
        assert main.emit(2, Error).value(2);
        assert main.emit(3, Error).value(3);
        assert main.isNotError();

        canRepeat.set(false);
        assert main.emit(1, Error).value(1);
        assert main.emit(2, Error).value();
        assert main.emit(3, Error).value();
        assert main.isError();
    }

    @Test
    public void retryIfNull() throws Exception {
        monitor(() -> signal(1).effect(log1).retryIf((BooleanSupplier) null));
        assert log1.value(1);
        assert main.value(1);
        assert main.isCompleted();
    }

    @Test
    public void retryUntil() throws Exception {
        monitor(signal -> signal.retryUntil(other.signal()));

        assert main.emit("success to retry", Error).value("success to retry");
        assert main.emit("success to retry", Error).value("success to retry");

        other.emit("never retry");
        assert main.emit("last message", Error).value("last message");
        assert main.emit("failt to retry", Error).value();
    }
}
