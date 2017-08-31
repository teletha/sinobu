/*
 * Copyright (C) 2017 Nameless Production Committee
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
import java.util.function.Function;

import org.junit.Test;

import antibug.Chronus;
import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;
import kiss.Ⅲ;

/**
 * @version 2017/05/14 12:12:40
 */
public class SignalTest {

    public static final Chronus chronus = new Chronus(I.class);

    @Test
    public void as() {
        Subject<Number, Integer> subject = new Subject<>(signal -> signal.as(Integer.class));

        assert subject.emitAndRetrieve(10).intValue() == 10;
        assert subject.emitAndRetrieve(2.1F) == null;
        assert subject.emitAndRetrieve(-1.1D) == null;
        assert subject.emitAndRetrieve(20L) == null;
        assert subject.dispose();
    }

    @Test
    public void buffer() {
        Subject<Integer, List<Integer>> subject = new Subject<>(signal -> signal.buffer(2));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieveAsList(20, 10, 20);
        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieveAsList(40, 30, 40);
        assert subject.dispose();
    }

    @Test
    public void bufferRepeat() {
        Subject<Integer, List<Integer>> subject = new Subject<>(signal -> {
            return signal.buffer(2).skip(1).take(1).repeat();
        });

        subject.emit(10);
        assert subject.emitAndRetrieve(20) == null;
        subject.emit(30);
        assert subject.emitAndRetrieveAsList(40, 30, 40);
        subject.emit(50);
        assert subject.emitAndRetrieve(60) == null;
        subject.emit(70);
        assert subject.emitAndRetrieveAsList(80, 70, 80);
        assert subject.disposeWithCountAlreadyDisposed(4);
    }

    @Test
    public void bufferInterval1() {
        Subject<Integer, List<Integer>> subject = new Subject<>(signal -> signal.buffer(2, 1));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieveAsList(20, 10, 20);
        assert subject.emitAndRetrieveAsList(30, 20, 30);
        assert subject.emitAndRetrieveAsList(40, 30, 40);
        assert subject.dispose();
    }

    @Test
    public void bufferInterval2() {
        Subject<Integer, List<Integer>> subject = new Subject<>(signal -> signal.buffer(2, 3));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieveAsList(30, 20, 30);
        assert subject.emitAndRetrieve(40) == null;
        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieveAsList(60, 50, 60);
        assert subject.dispose();
    }

    @Test
    public void bufferTime() {
        Subject<Integer, List<Integer>> subject = new Subject<>(signal -> signal.buffer(30, MILLISECONDS));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        chronus.freeze(50);
        assert subject.retrieveAsList(10, 20);
        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(40) == null;
        assert subject.emitAndRetrieve(50) == null;
        chronus.freeze(50);
        assert subject.retrieveAsList(30, 40, 50);
    }

    @Test
    public void combine() {
        Subject<Integer, Integer> sub = new Subject<>();
        Subject<Integer, Integer> subject = new Subject<Integer, Integer>(signal -> signal
                .combine(sub.signal(), (base, other) -> base + other));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == null;

        sub.emit(100);
        assert subject.retrieve() == 110;

        sub.emit(200);
        assert subject.retrieve() == 220;

        sub.emit(300);
        assert subject.retrieve() == 330;

        sub.emit(400);
        assert subject.retrieve() == null;
        assert subject.emitAndRetrieve(40) == 440;

        assert subject.dispose();
        assert sub.isCompleted();
    }

    @Test
    public void combineComplete() throws Exception {
        Subject<Integer, Integer> other = new Subject<>(signal -> signal);
        Subject<String, Ⅱ<String, Integer>> main = new Subject<>(signal -> signal.combine(other.signal()));

        assert main.isCompleteEventReceieved() == false;
        assert other.isCompleteEventReceieved() == false;

        main.complete();
        assert main.isCompleteEventReceieved() == false;
        assert other.isCompleteEventReceieved() == false;

        other.complete();
        assert main.isCompleteEventReceieved();
        assert other.isCompleteEventReceieved();
    }

