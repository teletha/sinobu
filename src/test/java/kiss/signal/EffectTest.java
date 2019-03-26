/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;
import kiss.WiseConsumer;
import kiss.WiseRunnable;

/**
 * @version 2018/03/21 22:56:42
 */
class EffectTest extends SignalTester {

    @Test
    void effectConsumer() {
        monitor(1, signal -> signal.effect(log1));

        assert main.emit(1).value(1);
        assert log1.value(1);
        assert main.emit(2, 3).value(2, 3);
        assert log1.value(2, 3);
    }

    @Test
    void effectRunnable() {
        monitor(1, signal -> signal.effect(() -> log1.accept("@")));

        assert main.emit(1).value(1);
        assert log1.value("@");
        assert main.emit(2, 3).value(2, 3);
        assert log1.value("@", "@");
    }

    @Test
    void effectOnError() {
        monitor(1, signal -> signal.effectOnError(log1::error));

        assert log1.isNotError();
        main.emit(Error);
        assert log1.isError();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void effectOnComplete() {
        monitor(signal -> signal.effectOnComplete(log1::complete));

        assert log1.isNotCompleted();
        main.emit(Complete);
        assert log1.isCompleted();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void effectOnCompleteForEachValue() {
        ArrayList<String> list = new ArrayList();

        monitor(1, String.class, signal -> signal.effectOnComplete(list::add));

        assert main.emit("A", "B").value("A", "B");
        assert list.size() == 0;
        assert main.emit(Complete).value();
        assert list.size() == 2;
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void effectOnTerminate() {
        // by complete
        monitor(signal -> signal.effectOnTerminate(log1::complete));
        assert log1.isNotCompleted();
        main.emit(Complete);
        assert log1.isCompleted();

        // by error
        monitor(signal -> signal.effectOnTerminate(log1::complete));
        assert log1.isNotCompleted();
        main.emit(Error);
        assert log1.isCompleted();
    }

    @Test
    void effectOnObserveByRunnable() {
        List<String> list = new ArrayList();
        Signal<String> signal = I.signal("1").effectOnObserve(() -> {
            list.add("subscribe");
        });

        assert list.isEmpty();
        signal.to();
        assert list.size() == 1;
        signal.to();
        assert list.size() == 2;
        signal.to();
        assert list.size() == 3;
    }

    @Test
    void effectOnObserveByNullRunnable() {
        Signal<String> signal = I.signal("1");
        assert signal == signal.effectOnObserve((WiseRunnable) null);
    }

    @Test
    void effectOnObserveByConsumer() {
        List<String> list = new ArrayList();
        Signal<String> signal = I.signal("1").effectOnObserve(disposer -> {
            list.add("subscribe");
        });

        assert list.isEmpty();
        signal.to();
        assert list.size() == 1;
        signal.to();
        assert list.size() == 2;
        signal.to();
        assert list.size() == 3;
    }

    @Test
    void effectOnObserveByNullConsumer() {
        Signal<String> signal = I.signal("1");
        assert signal == signal.effectOnObserve((WiseConsumer) null);
    }

    @Test
    void effectOnDispose() {
        monitor(signal -> signal.effectOnDispose(log1::complete));

        assert log1.isNotCompleted();
        main.dispose();
        assert log1.isCompleted();
    }

    @Test
    void acceptNull() {
        Signal<Integer> from = I.signal(0);
        assert from == from.effect((WiseRunnable) null);
        assert from == from.effect((WiseConsumer) null);
        assert from == from.effectOnComplete((WiseRunnable) null);
        assert from == from.effectOnError((WiseRunnable) null);
        assert from == from.effectOnError((WiseConsumer) null);
    }

    @Test
    void effectOnceRunnable() {
        monitor(1, signal -> signal.effectOnce(() -> log1.accept("once")));

        assert main.emit(1).value(1);
        assert log1.value("once");
        assert main.emit(2, 3).value(2, 3);
        assert log1.value();
    }

    @Test
    void effectOnceConsumer() {
        monitor(1, signal -> signal.effectOnce(log1));

        assert main.emit(1).value(1);
        assert log1.value(1);
        assert main.emit(2, 3).value(2, 3);
        assert log1.value();
    }

    @Test
    void effectOnceNullRunnable() {
        monitor(1, signal -> signal.effectOnce((WiseRunnable) null));

        assert main.emit(1).value(1);
        assert main.emit(2, 3).value(2, 3);
    }

    @Test
    void effectOnceNullConsumer() {
        monitor(1, signal -> signal.effectOnce((WiseConsumer) null));

        assert main.emit(1).value(1);
        assert main.emit(2, 3).value(2, 3);
    }
}
