/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import antibug.Chronus;

class ScheduleTest {

    Chronus chronus = new Chronus();

    String value;

    @Test
    void executeNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.schedule(null));
    }

    @Test
    void schedule() {
        Disposable result = I.schedule(30, MILLISECONDS, chronus).to(() -> {
            value = "scheduled";
        });

        assert value == null;
        assert result.isDisposed() == false;
        chronus.await();
        assert value.equals("scheduled");
        assert result.isDisposed();
    }

    @Test
    void scheduleNegativeDelay() {
        Disposable result = I.schedule(-1, MILLISECONDS).to(() -> {
            value = "done immediately";
        });

        assert value.equals("done immediately");
        assert result.isDisposed();
    }

    @Test
    void scheduleZeroDelay() {
        Disposable result = I.schedule(0, MILLISECONDS).to(() -> {
            value = "done immediately";
        });

        assert value.equals("done immediately");
        assert result.isDisposed();
    }

    @Test
    void scheduleSignal() {
        Variable<Long> variable = I.schedule(10, MILLISECONDS, chronus).to();

        assert variable.isAbsent();
        chronus.await();
        assert variable.is(1L);
    }

    @Test
    void scheduleSignalNegativeTime() {
        Variable<Long> variable = I.schedule(-1, MILLISECONDS, chronus).to();

        assert variable.is(1L);
    }

    @Test
    void scheduleSignalZeroTime() {
        Variable<Long> variable = I.schedule(0, MILLISECONDS, chronus).to();

        assert variable.is(1L);
    }

    @Test
    void scheduleSignalNullTimeUnit() {
        Assertions.assertThrows(NullPointerException.class, () -> I.schedule(10, null, chronus));
    }

    @Test
    void scheduleSignalNullScheduler() {
        Variable<Long> variable = I.schedule(10, TimeUnit.MILLISECONDS, (ScheduledExecutorService) null).to();

        assert variable.isAbsent();
        chronus.await(100, TimeUnit.MILLISECONDS);
        assert variable.is(1L);
    }
}