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

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * @version 2017/04/06 8:43:46
 */
public class RXTest extends SignalTestBase {

    public static void main(String[] args) {
        new RXTest().test();
    }

    private void test() {
        PublishSubject s = PublishSubject.create();
        Disposable subscribe = Observable.just(1, 2, 3).skip(1).take(2).repeat(1).subscribe(v -> {
            System.out.println(v);
        }, e -> {

        }, () -> {
            System.out.println("complete");
        });

        s.onNext(1);
        s.onComplete();
        s.onNext(2);
        s.onComplete();
        s.onNext(3);
        s.onComplete();
        s.onNext(4);
    }
}
