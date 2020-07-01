/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class JoinAllTest extends SignalTester {

    @Test
    void joinAll() {
        monitor(1, String.class, signal -> signal.joinAll(String::toUpperCase));

        assert main.emit("a", "b", "c").value();
        assert main.emit(Complete).value("A", "B", "C");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void errorByAll() {
        monitor(String.class, signal -> signal.joinAll(errorFunction()));

        assert main.emit("a", "b", "c", Complete).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorByOneOfThem() {
        monitor(int.class, signal -> signal.joinAll(v -> {
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
}