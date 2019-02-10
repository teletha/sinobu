/*
 * Copyright (C) 2019 Nameless Production Committee
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

/**
 * @version 2016/10/28 15:17:58
 */
public class PredicateTest {

    @Test
    public void accept() {
        Predicate<String> accept = I.accept();
        assert accept.test("test") == true;
        assert accept.test(null) == true;
    }

    @Test
    public void reject() {
        Predicate<String> reject = I.reject();
        assert reject.test("test") == false;
        assert reject.test(null) == false;
    }
}
