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

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/04/12 8:51:29
 */
class OnTest extends SignalTester {

    @Test
    void on() {
        monitor(signal -> signal.on(after20ms).map(v -> Thread.currentThread().getName()));

        main.emit("START");
        assert await().value("Sinobu Scheduler");
    }

    @Test
    void multiple() {
        monitor(String.class, Boolean.class, signal -> signal.on(after20ms)
                .map(v -> Thread.currentThread())
                .on(after20ms)
                .map(v -> v == Thread.currentThread()));

        main.emit("START");
        assert await().value(false);
    }

    @Test
    void error() {
        monitor(signal -> signal.on(after20ms).map(v -> Thread.currentThread().getName()));

        main.emit(Error.class);
        assert main.isNotError();
        assert main.isNotDisposed();
        await();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.on(after20ms).map(v -> Thread.currentThread().getName()));

        main.emit(Complete);
        assert main.isNotCompleted();
        await();
        assert main.isCompleted();
    }

    @Test
    void dispose() {
        monitor(signal -> signal.take(1).on(after20ms));

        assert main.emit("First value will be accepted", "Second will not!").value();
        assert await().size(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    /**
     * Scheduler.
     */
    private Consumer<Runnable> after20ms = runner -> {
        I.schedule(20, ms, true, runner);
    };
}
