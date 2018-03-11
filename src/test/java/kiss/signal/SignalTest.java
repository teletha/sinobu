/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.Test;

import antibug.Chronus;
import kiss.I;

/**
 * @version 2018/03/04 13:54:27
 */
public class SignalTest {

    public static final Chronus chronus = new Chronus(I.class);

    @Test
    public void toggle() {
        Subject<String, Boolean> subject = new Subject<>(signal -> signal.toggle());
        assert subject.emitAndRetrieve("1") == true;
        assert subject.emitAndRetrieve("2") == false;
        assert subject.emitAndRetrieve("3") == true;
        assert subject.emitAndRetrieve("4") == false;
    }

    @Test
    public void toggleWithInitialValue() {
        Subject<String, Boolean> subject = new Subject<>(signal -> signal.toggle(false));
        assert subject.emitAndRetrieve("1") == false;
        assert subject.emitAndRetrieve("2") == true;
        assert subject.emitAndRetrieve("3") == false;
        assert subject.emitAndRetrieve("4") == true;
    }

    @Test
    public void toggleWithValues() {
        Subject<Integer, String> subject = new Subject<>(signal -> signal.toggle("one", "other"));
        assert subject.emitAndRetrieve(1).equals("one");
        assert subject.emitAndRetrieve(2).equals("other");
        assert subject.emitAndRetrieve(3).equals("one");
        assert subject.emitAndRetrieve(4).equals("other");
    }

    @Test
    public void toggleWithValuesMore() {
        Subject<Integer, String> subject = new Subject<>(signal -> signal.toggle("one", "two", "three"));
        assert subject.emitAndRetrieve(1).equals("one");
        assert subject.emitAndRetrieve(2).equals("two");
        assert subject.emitAndRetrieve(3).equals("three");
        assert subject.emitAndRetrieve(4).equals("one");
        assert subject.emitAndRetrieve(5).equals("two");
        assert subject.emitAndRetrieve(6).equals("three");
    }

    @Test
    public void toggleWithNull() {
        Subject<Integer, String> subject = new Subject<>(signal -> signal.toggle("one", null));
        assert subject.emitAndRetrieve(1).equals("one");
        assert subject.emitAndRetrieve(2) == null;
        assert subject.emitAndRetrieve(3).equals("one");
        assert subject.emitAndRetrieve(4) == null;
    }

    @Test
    public void disposeFrom() {
        Store<Integer> store = new Store();
        I.signal(1, 2, 3, 4).effect(store::before).take(1).to(store::after);

        assert store.size() == 1;
    }

    @Test
    public void disposeFlatMap() {
        Store<Integer> store = new Store();
        I.signal(10, 20, 30, 40).flatMap(v -> I.signal(v, v + 1, v + 2)).effect(store::before).take(2).to(store::after);

        assert store.size() == 2;
        assert store.retrieve() == 10;
        assert store.retrieve() == 11;
    }

    @Test
    public void disposeInternalFlatMap() {
        Store<Integer> store = new Store();
        I.signal(10, 20).flatMap(v -> I.signal(v, v + 1, v + 2).take(2)).to(store::before);

        assert store.before.size() == 4;
        assert store.retrieve() == 10;
        assert store.retrieve() == 11;
        assert store.retrieve() == 20;
        assert store.retrieve() == 21;
    }

    @Test
    public void disposeFlatArray() {
        Store<Integer> store = new Store();
        I.signal(10, 20, 30, 40).flatArray(v -> new Integer[] {v, v + 1, v + 2}).effect(store::before).take(2).to(store::after);

        assert store.size() == 2;
        assert store.retrieve() == 10;
        assert store.retrieve() == 11;
    }

    /**
     * @version 2017/03/18 21:00:47
     */
    @SuppressWarnings("unused")
    private static class Store<T> {

        private Deque<T> before = new ArrayDeque();

        private int beforeCount;

        public void before(T item) {
            before.add(item);
        }

        public void beforeCount() {
            beforeCount++;
        }

        private Deque<T> after = new ArrayDeque();

        private int afterCount;

        public void after(T item) {
            after.add(item);
        }

        public void afterCount() {
            afterCount++;
        }

        private int size() {
            assert before.size() == after.size();
            return before.size();
        }

        private T retrieve() {
            return before.pollFirst();
        }
    }
}
