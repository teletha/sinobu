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

import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.Test;

import antibug.Chronus;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/02/28 19:24:35
 */
public class SignalTest {

    public static final Chronus chronus = new Chronus(I.class);

    @Test
    public void merge() {
        Subject<Integer, Integer> subject2 = new Subject<>();
        Subject<Integer, Integer> subject1 = new Subject<>(signal -> signal.merge(subject2.signal()));

        // from subject1
        assert subject1.emitAndRetrieve(10) == 10;
        assert subject1.emitAndRetrieve(20) == 20;

        // from subject2
        subject2.emit(100);
        subject2.emit(200);
        assert subject1.retrieve() == 100;
        assert subject1.retrieve() == 200;

        assert subject1.dispose();
        assert subject2.isCompleted();
    }

    @Test
    public void mergeIterable() {
        Subject<Integer, Integer> subject4 = new Subject<>();
        Subject<Integer, Integer> subject3 = new Subject<>();
        Subject<Integer, Integer> subject2 = new Subject<>();

        List<Signal<Integer>> list = new ArrayList();
        list.add(subject2.signal());
        list.add(subject3.signal());
        list.add(subject4.signal());

        Subject<Integer, Integer> subject1 = new Subject<>(signal -> signal.merge(list));

        // from main
        assert subject1.emitAndRetrieve(10) == 10;

        // from sub
        subject2.emit(100);
        subject3.emit(200);
        subject4.emit(300);
        assert subject1.retrieve() == 100;
        assert subject1.retrieve() == 200;
        assert subject1.retrieve() == 300;

        assert subject1.dispose();
        assert subject2.isCompleted();
        assert subject3.isCompleted();
        assert subject4.isCompleted();
    }

