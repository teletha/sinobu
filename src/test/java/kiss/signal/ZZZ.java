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

import io.reactivex.subjects.PublishSubject;

/**
 * @version 2018/07/20 8:15:35
 */
public class ZZZ {

    public static void main(String[] args) {
        PublishSubject<String> aa = PublishSubject.create();

        aa.buffer(4).subscribe(v -> {
            System.out.println(v);
        }, e -> {
            System.out.println("ERROR " + e);
        }, () -> {
            System.out.println("COMP");
        });

        aa.onNext("A");
        aa.onNext("B");
        aa.onNext("C");
        aa.onNext("D");
        aa.onNext("E");
        aa.onNext("F");
        aa.onError(new Error());
    }
}
