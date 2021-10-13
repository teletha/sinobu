/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.io.IOError;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;

class RetryTest extends SignalTester {

    @Test
    void retry() {
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
    void retryLimit() {
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
    void retryLimitZero() {
        monitor(signal -> signal.startWith("retry").retry(0));

        assert main.value("retry");
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void retryLimitNegative() {
        monitor(signal -> signal.startWith("retry").retry(-1));

        assert main.value("retry");
        assert main.emit("next will fail", Error).value("next will fail");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void retryWhenWithDelay() {
        monitor(signal -> signal.startWith("retry").retryWhen(fail -> fail.delay(delay, ms, scheduler)));

        assert main.value("retry");
        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.hasNoObserver();
        scheduler.await();
        assert main.value("retry");
        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.hasNoObserver();
        scheduler.await();
        assert main.value("retry");
        assert main.countObservers() == 1;
    }

    @Test
    void retryWhenWithDelayAndLimit() {
        monitor(signal -> signal.startWith("retry").retryWhen(fail -> fail.take(2).delay(delay, ms, scheduler)));

        assert main.value("retry");
        assert main.emit(Error).value();
        scheduler.await();
        assert main.value("retry");
        assert main.emit(Error).value();
        scheduler.await();
        assert main.value("retry");
        assert main.emit(Error).value();
        scheduler.await();
        assert main.value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void retryWhenWithError() {
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
    void retryWhenWithComplete() {
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
    void retryWhenImmediately() {
        monitor(() -> I.signal("start")
                .effect(log("Begin"))
                .map(errorFunction())
                .retryWhen(fail -> fail.take(3).effect(log("Retry")))
                .effect(log("Unreached"))
                .effectOnError(log("ErrorFinally")));

        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert checkLog("Begin").size() == 4;
        assert checkLog("Unreached").size() == 0;
        assert checkLog("Retry").size() == 3;
        assert checkLog("ErrorFinally").size() == 1;
    }

    @Test
    void retryWhenWithDelayImmediately() {
        monitor(1, () -> I.signal("start")
                .effect(log("Begin"))
                .map(errorFunction())
                .retryWhen(fail -> fail.delay(delay, ms, scheduler).take(3).effect(log("Retry")))
                .effect(log("Unreached"))
                .effectOnError(log("ErrorFinally")));

        scheduler.await();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert checkLog("Begin").size() == 4 : checkLog("Begin");
        assert checkLog("Unreached").size() == 0;
        assert checkLog("Retry").size() == 3;
        assert checkLog("ErrorFinally").size() == 1;
    }

    @Test
    void retryWhenNullNotifier() {
        Signal<Object> signal = I.signal();
        Signal<Object> retryWhen = signal.retryWhen(null);
        assert signal == retryWhen;
    }

    @Test
    void disposeRetry() {
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
    void retryThenMerge() {
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
    void retryIfType() {
        monitor(signal -> signal.startWith("retry").retryWhen(e -> e.as(IllegalAccessError.class)));

        assert main.value("retry");
        assert main.emit(IllegalAccessError.class).value("retry");
        assert main.emit(IllegalAccessError.class).value("retry");
        assert main.emit(IllegalAccessError.class).value("retry");
        assert main.isNotError();
        assert main.emit(Error, "will fail").value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void retryIfSuperType() {
        monitor(signal -> signal.startWith("retry").retryWhen(e -> e.as(Error.class)));

        assert main.value("retry");
        assert main.emit(Error.class).value("retry");
        assert main.emit(IllegalAccessError.class).value("retry");
        assert main.emit(IOError.class).value("retry");
        assert main.isNotError();
        assert main.emit(Exception.class, "will fail").value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void retryIfNullTypeAcceptsAnyType() {
        monitor(signal -> signal.startWith("retry").retryWhen(e -> e.as((Class[]) null)));

        assert main.value("retry");
        assert main.emit(Error.class).value("retry");
        assert main.emit(Exception.class).value("retry");
        assert main.emit(RuntimeException.class).value("retry");
        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void retryUntil() {
        monitor(signal -> signal.retryUntil(other.signal()));

        assert main.emit(Error.class, "Retry any error type").isEmmitted();
        assert main.emit(Exception.class, "Retry any error type").isEmmitted();
        assert main.emit(Throwable.class, "Retry any error type").isEmmitted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void retryUntilByType() {
        monitor(signal -> signal.retryWhen(e -> e.as(Error.class).takeUntil(other.signal())));
        assert main.emit(Error.class, "Error can retry").isEmmitted();
        assert main.emit(IOError.class, "Sub type can retry").isEmmitted();
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Exception.class, "Exception can't retry").value();
        assert main.isError();
        assert main.isDisposed();
    }
}