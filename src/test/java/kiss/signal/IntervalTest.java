/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;

class IntervalTest extends SignalTester {

    @Test
    void interval() {
        monitor(signal -> signal.interval(delay, ms, scheduler));

        assert main.emit("each", "events", "has", "enough", "interval", "time").value("each");
        scheduler.await();
        assert main.value("events", "has", "enough", "interval", "time");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        scheduler.mark().await(20, ms); // elapse interval time
        assert main.emit("next event is emitted immediately").value("next event is emitted immediately");
    }

    @Test
    void checkIntervalTime() {
        List<Duration> durations = I.signal(1, 2, 3, 4, 5)
                .interval(50, ms, scheduler)
                .scan(() -> Duration.ZERO, (duration, value) -> Duration.ofNanos(System.nanoTime()).minus(duration))
                .toList();

        assert durations.stream().allMatch(d -> 50 <= d.toMillis());
    }

    @Test
    void complete() {
        monitor(signal -> signal.interval(delay, ms, scheduler));

        assert main.emit("complete", "event", "has", "interval", "time", "too", Complete).value("complete");
        scheduler.await();
        assert main.value("event", "has", "interval", "time", "too");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(signal -> signal.interval(delay, ms));

        assert main.emit("dispose by error", Error).value("dispose by error");
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void zeroTime() {
        monitor(signal -> signal.interval(0, ms));

        assert main.emit("zero time", "makes", "no interval").value("zero time", "makes", "no interval");
    }

    @Test
    void negativeTime() {
        monitor(signal -> signal.interval(-30, ms));

        assert main.emit("negative time", "makes", "no interval").value("negative time", "makes", "no interval");
    }

    @Test
    void unitNull() {
        monitor(signal -> signal.interval(delay, null));

        assert main.emit("null unit", "makes", "no interval").value("null unit", "makes", "no interval");
    }
}