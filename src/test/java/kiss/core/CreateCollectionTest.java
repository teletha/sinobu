/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import kiss.I;

/**
 * @version 2017/03/20 9:36:00
 */
public class CreateCollectionTest {

    @Test
    public void list() {
        List<Integer> list = I.list(1, 2, 3);
        assert list instanceof ArrayList;
        assert list.size() == 3;
        assert list.get(0) == 1;
        assert list.get(1) == 2;
        assert list.get(2) == 3;
    }

    @Test
    public void listEmpty() {
        List<Integer> list = I.list();
        assert list instanceof ArrayList;
        assert list.size() == 0;
    }

    @Test
    public void set() {
        Set<Integer> set = I.set(1, 2, 3);
        assert set instanceof HashSet;
        assert set.size() == 3;
    }

    @Test
    public void setEmpty() {
        Set<Integer> set = I.set();
        assert set instanceof HashSet;
        assert set.size() == 0;
    }

    @Test
    public void collect() {
        List<Integer> list = I.collect(LinkedList.class, 1, 2, 3);
        assert list instanceof LinkedList;
        assert list.size() == 3;
        assert list.get(0) == 1;
        assert list.get(1) == 2;
        assert list.get(2) == 3;
    }

    @Test
    public void collectEMpty() {
        List<Integer> list = I.collect(LinkedList.class);
        assert list instanceof LinkedList;
        assert list.size() == 0;
    }

    @Test(expected = NullPointerException.class)
    public void collectNull() {
        I.collect((Class) null);
    }
}
