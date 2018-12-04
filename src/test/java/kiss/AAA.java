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

import io.reactivex.subjects.PublishSubject;

/**
 * @version 2018/12/05 8:20:06
 */
public class AAA {

    static class A {
        PublishSubject<Integer> v = PublishSubject.create();

    }

    public static void main(String[] args) {
        PublishSubject<A> sub = PublishSubject.create();

        sub.flatMap(v -> v.v).subscribe(v -> {
            System.out.println(v);
        });

        A a = new A();
        sub.onNext(a);
        a.v.onNext(1);
        a.v.onNext(3);
        sub.onComplete();

        A b = new A();
        sub.onNext(b);
        b.v.onNext(2);
        b.v.onNext(4);
        a.v.onNext(5);
        a.v.onNext(7);
    }
}
