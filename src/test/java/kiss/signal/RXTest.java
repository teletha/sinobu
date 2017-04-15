/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import kiss.SignalTester;

/**
 * @version 2017/04/06 8:43:46
 */
public class RXTest extends SignalTester {

    public static void main(String[] args) throws Exception {
        new RXTest().test();
    }

    private void test() throws Exception {
        PublishSubject s = PublishSubject.create();
        PublishSubject main = PublishSubject.create();
        Disposable subscribe = main.mergeWith(s).subscribe(v -> {
            System.out.println(v);
        }, e -> {

        }, () -> {
            System.out.println("complete");
        });

        main.onNext("1");
        s.onNext("2");
        subscribe.dispose();
        main.onNext("1");
        s.onNext("2");
    }
}
