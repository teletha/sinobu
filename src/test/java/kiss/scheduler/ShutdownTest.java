/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.RepeatedTest;

public class ShutdownTest extends SchedulerTestSupport {

    @RepeatedTest(MULTIPLICITY)
    void rejectNewTask() {
        assert scheduler.isShutdown() == false;
        assert scheduler.isTerminated() == false;

        scheduler.start().shutdown();
        assert scheduler.isShutdown();
        assert scheduler.isTerminated();

        assertThrows(RejectedExecutionException.class, () -> scheduler.execute(new Verifier()));
        assertThrows(RejectedExecutionException.class, () -> scheduler.submit(new Verifier().asCallable()));
        assertThrows(RejectedExecutionException.class, () -> scheduler.submit(new Verifier().asRunnable()));
        assertThrows(RejectedExecutionException.class, () -> scheduler.schedule(new Verifier().asRunnable(), 10, TimeUnit.SECONDS));
        assertThrows(RejectedExecutionException.class, () -> scheduler.schedule(new Verifier().asCallable(), 10, TimeUnit.SECONDS));
        assertThrows(RejectedExecutionException.class, () -> scheduler.scheduleAtFixedRate(new Verifier(), 10, 10, TimeUnit.SECONDS));
        assertThrows(RejectedExecutionException.class, () -> scheduler.scheduleAtFixedRate(new Verifier(), 10, 10, TimeUnit.SECONDS));
        assertThrows(RejectedExecutionException.class, () -> scheduler.scheduleAt(new Verifier(), "* * * * *"));
    }

    @RepeatedTest(MULTIPLICITY)
    void processExecutingTask() {
        Verifier<String> verifier = new Verifier(() -> {
            try {
                Thread.sleep(250);
                return "Long Task";
            } catch (InterruptedException e) {
                return "Stop";
            }
        });

        Future<String> future = scheduler.submit(verifier.asCallable());
        scheduler.start().shutdown();
        assert scheduler.isShutdown();
        // The result of isTerminated is undefined here because it is not necessarily retrieved from
        // the queue at this time, although the queued task will certainly be executed in the
        // future.
        // assert scheduler.isTerminated() == false;

        assert scheduler.awaitIdling();
        assert scheduler.isTerminated();
        assert verifySuccessed(future, "Long Task");
    }

    @RepeatedTest(MULTIPLICITY)
    void processQueuedTask() {
        Verifier<?> verifier = new Verifier("Queued");

        Future<?> future = scheduler.schedule(verifier.asCallable(), 150, TimeUnit.MILLISECONDS);
        scheduler.start().shutdown();
        assert scheduler.isShutdown();
        assert scheduler.isTerminated() == false;

        assert scheduler.awaitIdling();
        assert scheduler.isTerminated();
        assert verifySuccessed(future);
    }

    @RepeatedTest(MULTIPLICITY)
    void awaitTermination() throws InterruptedException {
        Verifier<?> verifier = new Verifier("Queued");

        Future<?> future = scheduler.schedule(verifier.asCallable(), 150, TimeUnit.MILLISECONDS);
        scheduler.start().shutdown();
        assert scheduler.isShutdown();
        assert scheduler.isTerminated() == false;

        assert scheduler.awaitTermination(300, TimeUnit.MILLISECONDS);
        assert scheduler.isTerminated();
        assert verifySuccessed(future);
    }

    @RepeatedTest(MULTIPLICITY)
    void awaitTerminationLongTask() throws InterruptedException {
        Verifier<String> verifier = new Verifier(() -> {
            try {
                Thread.sleep(150);
                return "Long Task";
            } catch (InterruptedException e) {
                Thread.sleep(100);
                return "Long Stop";
            }
        });

        Future<?> future = scheduler.submit(verifier.asCallable());
        assert scheduler.start().awaitRunning();

        scheduler.shutdown();
        assert scheduler.isShutdown();
        assert scheduler.isTerminated() == false;

        assert scheduler.awaitTermination(300, TimeUnit.MILLISECONDS);
        assert scheduler.isTerminated();
        assert verifySuccessed(future);
    }
}