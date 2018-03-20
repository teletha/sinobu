/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static java.util.concurrent.TimeUnit.*;

/**
 * @version 2018/03/20 16:58:20
 */
public class ZZZ {

    public static void main(String[] args) throws InterruptedException {
        I.signal(0, 2, SECONDS).share().effect(v -> {
            System.out.println(v);
        }).map(v -> 24 / v).effect(v -> {
            System.out.println(v);
        }).recover(10).take(2).to(v -> {
            System.out.println("#" + v);
        });

        Thread.sleep(8000);
    }
}
