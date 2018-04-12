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

import java.io.IOError;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/03/25 11:20:57
 */
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
        monitor(signal -> signal.recover(null));
        assert main.emit(NoSuchFieldError.class).value((Object) null);
        assert main.isNotError();
        assert main.isNotDisposed();

        monitor(signal -> signal.recover(IOException.class, null));
        assert main.emit(IOException.class).value((Object) null);
        assert main.isNotError();
        assert main.isNotDisposed();
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
        monitor(signal -> signal.recoverWhen(fail -> fail.delay(10, ms).mapTo("recover")));

        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.countObservers() == 1;
        assert await().value("recover");
        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.countObservers() == 1;
        assert await().value("recover");
        assert main.countObservers() == 1;
    }

    @Test
    void recoverWhenWithDelayAndLimit() {
        monitor(signal -> signal.recoverWhen(fail -> fail.take(2).delay(10, ms).mapTo("recover")));

        assert main.emit(Error).value();
        assert await().value("recover");
        assert main.emit(Error).value();
        assert await().value("recover");
        assert main.emit(Error).value();
        assert await().value();
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
}
