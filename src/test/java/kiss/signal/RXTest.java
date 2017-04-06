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

import org.junit.Test;

import io.reactivex.Observable;

/**
 * @version 2017/04/06 8:43:46
 */
public class RXTest extends SignalTestBase {

    @Test
    public void testname() throws Exception {
        Observable.just(1, 2).startWith(errorIterable()).onErrorResumeNext((Throwable e) -> Observable.just(10)).subscribe(v -> {
            System.out.println("value  " + v);
        }, e -> {
            System.out.println("erro " + e);
        }, () -> {
            System.out.println("complete");
        });
    }
}
