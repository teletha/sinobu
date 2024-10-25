/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scheduler;

import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.Test;

class ScheduleByCronTest extends SchedulerTestSupport {

    @Test
    void cron() {
        scheduler.limitAwaitTime(5000);

        Verifier verifier = new Verifier();
        ScheduledFuture<?> future = scheduler.scheduleAt(verifier, "* * * * * *");

        assert verifyRunning(future);
        assert scheduler.start().awaitExecutions(3);
        assert verifier.verifyExecutionCount(3);
        assert verifier.verifyRate(0, 1000, 1000);
    }

    @Test
    void step() {
        scheduler.limitAwaitTime(5000);

        Verifier verifier = new Verifier();
        ScheduledFuture<?> future = scheduler.scheduleAt(verifier, "*/2 * * * * *");

        assert verifyRunning(future);
        assert scheduler.start().awaitExecutions(2);
        assert verifier.verifyExecutionCount(2);
        assert verifier.verifyInterval(0, 2000);
    }
}
