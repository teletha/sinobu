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
import java.io.IOException;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;

class RecoverTest extends SignalTester {

    @Test
    void recover() {
        monitor(signal -> signal.recover("recover"));

        assert main.emit(Error).value("recover");
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void recoverByType() {
        monitor(signal -> signal.recover(IOException.class, "IO"));

        assert main.emit(IOException.class).value("IO");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Error.class).value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverByNullType() {
        monitor(signal -> signal.recover(null, "Any"));

        assert main.emit(IOException.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Exception.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(RuntimeException.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Error.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void recoverByNull() {
        monitor(signal -> signal.recover((String) null));
        assert main.emit(NoSuchFieldError.class).value((String) null);
        assert main.isNotError();
        assert main.isNotDisposed();

        monitor(signal -> signal.recover(IOException.class, (String) null));
        assert main.emit(IOException.class).value((String) null);
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void recoverSignal() {
        monitor(signal -> signal.recover(I.signal("recover")));

        assert main.emit(Error).value("recover");
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void recoverSignalByType() {
        monitor(signal -> signal.recover(IOException.class, I.signal("IO")));

        assert main.emit(IOException.class).value("IO");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Error.class).value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverSignalByNullType() {
        monitor(signal -> signal.recover(null, I.signal("Any")));

        assert main.emit(IOException.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Exception.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(RuntimeException.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Error.class).value("Any");
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void recoverSignalByNull() {
        monitor(signal -> signal.recover((Signal) null));
        assert main.emit(NoSuchFieldError.class).value();
        assert main.isNotError();
        assert main.isNotDisposed();

        monitor(signal -> signal.recover(IOException.class, (Signal) null));
        assert main.emit(IOException.class).value();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void recoverLimit() {
        monitor(signal -> signal.recover(2, "recover"));

        assert main.emit(Error.class).value("recover");
        assert main.emit(Error.class).value("recover");
        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverLimitZero() {
        monitor(signal -> signal.recover(0, "don't recover"));

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverLimitNegative() {
        monitor(signal -> signal.recover(-1, "don't recover"));

        assert main.emit(Error.class).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverWhenType() {
        monitor(signal -> signal.recoverWhen(IOError.class, fail -> fail.mapTo("recover")));

        assert main.emit(IOError.class).value("recover");
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(Error.class).value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverWhenWithDelay() {
        monitor(signal -> signal.recoverWhen(fail -> fail.delay(10, ms, scheduler).mapTo("recover")));

        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.countObservers() == 1;
        scheduler.await();
        assert main.value("recover");
        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.countObservers() == 1;
        scheduler.await();
        assert main.value("recover");
        assert main.countObservers() == 1;
    }

    @Test
    void recoverWhenWithDelayAndLimit() {
        monitor(signal -> signal.recoverWhen(fail -> fail.take(2).delay(10, ms, scheduler).mapTo("recover")));

        assert main.emit(Error).value();
        scheduler.await();
        assert main.value("recover");
        assert main.emit(Error).value();
        scheduler.await();
        assert main.value("recover");
        assert main.emit(Error).value();
        scheduler.await();
        assert main.value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverWhenWithError() {
        monitor(signal -> signal.recoverWhen(fail -> fail.flatMap(e -> e instanceof Error ? I.signal("recover") : I.signalError(e))));

        assert main.emit(Error).value("recover");
        assert main.isNotError();
        assert main.emit(Error).value("recover");
        assert main.isNotError();
        assert main.emit("next will fail", IllegalStateException.class).value("next will fail");
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverWhenWithComplete() {
        monitor(signal -> signal.recoverWhen(fail -> fail.take(2).mapTo("recover")));

        assert main.emit("first error will recover", Error).value("first error will recover", "recover");
        assert main.isNotError();
        assert main.emit("second error will recover", Error).value("second error will recover", "recover");
        assert main.isNotError();
        assert main.emit("third error will fail", Error).value("third error will fail");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void recoverWhenWithAfterEffect() {
        monitor(() -> I.signal("start")
                .effect(log("Begin"))
                .map(errorUnaryOperator())
                .recoverWhen(recover -> recover.take(3).mapTo("OK").effect(log("Recover")))
                .effect(log("End")));

        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert checkLog("Begin").size() == 1;
        assert checkLog("Recover").size() == 1;
        assert checkLog("End").size() == 1;
    }

    @Test
    void recoverWhenNullNotifier() {
        Signal<Object> signal = I.signal();
        Signal<Object> recover = signal.recoverWhen(null);
        assert signal == recover;
    }
}