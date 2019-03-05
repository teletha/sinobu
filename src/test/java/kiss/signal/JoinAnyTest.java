/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class JoinAnyTest extends SignalTester {

    @Test
    void joinAny() {
        monitor(String.class, signal -> signal.joinAny(String::toUpperCase));

        assert main.emit("a", "b", "c").value();
        assert main.emit(Complete).size(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void errorByAll() {
        monitor(String.class, signal -> signal.joinAny(errorFunction()));

        assert main.emit("a", "b", "c", Complete).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void successOnlyFirst() {
        monitor(int.class, signal -> signal.joinAny(v -> {
            if (v != 1) {
                throw new Exception();
            } else {
                return v;
            }
        }));

        assert main.emit(1, 2, 3, Complete).value(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void successOnlySecond() {
        monitor(int.class, signal -> signal.joinAny(v -> {
            if (v != 2) {
                throw new Exception();
            } else {
                return v;
            }
        }));

        assert main.emit(1, 2, 3, Complete).value(2);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void successOnlyThird() {
        monitor(int.class, signal -> signal.joinAny(v -> {
            if (v != 3) {
                throw new Exception();
            } else {
                return v;
            }
        }));

        assert main.emit(1, 2, 3, Complete).value(3);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
