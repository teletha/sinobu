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

import org.junit.Test;

import antibug.powerassert.PowerAssertOff;
import kiss.I;
import kiss.Signal;
import kiss.SignalTester;

/**
 * @version 2018/03/01 23:49:49
 */
public class EffectTest extends SignalTester {

    @Test
    @PowerAssertOff
    public void effect() {
        monitor(signal -> signal.effect(log1));

        assert main.emit(1).value(1);
        assert log1.value(1);
        assert main.emit(2, 3).value(2, 3);
        assert log1.value(2, 3);
    }

    @Test
    public void effectNull() {
        Signal<Integer> from = I.signal(0);
        assert from == from.effect(null);
        assert from == from.effectOnComplete((Runnable) null);
        assert from == from.effectOnError(null);
    }

    @Test
    public void effectOnComplet() throws Exception {
        monitor(signal -> signal.effectOnComplete(log1::complete));

        assert log1.isNotCompleted();
        main.emit(Complete);
        assert log1.isCompleted();
    }

    @Test
    public void effectOnError() throws Exception {
        monitor(signal -> signal.effectOnError(log1::error));

        assert log1.isNotError();
        main.emit(Error);
        assert log1.isError();
    }
}
