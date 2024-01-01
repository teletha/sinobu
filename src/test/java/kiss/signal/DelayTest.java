/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class DelayTest extends SignalTester {

    @Test
    void delay() {
        monitor(signal -> signal.delay(delay, ms, scheduler));

        assert main.emit("delay").value();
        scheduler.await();
        assert main.value("delay");

        assert main.emit("one", "more").value();
        scheduler.await();
        assert main.value("one", "more");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayZero() {
        monitor(signal -> signal.delay(0, ms));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNegative() {
        monitor(signal -> signal.delay(-10, ms));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNullUnit() {
        monitor(signal -> signal.delay(delay, null));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(delay), scheduler));

        assert main.emit("delay").value();
        scheduler.await();
        assert main.value("delay");

        assert main.emit("one", "more").value();
        scheduler.await();
        assert main.value("one", "more");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNegativeDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(-30)));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayZeroDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(0)));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNullDuration() {
        monitor(signal -> signal.delay((Duration) null));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayComplete() {
        monitor(signal -> signal.delay(delay, ms, scheduler));

        assert main.emit("1", "2").value();
        scheduler.await();
        assert main.value("1", "2");
        assert main.isNotCompleted();
        assert main.emit("3", Complete).value();
        scheduler.await();
        assert main.value("3");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void delayCompleteWithoutValues() {
        monitor(signal -> signal.delay(delay, ms, scheduler));

        assert main.emit(Complete).value();
        scheduler.await();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}