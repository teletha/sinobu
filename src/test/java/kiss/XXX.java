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

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * @version 2018/03/20 19:56:43
 */
public class XXX {

    public static void main(String[] args) {
        PublishSubject p1 = PublishSubject.create();
        PublishSubject p2 = PublishSubject.create();

        Observable merge = Observable.merge(p1, p2);
        Disposable dispose = merge.subscribe(v -> {
            System.out.println(v);
        }, e -> {
            System.out.println(e);
        }, () -> {
            System.out.println("COMP");
        });

        p1.onNext("1");
        p2.onNext("ONE");
        p1.onComplete();
        System.out.println(dispose.isDisposed());
        p1.onNext("2");
        p2.onNext("TWO");
        p2.onComplete();
        System.out.println(dispose.isDisposed());
    }
}
