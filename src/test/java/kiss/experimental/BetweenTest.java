/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BetweenTest {

    @Test
    void between() {
        assert Between.between(1, 3, 5);
        assert Between.between(4.1, 5.5, 7.089);
        assert Between.between("m", "o", "x");

        assert Between.between(1, 0, 5) == false;
        assert Between.between(1, 3, 2) == false;
        assert Between.between(4, 3, 5) == false;
        assert Between.between(5, 3, 1) == false;
    }

    @Test
    void betweenNullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> Between.between(null, 2, 5));
        Assertions.assertThrows(NullPointerException.class, () -> Between.between(1, null, 5));
        Assertions.assertThrows(NullPointerException.class, () -> Between.between(0, 2, null));
    }

    @Test
    void bound() {
        assert Between.bound(1, 3, 5) == 3;
        assert Between.bound(4.1, 5.5, 7.089) == 5.5;
        assert Between.bound("m", "o", "x") == "o";

        assert Between.bound(1, 0, 5) == 1;
        assert Between.bound(1, 3, 2) == 2;
        assert Between.bound(4, 3, 5) == 4;
        assert Between.bound(5, 3, 1) == 5;
    }

    @Test
    void boundNullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> Between.bound(null, 2, 5));
        Assertions.assertThrows(NullPointerException.class, () -> Between.bound(1, null, 5));
        Assertions.assertThrows(NullPointerException.class, () -> Between.bound(0, 2, null));
    }
}