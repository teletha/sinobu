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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/02/28 20:03:32
 */
class MapTest extends SignalTester {

    @Test
    void map() throws Exception {
        monitor(() -> signal(1, 2).map(v -> v * 2));

        assert main.value(2, 4);
        assert main.isCompleted();
    }

    @Test
    void mapNull() throws Exception {
        monitor(() -> signal(1, 2).map(null));

        assert main.value(1, 2);
        assert main.isCompleted();
    }

    @Test
    void throwError() {
        monitor(() -> signal(1, 2).map(errorFunction()));
        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void mapTo() {
        monitor(signal -> signal.mapTo("ZZZ"));

        assert main.emit("A").value("ZZZ");
        assert main.emit("B").value("ZZZ");
        assert main.emit("C").value("ZZZ");
        assert main.emit((String) null).value("ZZZ");
    }

    @Test
    void mapWithPreviousValue() {
        monitor(signal -> signal.map(1, (prev, now) -> prev + now));

        assert main.emit(1).value(2);
        assert main.emit(2).value(3);
        assert main.emit(3).value(5);
    }

    @Test
    void mapWithContext() {
        monitor(Integer.class, signal -> signal.map(AtomicInteger::new, (context, value) -> context.getAndIncrement() + value));

        assert main.emit(1).value(1);
        assert main.emit(2).value(3);
        assert main.emit(3).value(5);
    }
}
