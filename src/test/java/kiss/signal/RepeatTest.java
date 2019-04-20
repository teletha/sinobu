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

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;

class RepeatTest extends SignalTester {

    @Test
    void repeatLimit() {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat 1", Complete).value("success to repeat 1");
        assert main.isNotCompleted();
        assert main.emit("success to repeat 2", Complete).value("success to repeat 2");
        assert main.isNotCompleted();
        assert main.emit("success to repeat 3", Complete).value("success to repeat 3");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert main.emit("fail to repeat").value();
    }

    @Test
    void repeatLimitZero() {
        monitor(signal -> signal.repeat(0));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void repeatLimitNegative() {
        monitor(signal -> signal.repeat(-1));

        assert main.emit(Complete).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void repeatError() {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat 1", Error).value("success to repeat 1");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert main.emit("fail to repeat").value();
    }

    @Test
    void repeatInfinite() {
        monitor(signal -> signal.skip(1).take(1).repeat());

        assert main.emit(1, 2).value(2);
        assert main.emit(3, 4).value(4);
        assert main.emit(5, 6, 7, 8).value(6, 8);
        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void repeatInfiniteError() {
        monitor(1, signal -> signal.skip(1).take(1).repeat());

        assert main.emit(1, 2, 1, 2).value(2, 2);
        assert main.emit(3, 4, Error).value(4);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void disposeRepeat() {
        monitor(signal -> signal.repeat(3));

        assert main.emit("success to repeat", Complete).value("success to repeat");
        assert main.isNotCompleted();

        main.dispose();
        assert main.emit("fail to repeat", Complete).value();
        assert main.isNotCompleted();
    }

    @Test
    void repeatThenMerge() {
        monitor(signal -> signal.repeat().merge(other.signal()));

        // from main
        assert main.emit("skip", "take", Complete).value("skip", "take");
        assert main.emit("skip", "take", Complete).value("skip", "take");
        assert main.emit("skip", "take", Complete).value("skip", "take");

        // from other
        assert other.emit("external").value("external");

        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();

        // dispose
        main.dispose();
        assert main.emit("main is disposed so this value will be ignored").value();
        assert other.emit("other is disposed so this value will be ignored").value();

        assert main.isNotCompleted();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isDisposed();
    }

    @Test
    void repeatUntil() {
        monitor(signal -> signal.repeatUntil(other.signal()));

        assert main.emit("success to repeat", Complete).value("success to repeat");
        assert main.emit("success to repeat", Complete).value("success to repeat");

        other.emit("never repeat");
        assert main.emit("last message", Complete).value("last message");
        assert main.emit("failt to repeat", Complete).value();
    }

    @Test
    void repeatWhen() {
        monitor(signal -> signal.startWith("first").repeatWhen(repeat -> repeat.sample(other.signal())));

        assert main.value("first");
        assert main.countObservers() == 1;
        assert main.emit(Complete).value();
        assert main.hasNoObserver();
        other.emit("repeat observing");
        assert main.value("first");
        assert main.countObservers() == 1;
        assert main.emit(Complete).value();
        assert main.hasNoObserver();
        other.emit("repeat observing");
        assert main.value("first");
        assert main.countObservers() == 1;
    }

    @Test
    void repeatWhenWithDelayAndLimit() {
        monitor(signal -> signal.startWith("first").repeatWhen(repeat -> repeat.delay(10, ms).sample(other.signal()).take(2)));

        assert main.value("first");
        assert main.emit(Complete).value();
        other.emit("repeat observing");
        assert main.value("first");
        assert main.emit(Complete).value();
        other.emit("repeat observing");
        assert main.value("first");
        assert main.emit(Complete).value();
        other.emit("repeat observing");
        assert main.value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void repeatWhenWithError() {
        monitor(signal -> signal.startWith("repeat").repeatWhen(repeat -> repeat.takeAt(index -> {
            if (index == 2) {
                throw new Error();
            }
            return true;
        })));

        assert main.value("repeat");
        assert main.emit(Complete).value("repeat");
        assert main.isNotCompleted();
        assert main.emit(Complete).value("repeat");
        assert main.isNotCompleted();
        assert main.emit("next will fail", Complete).value("next will fail");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void repeatWhenWithComplete() {
        monitor(signal -> signal.repeatWhen(repeat -> repeat.take(2)));

        assert main.emit("first will repeat", Complete).value("first will repeat");
        assert main.isNotCompleted();
        assert main.emit("second will repeat", Complete).value("second will repeat");
        assert main.isNotCompleted();
        assert main.emit("third will fail", Complete).value("third will fail");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void repeatWhenImmediately() {
        monitor(() -> I.signal("start").effect(log("Begin")).repeatWhen(repeat -> repeat.take(3).effect(log("Repeat"))).effect(log("End")));

        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert checkLog("Begin").size() == 4;
        assert checkLog("Repeat").size() == 3;
        assert checkLog("End").size() == 4;
    }

    @Test
    void repeatWhenWithDelayImmediately() {
        monitor(1, () -> I.signal("start")
                .effect(log("Begin"))
                .repeatWhen(repeat -> repeat.delay(10, ms, scheduler).take(3).effect(log("Repeat")))
                .effect(log("End")));

        scheduler.await();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert checkLog("Begin").size() == 4;
        assert checkLog("Repeat").size() == 3;
        assert checkLog("End").size() == 4;
    }

    @Test
    void repeatWhenNullNotifier() {
        Signal<Object> signal = I.signal();
        Signal<Object> repeat = signal.repeatWhen(null);
        assert signal == repeat;
    }
}