    @Test
    public void combineBinary() throws Exception {
        Subject<Integer, Integer> other = new Subject<>();
        Subject<String, Ⅱ<String, Integer>> main = new Subject<>(signal -> signal.combine(other.signal()));

        main.emit("1");
        assert main.retrieve() == null;
        other.emit(10);
        assert main.retrieve().equals(I.pair("1", 10));

        main.emit("2");
        assert main.retrieve() == null;
        other.emit(20);
        assert main.retrieve().equals(I.pair("2", 20));

        other.emit(30);
        assert main.retrieve() == null;
        other.emit(40);
        assert main.retrieve() == null;
        main.emit("3");
        assert main.retrieve().equals(I.pair("3", 30));
        main.emit("4");
        assert main.retrieve().equals(I.pair("4", 40));
    }

    @Test
    public void combineTernary() throws Exception {
        Subject<Integer, Integer> other = new Subject<>();
        Subject<Double, Double> another = new Subject<>();
        Subject<String, Ⅲ<String, Integer, Double>> main = new Subject<>(signal -> signal.combine(other.signal(), another.signal()));

        main.emit("1");
        assert main.retrieve() == null;
        other.emit(10);
        assert main.retrieve() == null;
        another.emit(0.1);
        assert main.retrieve().equals(I.pair("1", 10, 0.1));

        main.emit("2");
        assert main.retrieve() == null;
        other.emit(20);
        assert main.retrieve() == null;
        another.emit(0.2);
        assert main.retrieve().equals(I.pair("2", 20, 0.2));
    }

    @Test
    public void combineLatest() {
        Subject<Integer, Integer> sub = new Subject<>();
        Subject<Integer, Integer> subject = new Subject<Integer, Integer>(signal -> signal
                .combineLatest(sub.signal(), (base, other) -> base + other));

        assert subject.emitAndRetrieve(1) == null;
        assert subject.emitAndRetrieve(2) == null;

        sub.emit(10);
        assert subject.retrieve() == 12; // 10 + 2
        assert subject.emitAndRetrieve(3) == 13; // 10 + 3

        sub.emit(20);
        assert subject.retrieve() == 23; // 20 + 3
        assert subject.emitAndRetrieve(4) == 24; // 20 + 4

        assert subject.dispose();
        assert sub.isCompleted();
    }

    @Test
    public void combineLatestComplete() throws Exception {
        Subject<Integer, Integer> other = new Subject<>(signal -> signal);
        Subject<String, Ⅱ<String, Integer>> main = new Subject<>(signal -> signal.combineLatest(other.signal()));

        assert main.isCompleteEventReceieved() == false;
        assert other.isCompleteEventReceieved() == false;

        main.complete();
        assert main.isCompleteEventReceieved() == false;
        assert other.isCompleteEventReceieved() == false;

        other.complete();
        assert main.isCompleteEventReceieved();
        assert other.isCompleteEventReceieved();
    }

    @Test
    public void combineLatestBinary() throws Exception {
        Subject<Integer, Integer> other = new Subject<>();
        Subject<String, Ⅱ<String, Integer>> main = new Subject<String, Ⅱ<String, Integer>>(signal -> signal.combineLatest(other.signal()));

        main.emit("1");
        assert main.retrieve() == null;
        other.emit(10);
        assert main.retrieve().equals(I.pair("1", 10));

        main.emit("2");
        assert main.retrieve().equals(I.pair("2", 10));
        other.emit(20);
        assert main.retrieve().equals(I.pair("2", 20));

        other.emit(30);
        assert main.retrieve().equals(I.pair("2", 30));
        other.emit(40);
        assert main.retrieve().equals(I.pair("2", 40));
        main.emit("3");
        assert main.retrieve().equals(I.pair("3", 40));
        main.emit("4");
        assert main.retrieve().equals(I.pair("4", 40));
    }

