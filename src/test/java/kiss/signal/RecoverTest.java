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

import java.io.IOException;

import org.junit.Test;

import antibug.powerassert.PowerAssertOff;
import kiss.I;

/**
 * @version 2018/03/23 16:16:02
 */
public class RecoverTest extends SignalTester {

    @Test
    public void recover() {
        monitor(signal -> signal.recover("recover"));

        assert main.emit(Error).value("recover");
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    @PowerAssertOff
    public void recoverByType() {
        monitor(signal -> signal.recover(fail -> {
            System.out.println(fail);
            return fail.getClass().getSimpleName();
        }));

        assert main.emit(new IOException()).value("IOException");
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void recoverWhenMultiple() {
        monitor(signal -> signal.recoverWhen(fail -> I.signal("success", "to", "recover")));

        assert main.emit(Error).value("success", "to", "recover");
    }

    @Test
    public void recoverWhenWithDelay() {
        monitor(signal -> signal.recoverWhen(fail -> fail.delay(10, ms).mapTo("recover")));

        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.countObservers() == 1;
        assert await(15).value("recover");
        assert main.countObservers() == 1;
        assert main.emit(Error).value();
        assert main.countObservers() == 1;
        assert await(15).value("recover");
        assert main.countObservers() == 1;
    }

    @Test
    public void recoverWhenWithDelayAndLimit() {
        monitor(signal -> signal.recoverWhen(fail -> fail.take(2).delay(10, ms).mapTo("recover")));

        assert main.emit(Error).value();
        assert await(15).value("recover");
        assert main.emit(Error).value();
        assert await(15).value("recover");
        assert main.emit(Error).value();
        assert await(15).value();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void recoverWhenWithError() {
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
    public void recoverWhenWithComplete() {
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
