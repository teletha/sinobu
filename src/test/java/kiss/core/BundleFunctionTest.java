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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;
import kiss.I;

/**
 * @version 2018/03/31 22:14:28
 */
public class BundleFunctionTest {

    @Test
    public void function() {
        AtomicInteger value = new AtomicInteger();
        assert value.get() == 0;

        Runnable bundled = I.bundle(runnable(() -> value.addAndGet(1)), runnable(() -> value.addAndGet(2)));
        bundled.run();
        assert value.get() == 3;
    }

    @Test
    public void functionList() {
        AtomicInteger value = new AtomicInteger();
        assert value.get() == 0;

        List<Runnable> list = I.list(runnable(() -> value.addAndGet(1)), runnable(() -> value.addAndGet(2)));
        Runnable bundled = I.bundle(list);
        bundled.run();
        assert value.get() == 3;
    }

    @Test
    public void generic() {
        AtomicInteger value = new AtomicInteger();
        assert value.get() == 0;

        Consumer<Integer> bundled = I.bundle(consumer(v -> value.addAndGet(v)), consumer(v -> value.addAndGet(v * 2)));
        bundled.accept(10);
        assert value.get() == 30;
    }

    @Test
    public void genericList() {
        AtomicInteger value = new AtomicInteger();
        assert value.get() == 0;

        List<Consumer<Integer>> list = I.list(consumer(v -> value.addAndGet(v)), consumer(v -> value.addAndGet(v * 2)));
        Consumer<Integer> bundled = I.bundle(list);
        bundled.accept(10);
        assert value.get() == 30;
    }

    @ExpectThrow(IllegalArgumentException.class)
    public void byClass() {
        ArrayList array = new ArrayList();
        LinkedList linked = new LinkedList();
        I.bundle(array, linked);
    }

    @Test
    public void byInterface1() {
        ArrayList array = new ArrayList();
        LinkedList linked = new LinkedList();
        List bundle = I.bundle(List.class, array, linked);
        bundle.add(10);
        assert array.size() == 1;
        assert linked.size() == 1;
    }

    @Test
    public void byInterface2() {
        ArrayList<String> array = new ArrayList();
        LinkedList<String> linked = new LinkedList();
        Collection<String> bundle = I.bundle(Collection.class, array, linked);
        bundle.add("test");
        assert array.size() == 1;
        assert linked.size() == 1;
    }

    private Runnable runnable(Runnable runnable) {
        return runnable;
    }

    private Consumer<Integer> consumer(Consumer<Integer> consumer) {
        return consumer;
    }
}