    @Test
    public void combineLatestTernary() throws Exception {
        Subject<Integer, Integer> other = new Subject<>();
        Subject<Double, Double> another = new Subject<>();
        Subject<String, Ⅲ<String, Integer, Double>> main = new Subject<>(signal -> signal.combineLatest(other.signal(), another.signal()));

        main.emit("1");
        assert main.retrieve() == null;
        other.emit(10);
        assert main.retrieve() == null;
        another.emit(0.1);
        assert main.retrieve().equals(I.pair("1", 10, 0.1));

        main.emit("2");
        assert main.retrieve().equals(I.pair("2", 10, 0.1));
        other.emit(20);
        assert main.retrieve().equals(I.pair("2", 20, 0.1));
        another.emit(0.2);
        assert main.retrieve().equals(I.pair("2", 20, 0.2));
    }

    @Test
    public void debounce() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.debounce(30, MILLISECONDS));

        chronus.mark();
        assert subject.emitAndRetrieve(10) == null;
        chronus.freezeFromMark(10);
        assert subject.emitAndRetrieve(20) == null;
        chronus.freezeFromMark(20);
        assert subject.emitAndRetrieve(30) == null;
        chronus.mark();
        assert subject.retrieve() == null;
        chronus.freezeFromMark(10);
        assert subject.retrieve() == null;
        chronus.freezeFromMark(35);
        assert subject.retrieve() != null;

        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(10) == null;

        chronus.await();
        assert subject.retrieve() == 10;
    }

    @Test
    public void debounceRepeat() throws Exception {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.debounce(10, MILLISECONDS).skip(1).take(1).repeat());

        assert subject.emitAndRetrieve(11) == null;
        assert subject.emitAndRetrieve(22) == null;
        assert subject.emitAndRetrieve(33) == null;

        chronus.await();
        assert subject.retrieve() == null;

        subject.emit(44);
        chronus.await();
        assert subject.retrieve() == 44;

        subject.emit(55);
        chronus.await();
        assert subject.retrieve() == null;
        subject.emit(66);
        chronus.await();
        assert subject.retrieve() == 66;
    }

    @Test
    public void diff() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.diff());

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
    }

    @Test
    public void distinct() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.distinct());

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.dispose();
    }

    @Test
    public void distinctRepeat() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.distinct().take(2).repeat());

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;

        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;

        assert subject.disposeWithCountAlreadyDisposed(4);
    }

    @Test
    public void flatArray() {
        Subject<String, String> subject = new Subject<>(signal -> signal.flatArray(v -> v.split("")));

        subject.emit("test");
        assert subject.retrieve().equals("t");
        assert subject.retrieve().equals("e");
        assert subject.retrieve().equals("s");
        assert subject.retrieve().equals("t");
        assert subject.retrieve() == null;
    }

    @Test
    public void flatIterable() {
        Function<String, Iterable<String>> chars = value -> {
            List<String> values = new ArrayList();

            for (int i = 0; i < value.length(); i++) {
                values.add(String.valueOf(value.charAt(i)));
            }
            return values;
        };

        Subject<String, String> subject = new Subject<>(signal -> signal.flatIterable(chars));

        subject.emit("test");
        assert subject.retrieve().equals("t");
        assert subject.retrieve().equals("e");
        assert subject.retrieve().equals("s");
        assert subject.retrieve().equals("t");
        assert subject.retrieve() == null;
    }

    @Test
    public void flatMap() {
        Subject<String, String> emitA = new Subject();
        Subject<String, String> emitB = new Subject();
        Subject<Integer, String> subject = new Subject<>(signal -> signal.flatMap(x -> x == 1 ? emitA.signal() : emitB.signal()));

        subject.emit(1); // connect to emitA
        assert subject.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("1A");
        assert subject.retrieve() == "1A";
        emitB.emit("1B"); // emitB has no relation yet
        assert subject.retrieve() == null;

        subject.emit(2); // connect to emitB
        assert subject.retrieve() == null; // emitB doesn't emit value yet
        emitB.emit("2B");
        assert subject.retrieve() == "2B";
        emitA.emit("2A");
        assert subject.retrieve() == "2A";

        // test disposing
        subject.dispose();
        emitA.emit("Disposed");
        assert subject.retrieve() == null;
        emitB.emit("Disposed");
        assert subject.retrieve() == null;
    }

    @Test
    public void switchMap() {
        Subject<String, String> emitA = new Subject();
        Subject<String, String> emitB = new Subject();
        Subject<Integer, String> subject = new Subject<>(signal -> signal.switchMap(x -> x == 1 ? emitA.signal() : emitB.signal()));

        subject.emit(1); // connect to emitA
        assert subject.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("1A");
        assert subject.retrieve() == "1A";
        emitB.emit("1B"); // emitB has no relation yet
        assert subject.retrieve() == null;

        subject.emit(2); // connect to emitB and disconnect from emitA
        assert subject.retrieve() == null; // emitB doesn't emit value yet
        emitB.emit("2B");
        assert subject.retrieve() == "2B";
        emitA.emit("2A");
        assert subject.retrieve() == null;

        subject.emit(1); // reconnect to emitA and disconnect from emitB
        assert subject.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("3A");
        assert subject.retrieve() == "3A";
        emitB.emit("3B");
        assert subject.retrieve() == null;

        // test disposing
        subject.dispose();
        emitA.emit("Disposed");
        assert subject.retrieve() == null;
    }

    @Test
    public void map() throws Exception {
        Subject<Integer, Integer> subject = new Subject<Integer, Integer>(signal -> signal.map(value -> value * 2));

        assert subject.emitAndRetrieve(10) == 20;
        assert subject.emitAndRetrieve(20) == 40;
        assert subject.emitAndRetrieve(30) == 60;
        assert subject.dispose();
    }

    @Test
    public void mapTo() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.mapTo(100));

        assert subject.emitAndRetrieve(1) == 100;
        assert subject.emitAndRetrieve(2) == 100;
        assert subject.emitAndRetrieve(3) == 100;
        assert subject.dispose();
    }

    @Test
    public void mapWithPreviousValue() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.map(1, (prev, now) -> prev + now));

        assert subject.emitAndRetrieve(1) == 2;
        assert subject.emitAndRetrieve(2) == 3;
        assert subject.emitAndRetrieve(3) == 5;
        assert subject.dispose();
    }

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
    public void repeatTakeUntil() {
        Subject<String, String> condition = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skip(1).take(1).repeat().takeUntil(condition.signal()));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == 20;

        assert subject.emitAndRetrieve(30) == null;
        assert subject.emitAndRetrieve(40) == 40;

        condition.emit("END");
        assert subject.emitAndRetrieve(50) == null;
        assert subject.emitAndRetrieve(60) == null;

        assert subject.isCompleted();
        assert condition.isCompleted();
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
    public void skipUntil() {
        Subject<String, String> condition = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil(condition.signal()));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
        assert condition.isCompleted();
    }

    @Test
    public void skipUntilWithRepeat() throws Exception {
        Subject<String, String> condition = new Subject();
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil(condition.signal()).take(1).repeat());

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == null;

        condition.emit("start");
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == null;

        assert subject.disposeWithCountAlreadyDisposed(4);
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
    public void skipUntilCondition() {
        Subject<Integer, Integer> subject = new Subject<>(signal -> signal.skipUntil(value -> value % 3 == 0));

        assert subject.emitAndRetrieve(10) == null;
        assert subject.emitAndRetrieve(20) == null;
        assert subject.emitAndRetrieve(30) == 30;
        assert subject.emitAndRetrieve(10) == 10;
        assert subject.emitAndRetrieve(20) == 20;
        assert subject.dispose();
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
        Subject<Integer, Integer> subject = new Subject<>(signal -> I.signalInfinite(1, 20, MILLISECONDS).take(2));

        chronus.mark();
        chronus.freezeFromMark(10);
        assert subject.retrieve() == 1;
        assert subject.retrieve() == null;
        chronus.freezeFromMark(30);
        assert subject.retrieve() == 1;
        assert subject.retrieve() == null;

        assert subject.isCompleted();
        assert subject.retrieve() == null;
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
