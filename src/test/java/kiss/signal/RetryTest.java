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

import antibug.powerassert.PowerAssertOff;
import kiss.I;

/**
 * @version 2017/04/18 20:12:05
 */
public class RetryTest extends SignalTester {

    @Test
    public void retry() {
        monitor(signal -> signal.startWith("retry").retry());

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
    }

    @Test
    public void retryWithLimit() {
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
        assert main.isDisposed();
    }

    @Test
    public void retryWhenWithDelay() {
        monitor(signal -> signal.startWith("retry").retryWhen(fail -> fail.delay(10, ms)));

        assert main.value("retry");
        assert main.emit(Error).value();
        assert await(15).value("retry");
        assert main.emit(Error).value();
        assert await(15).value("retry");
    }

    @Test
    public void retryWhenWithDelayAndLimit() {
        monitor(signal -> signal.startWith("retry").retryWhen(fail -> fail.take(2).delay(10, ms)));

        assert main.value("retry");
        assert main.emit(Error).value();
        assert await(30).value("retry");
        assert main.emit(Error).value();
        assert await(30).value("retry");
        assert main.emit(Error).value();
        assert await(30).value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void retryWhenWithError() {
        monitor(signal -> signal.startWith("retry")
                .retryWhen(fail -> fail.flatMap(e -> e instanceof Error ? I.signal(e) : I.signalError(e))));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit(Error).value("retry");
        assert main.isNotError();
        assert main.emit("next will fail", IllegalStateException.class).value("next will fail");
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void retryWhenWithComplete() {
        monitor(signal -> signal.retryWhen(fail -> fail.take(2)));

        assert main.emit("first error will retry", Error).value("first error will retry");
        assert main.isNotError();
        assert main.emit("second error will retry", Error).value("second error will retry");
        assert main.isNotError();
        assert main.emit("third error will fail", Error).value("third error will fail");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void disposeRetry() {
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
    public void retryIf() {
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
        assert main.isDisposed();
    }

    @Test
    public void retryIfNull() {
        monitor(() -> signal(1).effect(log1).retryIf((BooleanSupplier) null));
        assert log1.value(1);
        assert main.value(1);
        assert main.isCompleted();
    }

    @Test
    public void retryUntil() {
        monitor(signal -> signal.startWith("retry").retryUntil(other.signal()));

        assert main.value("retry");
        assert main.emit(Error).value("retry");
        assert main.emit(Error).value("retry");
        assert main.isNotError();

        other.emit("never retry");
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isError();
        assert main.isDisposed();
    }
}
