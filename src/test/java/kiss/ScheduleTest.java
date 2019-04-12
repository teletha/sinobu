/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        CompletableFuture result = I.schedule(30, MILLISECONDS, null);

        assert value == null;
        assert result.isDone();
    }
}
