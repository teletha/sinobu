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

import java.util.Random;
import java.util.concurrent.TimeUnit;

import kiss.Scheduler;

public class StressBench {

    @SuppressWarnings("resource")
    public static void main(String args[]) throws Exception {
        Random random = new Random();
        Scheduler scheduler = new Scheduler();

        for (int counter = 0; counter < 1000_000; ++counter) {
            scheduler.schedule(new Job(counter), random.nextLong(5000, 1000 * 90), TimeUnit.MILLISECONDS);
        }

        scheduler.scheduleAtFixedRate(System::gc, 30, 20, TimeUnit.SECONDS);

        Thread.sleep(1000 * 90);
    }

    record Job(int id) implements Runnable {
        @Override
        public void run() {
            System.out.println(id);
        }
    }
}