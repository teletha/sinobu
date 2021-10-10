/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import kiss.I;

class PredicateTest {

    /**
     * @see I#Accept
     */
    @Test
    void accept() {
        Predicate<String> accept = I.Accept;
        assert accept.test("accept") == true;
        assert accept.test("all") == true;
        assert accept.test(null) == true;
    }

    // /**
    // * @see I#accepţ()
    // */
    // @Test
    // void accepţ() {
    // BiPredicate<String, Integer> accept = I.accepţ();
    // assert accept.test("accept", 1) == true;
    // assert accept.test("all", -1) == true;
    // assert accept.test(null, 0) == true;
    // }
    //
    // /**
    // * @see I#reject()
    // */
    // @Test
    // void reject() {
    // Predicate<String> reject = I.reject();
    // assert reject.test("reject") == false;
    // assert reject.test("all") == false;
    // assert reject.test(null) == false;
    // }
    //
    // /**
    // * @see I#rejecţ()
    // */
    // @Test
    // void rejecţ() {
    // BiPredicate<String, Integer> accept = I.rejecţ();
    // assert accept.test("reject", 1) == false;
    // assert accept.test("all", -1) == false;
    // assert accept.test(null, 0) == false;
    // }
}