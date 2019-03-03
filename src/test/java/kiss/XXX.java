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

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * 
 */
public class XXX {

    public static void main(String[] args) throws InterruptedException {

        Observable.just(50, 30, 10).map(e -> {
            System.out.println("task start " + e + " on " + Thread.currentThread());
            return e;
        }).observeOn(Schedulers.computation()).map(e -> {
            Thread.sleep(1000);
            System.out.println(e + " on map " + Thread.currentThread());
            return e * 2;
        }).subscribe(v -> {
        }, e -> {

        }, () -> {
            System.out.println("COMPLETE");
        });
        Thread.sleep(5000);
    }
}
