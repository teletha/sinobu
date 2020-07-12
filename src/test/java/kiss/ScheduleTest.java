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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import antibug.Chronus;

class ScheduleTest {

    Chronus chronus = new Chronus();

    String value;

    @Test
    void executeImmediately() {
        CompletableFuture result = I.schedule(Runnable::run, () -> {
            value = "executed";
        });
        assert value.equals("executed");
        assert result.isDone();
    }

    @Test
    void executeLazily() {
        Executor scheduler = task -> {
            chronus.schedule(task, 30, MILLISECONDS);
        };

        CompletableFuture result = I.schedule(scheduler, () -> {
            value = "scheduled";
        });

        assert value == null;
        assert result.isDone() == false;
        chronus.await();
        assert value.equals("scheduled");
        assert result.isDone();
    }

    @Test
    void executeNull() {
        CompletableFuture future = I.schedule(null);
        assert future.isDone();
    }

    @Test
    void schedule() {
        CompletableFuture result = I.schedule(30, MILLISECONDS, chronus, () -> {
            value = "scheduled";
        });

        assert value == null;
        assert result.isDone() == false;
        chronus.await();
        assert value.equals("scheduled");
        assert result.isDone();
    }

    @Test
    void scheduleNegativeDelay() {
        CompletableFuture result = I.schedule(-1, MILLISECONDS, () -> {
            value = "done immediately";
        });

        assert value.equals("done immediately");
        assert result.isDone();
    }

    @Test
    void scheduleZeroDelay() {
        CompletableFuture result = I.schedule(0, MILLISECONDS, () -> {
            value = "done immediately";
        });

        assert value.equals("done immediately");
        assert result.isDone();
    }

    @Test
    void scheduleNullTask() {
        CompletableFuture result = I.schedule(30, MILLISECONDS, (Runnable) null);

        assert value == null;
        assert result.isDone();
    }

    @Test
    void scheduleSignal() {
        Variable<Long> variable = I.schedule(10, MILLISECONDS, chronus).to();

        assert variable.isAbsent();
        chronus.await();
        assert variable.is(0L);
    }

    @Test
    void scheduleSignalNegativeTime() {
        Variable<Long> variable = I.schedule(-1, MILLISECONDS, chronus).to();

        assert variable.is(0L);
    }

    @Test
    void scheduleSignalZeroTime() {
        Variable<Long> variable = I.schedule(0, MILLISECONDS, chronus).to();

        assert variable.is(0L);
    }

    @Test
    void scheduleSignalNullTimeUnit() {
        Variable<Long> variable = I.schedule(10, null, chronus).to();

        assert variable.is(0L);
    }

    @Test
    void scheduleSignalNullScheduler() {
        Variable<Long> variable = I.schedule(10, TimeUnit.MILLISECONDS, (ScheduledExecutorService) null).to();

        assert variable.isAbsent();
        chronus.await(100, TimeUnit.MILLISECONDS);
        assert variable.is(0L);
    }
}