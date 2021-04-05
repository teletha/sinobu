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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kiss.I;

class BundleFunctionTest {

    /**
     * @see I#bundle(Object...)
     */
    @Test
    void bundleRunnables() {
        int[] value = {0};

        Runnable bundled = I.bundle(() -> value[0] += 1, () -> value[0] += 2);
        bundled.run();
        assert value[0] == 3;

        bundled.run();
        assert value[0] == 6;
    }

    /**
     * @see I#bundle(Object...)
     */
    @Test
    void returnLastInterfaceResult() {
        Function<Integer, Integer> bundled = I.bundle(x -> x + 1, y -> y + 2);
        assert bundled.apply(10) == 12;
    }

    /**
     * @see I#bundle(Iterable)
     */
    @Test
    void bundleRunnableList() {
        int[] value = {0};

        Runnable bundled = I.bundle(List.of(() -> value[0] += 1, () -> value[0] += 2));
        bundled.run();
        assert value[0] == 3;

        bundled.run();
        assert value[0] == 6;
    }

    @Test
    void generic() {
        AtomicInteger value = new AtomicInteger();
        assert value.get() == 0;

        Consumer<Integer> bundled = I.bundle(v -> value.addAndGet(v), v -> value.addAndGet(v * 2));
        bundled.accept(10);
        assert value.get() == 30;
    }

    @Test
    void genericList() {
        AtomicInteger value = new AtomicInteger();
        assert value.get() == 0;

        List<Consumer<Integer>> list = I.list(v -> value.addAndGet(v), v -> value.addAndGet(v * 2));
        Consumer<Integer> bundled = I.bundle(list);
        bundled.accept(10);
        assert value.get() == 30;
    }

    /**
     * @see I#bundle(Object...)
     */
    @Test
    void cantInferCommonInterface() {
        ArrayList array = new ArrayList();
        LinkedList linked = new LinkedList();

        assertThrows(IllegalArgumentException.class, () -> I.bundle(array, linked));
    }

    /**
     * @see I#bundle(Class, Object...)
     */
    @Test
    void bundleList() {
        ArrayList array = new ArrayList();
        LinkedList linked = new LinkedList();

        List bundled = I.bundle(List.class, array, linked);
        bundled.add(10);

        assert array.size() == 1;
        assert linked.size() == 1;
    }

    /**
     * @see I#bundle(Class, Iterable)
     */
    @Test
    void bundleCollectionList() {
        HashSet<Integer> set = new HashSet();
        LinkedList<Integer> linked = new LinkedList();

        Collection<Integer> bundled = I.bundle(Collection.class, set, linked);
        bundled.add(10);

        assert set.size() == 1;
        assert linked.size() == 1;
    }

    @Test
    void error() {
        ArrayList<String> array = new ArrayList();
        List<String> bundle = I.bundle(List.class, array);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            bundle.set(10, "Bundle will throw not UndeclaredThrowableException but IndexOutOfBoundsException.");
        });
    }
}