    @Test
    public void mergeNull() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.merge((Signal) null));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.dispose();
    }

    @Test
    public void never() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> Signal.NEVER);

        assert subject.emitAndRetrieve(0) == null;
        assert subject.emitAndRetrieve(1) == null;
        assert subject.emitAndRetrieve(2) == null;
    }

    @Test
    public void on() throws Exception {
        String mainThread = Thread.currentThread().getName();
        Subject<Integer, String> subject = new Subject<>(signal -> signal.map(v -> mainThread)
                .on(this::runOtherThread)
                .map(v -> v + " - " + Thread.currentThread().getName()));

        subject.emit(10);
        chronus.freeze(10);
        assert subject.retrieve().equals(mainThread + " - other");
    }

    private void runOtherThread(Runnable action) {
        Thread thread = new Thread(action);
        thread.setName("other");
        thread.start();

    }

    @Test
    public void repeat() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1).take(1).repeat());

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;

        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(40) == 40;

        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieve(60) == 60;

        assert subject.disposeWithCountAlreadyDisposed(6);
    }

    @Test
    public void repeatFinitely() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1).take(1).repeat(2));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;

        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(40) == 40;

        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieve(60) == null;

        assert subject.isCompleted();
    }

    @Test
    public void repeatThen() {
        Subject<Integer, Integer> sub = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1).take(2).repeat().merge(sub.signal()));

        // from main subject
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;

        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(40) == 40;
        assert subject.emitAndRetrieve(50) == 50;

        // from sub subject
        sub.emit(100);
        assert subject.retrieve() == 100;
        sub.emit(200);
        assert subject.retrieve() == 200;

        assert subject.disposeWithCountAlreadyDisposed(4);
        assert sub.isCompleted();

        // from sub subject
        sub.emit(300);
        assert subject.retrieve() == null;
    }

    @Test
    public void sampleBySamplersignal() {
        Subject<String, String> sampler = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.sample(sampler.signal()));
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == null;

        sampler.emit("NOW");
        assert subject.retrieve() == 30;

        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(10) == null;

        sampler.emit("NOW");
        assert subject.retrieve() == 10;
        assert subject.dispose();
        assert sampler.isCompleted();
    }

    @Test
    public void scan() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.scan(10, (accumulated, value) -> accumulated + value));

        assert subject.emitAndRetrieve(1) == 11; // 10 + 1
        assert subject.emitAndRetrieve(2) == 13; // 11 + 2
        assert subject.emitAndRetrieve(3) == 16; // 13 + 3
        assert subject.dispose();
    }

    @Test
    public void skipByItems() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(10, 30));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.dispose();
    }

    @Test
    public void skipByItemCollection() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(I.set(10, 30)));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.dispose();
    }

    @Test
    public void skipByCondition() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(value -> value % 3 == 0));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(40) == 40;
        assert subject.emitAndRetrieve(50) == 50;
        assert subject.emitAndRetrieve(60) == null;
        assert subject.dispose();
    }

    @Test
    public void skipByConditionWithPreviousValue() {
        Subject<Integer, Integer> subject = new Subject<>(e -> e.skip(0, (prev, value) -> value - prev > 5));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(11) == 11;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(22) == 22;
        assert subject.dispose();
    }

    @Test
    public void skipByConditionEvent() {
        Subject<Boolean, Boolean> condition = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipWhile(condition.signal()));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;

        condition.emit(true);
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;

        condition.emit(false);
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
        assert condition.isCompleted();
    }

    @Test
    public void skipByCount() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.dispose();
    }

    @Test
    public void skipByTime() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(10, MILLISECONDS));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        chronus.freeze(100);
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
    }

    @Test
    public void skipByTimeWaitingAtFirst() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(10, MILLISECONDS));

        chronus.freeze(100);
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
    }

    @Test
    public void skipUntilValue() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil(30));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
    }

    @Test
    public void skipUntilNullValue() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil((Integer) null));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(null) == null;
        assert subject.emitAndRetrieve(40) == 40;
    }

    @Test
    public void skipUntilValueWithRepeat() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil(30).take(2).repeat());

        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(40) == 40;
        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(40) == 40;
        assert subject.emitAndRetrieve(50) == null;
        assert subject.disposeWithCountAlreadyDisposed(4);
    }

    @Test
    public void skipUntilConditionWithRepeat() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil(value -> value % 3 == 0).take(2).repeat());

        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(40) == 40;
        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieve(60) == 60;
        assert subject.emitAndRetrieve(70) == 70;
        assert subject.emitAndRetrieve(80) == null;
        assert subject.disposeWithCountAlreadyDisposed(4);
    }

    @Test
    public void takeUntilValue() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.takeUntil(30));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(40) == null;
        assert subject.isCompleted();
    }

    @Test
    public void takeUntilNullValue() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.takeUntil((Integer) null));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(null) == null;
        assert subject.emitAndRetrieve(30) == null;
        assert subject.isCompleted();
    }

    @Test
    public void takeUntilValueRepeat() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1).takeUntil(30).repeat());

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;

        assert subject.disposeWithCountAlreadyDisposed(4);
    }

    @Test
    public void takeUntilCondition() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.takeUntil(value -> value % 3 == 0));

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(40) == null;
        assert subject.isCompleted();
    }

    @Test
    public void takeUntilConditionRepeat() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1).takeUntil(value -> value % 3 == 0).repeat());

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(30) == 30;

        assert subject.emitAndRetrieve(40) == null;
        assert subject.emitAndRetrieve(60) == 60;

        assert subject.emitAndRetrieve(70) == null;
        assert subject.emitAndRetrieve(80) == 80;
        assert subject.emitAndRetrieve(100) == 100;

        assert subject.disposeWithCountAlreadyDisposed(4);
    }

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
    public void throttle() {
        Subject<String, String> subject = new Subject<>(signal -> signal.throttle(30, MILLISECONDS));

        chronus.mark();
        assert subject.emitAndRetrieve("OK").equals("OK");

        chronus.freezeFromMark(10, 20, () -> {
            assert subject.emitAndRetrieve("10ms skip") == null;
        });
        chronus.freezeFromMark(20, 30, () -> {
            assert subject.emitAndRetrieve("20ms skip") == null;
        });
        chronus.freezeFromMark(35);
        assert subject.emitAndRetrieve("30ms OK") != null;
        assert subject.emitAndRetrieve("skip") == null;
    }

    @Test
    public void infinite() {
        Subject<Integer, Long> subject = new Subject<>(signal -> I.signal(0, 20, MILLISECONDS).take(2));

        chronus.mark();
        chronus.freezeFromMark(10);
        assert subject.retrieve() == 0;
        assert subject.retrieve() == null;
        chronus.freezeFromMark(30);
        assert subject.retrieve() == 1;
        assert subject.retrieve() == null;

        assert subject.isCompleted();
        assert subject.retrieve() == null;
    }

    @Test
    public void range() throws Exception {
        List<Long> list = I.signalRange(0, 5).toList();
        assert list.get(0) == 0;
        assert list.get(1) == 1;
        assert list.get(2) == 2;
        assert list.get(3) == 3;
        assert list.get(4) == 4;
    }

    @Test
    public void rangeWithStep() throws Exception {
        List<Long> list = I.signalRange(2, 5, 2).toList();
        assert list.get(0) == 2;
        assert list.get(1) == 4;
        assert list.get(2) == 6;
        assert list.get(3) == 8;
        assert list.get(4) == 10;
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
