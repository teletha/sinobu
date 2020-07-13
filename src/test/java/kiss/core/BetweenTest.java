/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;

class BetweenTest {

    @Test
    void between() {
        assert I.between(1, 3, 5);
        assert I.between(4.1, 5.5, 7.089);
        assert I.between("m", "o", "x");

        assert I.between(1, 0, 5) == false;
        assert I.between(1, 3, 2) == false;
        assert I.between(4, 3, 5) == false;
        assert I.between(5, 3, 1) == false;
    }

    @Test
    void betweenNullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> I.between(null, 2, 5));
        Assertions.assertThrows(NullPointerException.class, () -> I.between(1, null, 5));
        Assertions.assertThrows(NullPointerException.class, () -> I.between(0, 2, null));
    }

    @Test
    void bound() {
        assert I.bound(1, 3, 5) == 3;
        assert I.bound(4.1, 5.5, 7.089) == 5.5;
        assert I.bound("m", "o", "x") == "o";

        assert I.bound(1, 0, 5) == 1;
        assert I.bound(1, 3, 2) == 2;
        assert I.bound(4, 3, 5) == 4;
        assert I.bound(5, 3, 1) == 5;
    }

    @Test
    void boundNullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> I.bound(null, 2, 5));
        Assertions.assertThrows(NullPointerException.class, () -> I.bound(1, null, 5));
        Assertions.assertThrows(NullPointerException.class, () -> I.bound(0, 2, null));
    }
}
