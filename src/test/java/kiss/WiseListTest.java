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

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class WiseListTest {

    @Test
    void peekFirst() {
        WiseList<Integer> list = I.list(ArrayList.class);
        assert list.first().isAbsent();

        list.add(1);
        assert list.first().isPresent();
    }

    @Test
    void select() {
        WiseList<Integer> list = I.list(1, 2, 3, 4, 5);
        WiseList<Integer> selected = list.take(v -> v % 2 == 0);
        assert selected.size() == 2;
        assert selected instanceof ArrayList;
    }
}
