/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ChronoScheduler extends ChronoExecutor<ScheduledExecutorService> implements ScheduledExecutorService {

    /**
     * By {@link Executors#newScheduledThreadPool(int)}.
     */
    public ChronoScheduler() {
        this(() -> Executors.newScheduledThreadPool(1));
    }

    /**
     * By your {@link ScheduledExecutorService}.
     * 
     * @param builder Your {@link ScheduledExecutorService}.
     */
    public ChronoScheduler(Supplier<ScheduledExecutorService> builder) {
        super(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Task task = new Task(command);

        return task.connect(executor().schedule((Callable) task, delay, unit));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        Task task = new Task(callable);

        return task.connect(executor().schedule((Callable) task, delay, unit));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executor().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executor().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}