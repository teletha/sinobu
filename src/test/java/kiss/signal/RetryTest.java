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

import kiss.I;

/**
 * @version 2017/04/18 20:12:05
 */
public class RetryTest extends SignalTester {

    @Test
    public void retry() throws Exception {
        monitor(signal -> signal.startWith("retry").retry(3));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
    }

    @Test
    public void retryWhenWithError() throws Exception {
        monitor(signal -> signal.startWith("retry")
                .retryWhen(fail -> fail.flatMap(e -> e instanceof Error ? I.signal(e) : I.signalError(e))));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit("next will fail", IllegalStateException.class).value("next will fail");
        assert main.isError();
        assert main.emit("next will fail", IllegalStateException.class).value("next will fail");
        assert main.isError();
    }

    @Test
    public void retryWhenWithComplete() throws Exception {
        monitor(signal -> signal.startWith("retry").retryWhen(fail -> fail.take(2)));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
    }

    @Test
    public void disposeRetry() throws Exception {
        monitor(signal -> signal.startWith("retry").retry(3));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();

        main.dispose();
        assert main.emit("next will be ignored", Error).value();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void retryThenMerge() {
        monitor(signal -> signal.startWith("retry").retry(3).merge(other.signal()));

        // from main
        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.emit(Error).value("retry");
        assert main.emit(Error).value("retry");

        // from other
        assert other.emit("external").value("external");

        assert main.isNotError();
        assert main.isNotDisposed();

        // dispose
        main.dispose();
        assert main.emit("main is disposed so this value will be ignored").value();
        assert other.emit("other is disposed so this value will be ignored").value();

        assert main.isNotError();
        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    public void retryIf() throws Exception {
        AtomicBoolean canRetry = new AtomicBoolean(true);
        monitor(signal -> signal.startWith("retry").retryIf(canRetry::get));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.emit(Error).value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();

        canRetry.set(false);
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
        assert main.emit("next will fail", Error).value("next will fail");
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
        monitor(signal -> signal.startWith("retry").retryUntil(other.signal()));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();

        other.emit("never retry");
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
    }
}
