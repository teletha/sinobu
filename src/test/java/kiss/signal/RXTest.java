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

/**
 * @version 2017/04/06 8:43:46
 */
public class RXTest extends SignalTestBase {

    public static void main(String[] args) {
        new RXTest().test();
    }

    private void test() {
        PublishSubject other = PublishSubject.create();
        PublishSubject s = PublishSubject.create();
        Disposable subscribe = s.mergeWith(other).subscribe(v -> {
            System.out.println(v);
        }, e -> {

        }, () -> {
            System.out.println("complete");
        });

        other.onNext("one");
        s.onNext(1);
        s.onNext(2);
        s.onComplete();
        other.onNext("two");
        s.onNext(3);

    }
}
