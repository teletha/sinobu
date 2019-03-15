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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class IntervalTest extends SignalTester {

    @Test
    void interval() {
        monitor(signal -> signal.interval(10, ms, scheduler).map(v -> Instant.now()));

        assert main.emit("each", "events", "has", "enough", "interval", "time").value();
        scheduler.await();
        assert main.validate(intervalAtLeast(10, ms));
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.interval(10, ms, scheduler).map(v -> Instant.now()));

        assert main.emit("complete", "event", "has", "interval", "time", "too", Complete).value();
        scheduler.await();
        assert main.validate(intervalAtLeast(10, ms));
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    private Consumer<List<Instant>> intervalAtLeast(long time, TimeUnit unit) {
        return times -> {
            assert 2 < times.size();

            for (int i = 1; i < times.size(); i++) {
                Instant prev = times.get(i - 1);
                Instant next = times.get(i);

                assert unit.toNanos(time) <= Duration.between(prev, next).toNanos();
            }
        };
    }
}
