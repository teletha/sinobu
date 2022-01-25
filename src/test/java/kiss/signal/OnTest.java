/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class OnTest extends SignalTester {

    private Consumer<Runnable> after = runner -> scheduler.schedule(runner, delay, ms);

    @Test
    void on() {
        monitor(signal -> signal.on(after).map(v -> Thread.currentThread().getName().contains("pool")));

        main.emit("START");
        assert main.value();
        scheduler.await();
        assert main.value(true);
    }

    @Test
    void error() {
        monitor(signal -> signal.on(after).map(v -> Thread.currentThread().getName()));

        main.emit(Error.class);
        assert main.isNotError();
        assert main.isNotDisposed();
        scheduler.await();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.on(after).map(v -> Thread.currentThread().getName()));

        main.emit(Complete);
        assert main.isNotCompleted();
        scheduler.await();
        assert main.isCompleted();
    }

    @Test
    void dispose() {
        // Signal#on doesn't guarantee the execution order of events (including COMPLETE)
        // Single thread executor will arrange all events in serial. (FIFO)
        scheduler.configExecutionLimit(1);

        monitor(signal -> signal.take(1).on(after));

        assert main.emit("First value will be accepted", "Second will not!").value();
        scheduler.await();
        assert main.value("First value will be accepted");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void ignoreNull() {
        monitor(signal -> signal.on(null));

        assert main.emit("ignore").value("ignore");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }
}