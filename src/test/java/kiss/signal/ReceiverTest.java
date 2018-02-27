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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import kiss.Signal;
import kiss.Variable;

/**
 * @version 2017/05/14 12:09:49
 */
public class ReceiverTest {

    @Test
    public void to() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal);

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
    }

    @Test
    public void toCollection() {
        Subject<Integer, Integer> subject = new Subject<>();
        LinkedHashSet<Integer> set = subject.signal().to(LinkedHashSet.class);

        subject.emit(30);
        subject.emit(20);
        subject.emit(10);

        Iterator<Integer> iterator = set.iterator();
        assert iterator.next() == 30;
        assert iterator.next() == 20;
        assert iterator.next() == 10;
    }

    @Test
    public void toAlternate() {
        Subject<Integer, Integer> subject = new Subject<>();
        Set<Integer> set = subject.signal().toAlternate();

        subject.emit(10);
        assert set.size() == 1;

        subject.emit(20);
        assert set.size() == 2;

        // duplicate
        subject.emit(10);
        assert set.size() == 1;

        subject.emit(20);
        assert set.size() == 0;

        // again
        subject.emit(10);
        assert set.size() == 1;

        subject.emit(20);
        assert set.size() == 2;
    }

    @Test
    public void toBinary() {
        Subject<Integer, Integer> subject = new Subject<>();
        Variable<Boolean> binary = subject.signal().toBinary();

        subject.emit(10);
        assert binary.get() == true;

        subject.emit(20);
        assert binary.get() == false;

        subject.emit(30);
        assert binary.get() == true;

        subject.emit(10);
        assert binary.get() == false;
    }

    @Test
    public void toList() {
        Subject<Integer, Integer> subject = new Subject<>();
        List<Integer> list = subject.signal().toList();

        subject.emit(10);
        assert list.size() == 1;
        assert list.get(0) == 10;

        subject.emit(20);
        assert list.size() == 2;
        assert list.get(1) == 20;

        subject.emit(30);
        assert list.size() == 3;
        assert list.get(2) == 30;

        // duplicate
        subject.emit(10);
        assert list.size() == 4;
        assert list.get(3) == 10;
    }

    @Test
    public void toMultiList() {
        Subject<Integer, Integer> subject = new Subject<>();
        Signal<Integer> signal = subject.signal();
        List<Integer> list1 = signal.toList();
        List<Integer> list2 = signal.toList();

        subject.emit(10);
        assert list1.size() == 1;
        assert list1.get(0) == 10;
        assert list2.size() == 1;
        assert list2.get(0) == 10;

        subject.emit(20);
        assert list1.size() == 2;
        assert list1.get(1) == 20;
        assert list2.size() == 2;
        assert list2.get(1) == 20;
    }

    @Test
    public void toMap() {
        Subject<Integer, Integer> subject = new Subject<>();
        Map<String, Integer> map = subject.signal().toMap(v -> String.valueOf(v));

        subject.emit(10);
        assert map.size() == 1;
        assert map.get("10") == 10;

        subject.emit(20);
        assert map.size() == 2;
        assert map.get("20") == 20;

        // duplicate
        subject.emit(10);
        assert map.size() == 2;
        assert map.get("10") == 10;
    }

    @Test
    public void toSet() {
        Subject<Integer, Integer> subject = new Subject<>();
        Set<Integer> set = subject.signal().toSet();

        subject.emit(10);
        assert set.size() == 1;

        subject.emit(20);
        assert set.size() == 2;

        subject.emit(30);
        assert set.size() == 3;

        // duplicate
        subject.emit(10);
        assert set.size() == 3;
    }
}
