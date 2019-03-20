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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ZZZ {

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            int x = i;
            CompletableFuture future = I.schedule(1000, TimeUnit.MILLISECONDS, null, () -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
                System.out.println("ok" + x);
            });
        }
        Thread.sleep(2000);
    }

    public static void byCachedThreadPoolWithCompletableFuture(String[] args) throws InterruptedException {
        ExecutorService s = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        for (int i = 0; i < 30; i++) {
            int x = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
                System.out.println("ok" + x);
            }, CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS, s));
        }
        Thread.sleep(2000);
    }

    public static void byWorkStealingPool(String[] args) throws InterruptedException {
        ExecutorService s = Executors.newWorkStealingPool();

        for (int i = 0; i < 30; i++) {
            int x = i;
            s.submit(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
                System.out.println("ok" + x);
            });
        }
        Thread.sleep(2000);
    }

    public static void byThread(String[] args) throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            int x = i;

            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
                System.out.println("ok" + x);
            });
            t.setDaemon(true);
            t.start();
        }
        Thread.sleep(2000);
    }

    public static void byTimer(String[] args) throws InterruptedException {
        Timer timer = new Timer(true);
        for (int i = 0; i < 30; i++) {
            int x = i;

            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw I.quiet(e);
                    }
                    System.out.println("ok" + x);
                }
            }, 100);
        }
        Thread.sleep(2000);
    }

    public static void byScheduledThreadPool(String[] args) throws InterruptedException {
        ScheduledExecutorService s = Executors.newScheduledThreadPool(4, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        for (int i = 0; i < 30; i++) {
            int x = i;
            s.schedule(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
                System.out.println("ok" + x);
            }, 100, TimeUnit.MILLISECONDS);
        }
        Thread.sleep(2000);
    }
}
