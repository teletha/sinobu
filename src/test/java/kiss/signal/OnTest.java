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
 * @version 2018/04/12 8:35:33
 */
class OnTest extends SignalTester {

    @Test
    void on() {
        monitor(signal -> signal.on(thread("other")).map(v -> Thread.currentThread().getName()));

        main.emit("START");
        assert await(20).value("other");
    }

    @Test
    void multiple() {
        monitor(signal -> signal.on(thread("other"))
                .map(v -> Thread.currentThread().getName())
                .on(thread("last"))
                .map(v -> v + " " + Thread.currentThread().getName()));

        main.emit("START");
        assert await(20).value("other last");
    }

    @Test
    void error() {
        monitor(signal -> signal.on(thread("other")).map(v -> Thread.currentThread().getName()));

        main.emit(Error.class);
        assert main.isNotError();
        assert main.isNotDisposed();
        await(20);
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.on(thread("other")).map(v -> Thread.currentThread().getName()));

        main.emit(Complete);
        assert main.isNotCompleted();
        await(20);
        assert main.isCompleted();
    }

    @Test
    void dispose() {
        monitor(signal -> signal.take(1).on(after20ms));

        assert main.emit("send value", "immediately").value();
        assert await().value("send value");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    /**
     * Thread builder.
     * 
     * @param action
     */
    private Consumer<Runnable> thread(String name) {
        return action -> {
            Thread thread = new Thread(action);
            thread.setName(name);
            thread.start();
        };
    }

    /**
     * Scheduler.
     */
    private Consumer<Runnable> after20ms = runner -> {
        I.schedule(20, ms, false, runner);
    };
}
