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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.WiseConsumer;
import kiss.WiseRunnable;

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
    void effectOnObserveUsingDisposer() {
        List<String> list = new ArrayList();
        Signaling<String> signaling = new Signaling();
        Signal<String> signal = signaling.expose.effectOnObserve(disposer -> {
            list.add("subscribe");
            disposer.add(() -> {
                list.add("dispose");
            });
        });

        assert list.isEmpty();
        Disposable disposer = signal.to(I.NoOP);
        assert list.size() == 1; // effect on subscribe
        signaling.accept("value");
        assert list.size() == 1; // no-effect on value signal

        disposer.dispose(); // effect on dispose
        assert list.size() == 2;
        disposer.dispose(); // no-effect on duplicated operation
        assert list.size() == 2;

        disposer = signal.to(I.NoOP);
        assert list.size() == 3; // effect on subscribe
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

    @Test
    void effectOnLifecycle() {
        List<String> list = new ArrayList();
        Signaling<String> signaling = new Signaling();
        Signal<String> signal = signaling.expose.effectOnLifecycle(disposer -> {
            list.add("subscribe");
            disposer.add(() -> {
                list.add("dispose");
            });
            return list::add;
        });

        assert list.isEmpty();
        Disposable disposer = signal.to(I.NoOP);
        assert list.size() == 1; // effect on subscribe
        signaling.accept("value");
        assert list.size() == 2; // effect on value stream

        disposer.dispose(); // effect on dispose
        assert list.size() == 3;
        disposer.dispose(); // no-effect on duplicated operation
        assert list.size() == 3;

        disposer = signal.to(I.NoOP);
        assert list.size() == 4; // effect on subscribe
        signaling.accept("value");
        assert list.size() == 5; // effect on value stream
    }
}