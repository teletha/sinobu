/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.LongUnaryOperator;

class Task<V> extends FutureTask<V> implements ScheduledFuture<V> {

    /** The next trigger time. (epoch ms) */
    volatile long next;

    /** The interval calculator. */
    final LongUnaryOperator interval;

    /** The executing thread. */
    Thread thread;

    /**
     * Create new task.
     * 
     * @param task
     * @param next
     * @param interval
     */
    Task(Callable<V> task, long next, LongUnaryOperator interval) {
        super(task);

        this.next = next;
        this.interval = interval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        if (interval == null) {
            // one shot
            super.run();
        } else {
            // periodically
            runAndReset();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(next - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Delayed other) {
        if (other instanceof Task task) {
            return Long.compare(next, task.next);
        } else {
            return 0;
        }
    }
}