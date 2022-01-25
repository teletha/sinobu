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

import java.util.Comparator;

import org.junit.jupiter.api.Test;

class SortTest extends SignalTester {

    @Test
    void sort() {
        monitor(int.class, signal -> signal.sort(Comparator.naturalOrder()));

        assert main.emit(5, 2, 3, 1, 4).value();
        assert main.emit(Complete).value(1, 2, 3, 4, 5);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void empty() {
        monitor(int.class, signal -> signal.sort(null));

        main.emit(1, 2, Complete);
    }

    @Test
    void error() {
        monitor(int.class, signal -> signal.sort(Comparator.naturalOrder()));

        assert main.emit(Error).value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(int.class, signal -> signal.sort(Comparator.naturalOrder()));

        assert main.emit(Complete).value();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }
}