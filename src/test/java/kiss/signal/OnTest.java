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

import org.junit.Test;

/**
 * @version 2018/03/04 13:33:46
 */
public class OnTest extends SignalTester {

    @Test
    public void on() {
        monitor(signal -> signal.on(thread("other")).map(v -> Thread.currentThread().getName()));

        main.emit("START");
        assert await(20).value("other");
    }

    @Test
    public void multiple() {
        monitor(signal -> signal.on(thread("other"))
                .map(v -> Thread.currentThread().getName())
                .on(thread("last"))
                .map(v -> v + " " + Thread.currentThread().getName()));

        main.emit("START");
        assert await(20).value("other last");
    }

    @Test
    public void error() {
        monitor(signal -> signal.on(thread("other")).map(v -> Thread.currentThread().getName()));

        main.emit(Error.class);
        assert main.isNotError();
        assert main.isNotDisposed();
        await(20);
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void complete() {
        monitor(signal -> signal.on(thread("other")).map(v -> Thread.currentThread().getName()));

        main.emit(Complete);
        assert main.isNotCompleted();
        await(20);
        assert main.isCompleted();
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
}
