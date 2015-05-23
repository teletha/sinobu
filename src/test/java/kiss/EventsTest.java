/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.ClassRule;
import org.junit.Test;

import antibug.Chronus;

/**
 * @version 2015/05/23 17:03:30
 */
public class EventsTest {

    @ClassRule
    public static final Chronus chronus = new Chronus(I.class);

    @Test
    public void to() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
    }

    @Test
    public void as() {
        EventFacade<Number, Integer> facade = new EventFacade<>(events -> events.as(Integer.class));

        assert facade.emitAndRetrieve(10).intValue() == 10;
        assert facade.emitAndRetrieve(2.1F) == null;
        assert facade.emitAndRetrieve(-1.1D) == null;
        assert facade.emitAndRetrieve(20L) == null;
        assert facade.dispose();
    }

    @Test
    public void buffer() {
        EventFacade<Integer, List<Integer>> facade = new EventFacade<>(events -> events.buffer(2));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieveAsList(20, 10, 20);
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieveAsList(40, 30, 40);
        assert facade.dispose();
    }

    @Test
    public void bufferRepeat() {
        EventFacade<Integer, List<Integer>> facade = new EventFacade<>(events -> {
            return events.buffer(2).skip(1).take(1).repeat();
        });

        facade.emit(10);
        assert facade.emitAndRetrieve(20) == null;
        facade.emit(30);
        assert facade.emitAndRetrieveAsList(40, 30, 40);
        facade.emit(50);
        assert facade.emitAndRetrieve(60) == null;
        facade.emit(70);
        assert facade.emitAndRetrieveAsList(80, 70, 80);
        assert facade.disposeWithCountAlreadyDisposed(2);
    }

    @Test
    public void bufferInterval1() {
        EventFacade<Integer, List<Integer>> facade = new EventFacade<>(events -> events.buffer(2, 1));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieveAsList(20, 10, 20);
        assert facade.emitAndRetrieveAsList(30, 20, 30);
        assert facade.emitAndRetrieveAsList(40, 30, 40);
        assert facade.dispose();
    }

    @Test
    public void bufferInterval2() {
        EventFacade<Integer, List<Integer>> facade = new EventFacade<>(events -> events.buffer(2, 3));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieveAsList(30, 20, 30);
        assert facade.emitAndRetrieve(40) == null;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieveAsList(60, 50, 60);
        assert facade.dispose();
    }

    @Test
    public void bufferTime() throws Exception {
        EventFacade<Integer, List<Integer>> facade = new EventFacade<>(events -> events.buffer(30, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        // chronus.freeze(30);
        Thread.sleep(300);
        assert facade.retrieveAsList(10, 20);
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == null;
        assert facade.emitAndRetrieve(50) == null;
        // chronus.freeze(30);
        Thread.sleep(300);
        assert facade.retrieveAsList(30, 40, 50);
    }

    @Test
    public void combine() {
        EventFacade<Integer, Integer> sub = new EventFacade<>();
        EventFacade<Integer, Integer> facade = new EventFacade<Integer, Integer>(events -> events
                .combine(sub.observe(), (base, other) -> base + other));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        sub.emit(100);
        assert facade.retrieve() == 110;

        sub.emit(200);
        assert facade.retrieve() == 220;

        sub.emit(300);
        assert facade.retrieve() == 330;

        sub.emit(400);
        assert facade.retrieve() == null;
        assert facade.emitAndRetrieve(40) == 440;

        assert facade.dispose();
        assert sub.isCompleted();
    }

    @Test
    public void combineLatest() {
        EventFacade<Integer, Integer> sub = new EventFacade<>();
        EventFacade<Integer, Integer> facade = new EventFacade<Integer, Integer>(events -> events
                .combineLatest(sub.observe(), (base, other) -> base + other));

        assert facade.emitAndRetrieve(1) == null;
        assert facade.emitAndRetrieve(2) == null;

        sub.emit(10);
        assert facade.retrieve() == 12; // 10 + 2
        assert facade.emitAndRetrieve(3) == 13; // 10 + 3

        sub.emit(20);
        assert facade.retrieve() == 23; // 20 + 3
        assert facade.emitAndRetrieve(4) == 24; // 20 + 4

        assert facade.dispose();
        assert sub.isCompleted();
    }

    @Test
    public void debounce() throws Exception {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.debounce(10, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        chronus.await();
        assert facade.retrieve() == 30;

        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(10) == null;

        chronus.await();
        assert facade.retrieve() == 10;
    }

    @Test
    public void debounceRepeat() throws Exception {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.debounce(10, MILLISECONDS)
                .skip(1)
                .take(1)
                .repeat());

        assert facade.emitAndRetrieve(11) == null;
        assert facade.emitAndRetrieve(22) == null;
        assert facade.emitAndRetrieve(33) == null;

        chronus.await();
        assert facade.retrieve() == null;

        facade.emit(44);
        chronus.await();
        assert facade.retrieve() == 44;

        facade.emit(55);
        chronus.await();
        assert facade.retrieve() == null;
        facade.emit(66);
        chronus.await();
        assert facade.retrieve() == 66;
    }

    @Test
    public void delay() throws Exception {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.delay(10, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == null;
        chronus.await();
        assert facade.retrieve() == 10;

        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;
        chronus.await();
        assert facade.retrieve() == 20;
        assert facade.retrieve() == 30;
    }

    @Test
    public void diff() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.diff());

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
    }

    @Test
    public void distinct() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.distinct());

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.dispose();
    }

    @Test
    public void distinctRepeat() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.distinct().take(2).repeat());

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        assert facade.disposeWithCountAlreadyDisposed(2);
    }

    @Test
    public void filter() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.filter(value -> value % 2 == 0));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(15) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(25) == null;
        assert facade.dispose();
    }

    @Test
    public void filterWithPreviousValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(e -> e.filter(0, (prev, value) -> value - prev > 5));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(11) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(22) == null;
        assert facade.dispose();
    }

    @Test
    public void filterByEvent() {
        EventFacade<Boolean, Boolean> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.filter(condition.observe()));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        condition.emit(true);
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        condition.emit(false);
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        assert facade.dispose();
        assert condition.isCompleted();
    }

    @Test
    public void flatMap() {
        EventFacade<Integer, Integer> facade = new EventFacade<>((events, that) -> events
                .flatMap(value -> that.observeWith(value, value + 1)));

        facade.emit(10);
        assert facade.retrieve() == 10;
        assert facade.retrieve() == 11;
        facade.emit(20);
        assert facade.retrieve() == 20;
        assert facade.retrieve() == 21;
        assert facade.dispose();
    }

    @Test
    public void just() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> Events.just(1, 2, 3));

        assert facade.retrieve() == 1;
        assert facade.retrieve() == 2;
        assert facade.retrieve() == 3;
    }

    @Test
    public void map() throws Exception {
        EventFacade<Integer, Integer> facade = new EventFacade<Integer, Integer>(events -> events
                .map(value -> value * 2));

        assert facade.emitAndRetrieve(10) == 20;
        assert facade.emitAndRetrieve(20) == 40;
        assert facade.emitAndRetrieve(30) == 60;
        assert facade.dispose();
    }

    @Test
    public void mapConstant() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.map(100));

        assert facade.emitAndRetrieve(1) == 100;
        assert facade.emitAndRetrieve(2) == 100;
        assert facade.emitAndRetrieve(3) == 100;
        assert facade.dispose();
    }

    @Test
    public void mapWithPreviousValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.map(1, (prev, now) -> prev + now));

        assert facade.emitAndRetrieve(1) == 2;
        assert facade.emitAndRetrieve(2) == 3;
        assert facade.emitAndRetrieve(3) == 5;
        assert facade.dispose();
    }

    @Test
    public void merge() {
        EventFacade<Integer, Integer> facade2 = new EventFacade<>();
        EventFacade<Integer, Integer> facade1 = new EventFacade<>(events -> events.merge(facade2.observe()));

        // from facade1
        assert facade1.emitAndRetrieve(10) == 10;
        assert facade1.emitAndRetrieve(20) == 20;

        // from facade2
        facade2.emit(100);
        facade2.emit(200);
        assert facade1.retrieve() == 100;
        assert facade1.retrieve() == 200;

        assert facade1.dispose();
        assert facade2.isCompleted();
    }

    @Test
    public void mergeIterable() {
        EventFacade<Integer, Integer> facade4 = new EventFacade<>();
        EventFacade<Integer, Integer> facade3 = new EventFacade<>();
        EventFacade<Integer, Integer> facade2 = new EventFacade<>();

        List<Events<Integer>> list = new ArrayList();
        list.add(facade2.observe());
        list.add(facade3.observe());
        list.add(facade4.observe());

        EventFacade<Integer, Integer> facade1 = new EventFacade<>(events -> events.merge(list));

        // from main
        assert facade1.emitAndRetrieve(10) == 10;

        // from sub
        facade2.emit(100);
        facade3.emit(200);
        facade4.emit(300);
        assert facade1.retrieve() == 100;
        assert facade1.retrieve() == 200;
        assert facade1.retrieve() == 300;

        assert facade1.dispose();
        assert facade2.isCompleted();
        assert facade3.isCompleted();
        assert facade4.isCompleted();
    }

    @Test
    public void mergeNull() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.merge((Events) null));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.dispose();
    }

    @Test
    public void never() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> Events.NEVER);

        assert facade.emitAndRetrieve(0) == null;
        assert facade.emitAndRetrieve(1) == null;
        assert facade.emitAndRetrieve(2) == null;
    }

    @Test
    public void on() {
        Set<Integer> sideEffects = new HashSet();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.on((observer, value) -> {
            sideEffects.add(value);
            observer.accept(value);
        }));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert sideEffects.contains(10);
        assert sideEffects.contains(20);
        assert facade.dispose();
    }

    @Test
    public void repeat() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1).take(1).repeat());

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;

        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == 60;

        assert facade.disposeWithCountAlreadyDisposed(3);
    }

    @Test
    public void repeatFinitely() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1).take(1).repeat(2));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;

        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == null;

        assert facade.isCompleted();
    }

    @Test
    public void repeatThen() {
        EventFacade<Integer, Integer> sub = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1)
                .take(2)
                .repeat()
                .merge(sub.observe()));

        // from main facade
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == 50;

        // from sub facade
        sub.emit(100);
        assert facade.retrieve() == 100;
        sub.emit(200);
        assert facade.retrieve() == 200;

        assert facade.disposeWithCountAlreadyDisposed(2);
        assert sub.isCompleted();

        // from sub facade
        sub.emit(300);
        assert facade.retrieve() == null;
    }

    @Test
    public void scan() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events
                .scan(10, (accumulated, value) -> accumulated + value));

        assert facade.emitAndRetrieve(1) == 11; // 10 + 1
        assert facade.emitAndRetrieve(2) == 13; // 11 + 2
        assert facade.emitAndRetrieve(3) == 16; // 13 + 3
        assert facade.dispose();
    }

    @Test
    public void skip() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.dispose();
    }

    @Test
    public void skipUntil() {
        EventFacade<String, String> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(condition.observe()));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
        assert condition.isCompleted();
    }

    @Test
    public void skipUntilWithRepeat() throws Exception {
        EventFacade<String, String> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(condition.observe())
                .take(1)
                .repeat());

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        condition.emit("start");
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        assert facade.disposeWithCountAlreadyDisposed(2);
        assert condition.isCompleted();
    }

    @Test
    public void skipUntilCondition() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(value -> value % 3 == 0));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
    }

    @Test
    public void skipUntilConditionWithRepeat() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(value -> value % 3 == 0)
                .take(2)
                .repeat());

        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == 60;
        assert facade.emitAndRetrieve(70) == 70;
        assert facade.emitAndRetrieve(80) == null;
        assert facade.disposeWithCountAlreadyDisposed(2);
    }

    @Test
    public void startWith() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.startWith(10));

        assert facade.retrieve() == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
    }

    @Test
    public void startWithTwice() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.startWith(0).startWith(10));

        assert facade.retrieve() == 10;
        assert facade.retrieve() == 0;
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.dispose();
    }

    @Test
    public void take() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.take(1));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.isCompleted();
    }

    @Test
    public void takeUntil() {
        EventFacade<String, String> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.takeUntil(condition.observe()));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        condition.emit("start");
        assert facade.isCompleted();
        assert condition.isCompleted();
    }

    @Test
    public void takeUntilCondition() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.takeUntil(value -> value % 3 == 0));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == null;
        assert facade.isCompleted();
    }

    @Test
    public void takeUntilConditionRepeat() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1)
                .takeUntil(value -> value % 3 == 0)
                .repeat());

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        assert facade.emitAndRetrieve(40) == null;
        assert facade.emitAndRetrieve(60) == 60;

        assert facade.emitAndRetrieve(70) == null;
        assert facade.emitAndRetrieve(80) == 80;
        assert facade.emitAndRetrieve(100) == 100;

        assert facade.disposeWithCountAlreadyDisposed(2);
    }

    @Test
    public void throttle() throws Exception {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.throttle(20, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        Thread.sleep(20);

        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(10) == null;
    }
}
