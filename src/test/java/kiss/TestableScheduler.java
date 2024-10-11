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

import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TestableScheduler extends Scheduler {

    private long awaitingLimit = 3000;

    private final AtomicBoolean starting = new AtomicBoolean();

    private List<Task> startingBuffer = new ArrayList();

    private AtomicLong executed = new AtomicLong();

    /**
     * 
     */
    public TestableScheduler() {
        super();
    }

    /**
     * @param limit
     */
    public TestableScheduler(int limit) {
        super(limit);
    }

    private Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                executed.incrementAndGet();
            }
        };
    }

    private <V> Callable<V> wrap(Callable<V> task) {
        return () -> {
            try {
                return task.call();
            } finally {
                executed.incrementAndGet();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return super.schedule(wrap(command), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> command, long delay, TimeUnit unit) {
        return super.schedule(wrap(command), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long delay, long interval, TimeUnit unit) {
        return super.scheduleAtFixedRate(wrap(command), delay, interval, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long delay, long interval, TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrap(command), delay, interval, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAt(Runnable command, String format) {
        return super.scheduleAt(wrap(command), format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Task executeTask(Task task) {
        if (starting.get()) {
            super.executeTask(task);
        } else {
            startingBuffer.add(task);
        }
        return task;
    }

    /**
     * Start task handler thread.
     * 
     * @return
     */
    public final TestableScheduler start() {
        if (starting.compareAndSet(false, true)) {
            for (Task task : startingBuffer) {
                super.executeTask(task);
            }
            startingBuffer.clear();
        }
        return this;
    }

    /**
     * Await any task is running.
     */
    public boolean awaitRunning() {
        int count = 0; // await at least once
        long start = System.currentTimeMillis();
        while (count++ == 0 || runs.isEmpty()) {
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }

            if (awaitingLimit <= System.currentTimeMillis() - start) {
                throw new Error("No task is active. " + this);
            }
        }
        return true;
    }

    public TestableScheduler limitAwaitTime(long millis) {
        awaitingLimit = millis;
        return this;
    }

    /**
     * Await all tasks are executed.
     */
    public boolean awaitIdling() {
        int count = 0; // await at least once
        long start = System.currentTimeMillis();

        while (count++ == 0 || !queue.isEmpty() || !runs.isEmpty()) {
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }

            if (awaitingLimit <= System.currentTimeMillis() - start) {
                throw new Error("Too long task is active. " + this);
            }
        }
        return true;
    }

    /**
     * Awaits until the specified number of tasks have been executed.
     * 
     * @param required
     * @return
     */
    public boolean awaitExecutions(long required) {
        long start = System.currentTimeMillis();

        while (executed.get() < required) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }

            if (awaitingLimit <= System.currentTimeMillis() - start) {
                throw new Error("Too long task is active. " + this);
            }
        }
        return true;
    }

    public boolean await() {
        return start().awaitIdling();
    }

    /**
     * Freeze process.
     */
    public void await(long time, TimeUnit unit) {
        freezeNano(unit.toNanos(time));
    }

    /**
     * <p>
     * Freeze process.
     * </p>
     * 
     * @param time A nano time to freeze.
     */
    private void freezeNano(long time) {
        try {
            long start = System.nanoTime();
            NANOSECONDS.sleep(time);
            long end = System.nanoTime();

            long remaining = start + time - end;

            if (0 < remaining) {
                freezeNano(remaining);
            }
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private long marked;

    /**
     * Record the current time. Hereafter, it is used as the start time when using
     * {@link #elapse(int, TimeUnit)} or {@link #within(int, TimeUnit, Runnable)}.
     */
    public final TestableScheduler mark() {
        marked = System.nanoTime();

        return this;
    }

    /**
     * <p>
     * Waits for the specified time from the marked time. It does not wait if it has already passed.
     * </p>
     * <pre>
     * chronus.mark();
     * asynchronous.process();
     * 
     * chronus.elapse(100, TimeUnit.MILLSECONDS);
     * assert validation.code();
     * </pre>
     * 
     * @param amount Time amount.
     * @param unit Time unit.
     */
    public final TestableScheduler elapse(int amount, TimeUnit unit) {
        long startTime = marked + unit.toNanos(amount);

        await(startTime - System.nanoTime(), NANOSECONDS);

        return this;
    }

    /**
     * <p>
     * Performs the specified operation if the specified time has not yet elapsed since the marked
     * time. If it has already passed, do nothing.
     * </p>
     * <pre>
     * chronus.mark();
     * synchronous.process();
     * 
     * chronus.within(100, TimeUnit.MILLSECONDS, () -> {
     * assert validation.code();
     * });
     * </pre>
     * 
     * @param amount Time amount.
     * @param unit Time unit.
     * @param within Your process.
     */
    public final TestableScheduler within(int amount, TimeUnit unit, Runnable within) {
        if (within != null && System.nanoTime() < marked + unit.toNanos(amount)) {
            within.run();
        }
        return this;
    }

    /**
     * Create delayed {@link Executors} in the specified duration.
     * 
     * @param time A delay time.
     * @param unit A time unit.
     * @return A delayed {@link Executor}.
     */
    public Executor in(long time, TimeUnit unit) {
        return task -> schedule(task, time, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Executor [running: " + runs.size() + " executed: " + executed + " queue: " + queue + "]";
    }
}