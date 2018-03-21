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

import java.util.function.Consumer;

import org.junit.Test;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/03/21 22:56:42
 */
public class EffectTest extends SignalTester {

    @Test
    public void effectConsumer() {
        monitor(1, signal -> signal.effect(log1));

        assert main.emit(1).value(1);
        assert log1.value(1);
        assert main.emit(2, 3).value(2, 3);
        assert log1.value(2, 3);
    }

    @Test
    public void effectRunnable() {
        monitor(1, signal -> signal.effect(() -> log1.accept("@")));

        assert main.emit(1).value(1);
        assert log1.value("@");
        assert main.emit(2, 3).value(2, 3);
        assert log1.value("@", "@");
    }

    @Test
    public void effectOnComplet() {
        monitor(signal -> signal.effectOnComplete(log1::complete));

        assert log1.isNotCompleted();
        main.emit(Complete);
        assert log1.isCompleted();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void effectOnError() {
        monitor(1, signal -> signal.effectOnError(log1::error));

        assert log1.isNotError();
        main.emit(Error);
        assert log1.isError();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void acceptNull() {
        Signal<Integer> from = I.signal(0);
        assert from == from.effect((Runnable) null);
        assert from == from.effect((Consumer) null);
        assert from == from.effectOnComplete(null);
        assert from == from.effectOnError(null);
    }
}
