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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class JoinAllOrNoneTest extends SignalTester {

    @Test
    void joinAllOrNone() {
        monitor(1, String.class, signal -> signal.joinAllOrNone(String::toUpperCase));

        assert main.emit("a", "b", "c").value();
        assert main.emit(Complete).value("A", "B", "C");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void errorByAll() {
        monitor(String.class, signal -> signal.joinAllOrNone(errorFunction()));

        assert main.emit("a", "b", "c", Complete).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorByOneOfThem() {
        monitor(int.class, signal -> signal.joinAllOrNone(v -> {
            if (v == 2) {
                throw new Exception();
            } else {
                return v;
            }
        }));

        assert main.emit(1, 2, 3, Complete).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void asyncExecutorService() {
        monitor(String.class, signal -> signal.joinAllOrNone(v -> {
            if (v.equals("a")) {
                Thread.sleep(200);
            }
            return v.toUpperCase();
        }));

        assert main.emit("a", "b", "c").value();
        assert main.emit(Complete).value("A", "B", "C");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void syncExecutorService() {
        monitor(String.class, signal -> signal.joinAllOrNone(v -> {
            if (v.equals("a")) {
                Thread.sleep(200);
            }
            return v.toUpperCase();
        }, Executors.newSingleThreadExecutor()));

        assert main.emit("a", "b", "c").value();
        assert main.emit(Complete).value("A", "B", "C");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void nullExecutorService() {
        monitor(String.class, signal -> signal.joinAllOrNone(v -> {
            if (v.equals("a")) {
                Thread.sleep(200);
            }
            return v.toUpperCase();
        }, (ExecutorService) null));

        assert main.emit("a", "b", "c").value();
        assert main.emit(Complete).value("A", "B", "C");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
