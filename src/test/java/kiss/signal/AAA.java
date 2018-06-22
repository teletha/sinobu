/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import java.util.concurrent.TimeUnit;

import kiss.I;

/**
 * @version 2018/06/22 19:56:54
 */
public class AAA {

    public static void main(String[] args) throws InterruptedException {
        I.signalBox(1, s -> s.map(v -> v + 1).delay(100, TimeUnit.MILLISECONDS)).take(30).to(v -> {
            System.out.println("show " + v);
        });
        long start = System.currentTimeMillis();
        Thread.sleep(3000);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
