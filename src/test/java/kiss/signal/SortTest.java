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

import java.util.Comparator;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/09/28 13:34:18
 */
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
}
