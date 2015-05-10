/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.Property;

import org.junit.ClassRule;
import org.junit.Test;

import antibug.Chronus;

/**
 * @version 2014/01/11 2:51:33
 */
public class EventsTest {

    @ClassRule
    public static final Chronus chronus = new Chronus(I.class);

    @Test
    public void subscribe() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;

        unsubscribe.dispose();
        assert emitter.emitAndRetrieve(30) == null;
    }

    @Test
    public void skip() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skip(1).to(emitter);

        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;

        unsubscribe.dispose();
        assert emitter.emitAndRetrieve(30) == null;
    }

    @Test
    public void skipUntil() throws Exception {
        EventEmitter<String> condition = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skipUntil(condition.observe()).to(emitter);

        assert condition.isSubscribed();
        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;

        unsubscribe.dispose();
        assert condition.isUnsubscribed() == true;
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(10) == null;
    }

    @Test
    public void skipUntilCondition() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skipUntil(v -> {
            return v % 3 == 0;
        }).to(emitter);

        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(30) == 30;
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;

        unsubscribe.dispose();
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(10) == null;
    }

    @Test
    public void skipUntilConditionRepeat() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skipUntil(v -> {
            return v % 3 == 0;
        }).take(2).repeat().to(emitter);

        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(30) == 30;
        assert emitter.emitAndRetrieve(40) == 40;
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(60) == 60;
        assert emitter.emitAndRetrieve(90) == 90;
        assert emitter.emitAndRetrieve(100) == null;

        unsubscribe.dispose();
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(30) == null;
    }

    @Test
    public void skipUntilRepeat() throws Exception {
        EventEmitter<String> condition = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skipUntil(condition.observe()).take(1).repeat().to(emitter);

        assert condition.isSubscribed();
        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == null;

        unsubscribe.dispose();
        assert condition.isUnsubscribed() == true;
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(10) == null;
    }

    /**
     * Helper method to assert list items.
     * 
     * @param list
     * @param items
     */
    private <V> void assertList(List<V> list, V... items) {
        assert list.size() == items.length;

        for (int i = 0; i < items.length; i++) {
            assert list.get(i) == items[i];
        }
    }

    @Test
    public void buffer() throws Exception {
        EventEmitter<List<Integer>> reciever = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().buffer(2).to(reciever);

        emitter.emit(10);
        assert reciever.retrieve() == null;
        emitter.emit(20);
        assertList(reciever.retrieve(), 10, 20);

        emitter.emit(30);
        assert reciever.retrieve() == null;
        emitter.emit(40);
        assertList(reciever.retrieve(), 30, 40);

        unsubscribe.dispose();
        assert emitter.isUnsubscribed();

        emitter.emit(50);
        emitter.emit(60);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferRepeat() throws Exception {
        EventEmitter<List<Integer>> reciever = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().buffer(2).skip(1).take(1).repeat().to(reciever);

        emitter.emit(10);
        emitter.emit(20);
        assert reciever.retrieve() == null;
        emitter.emit(30);
        emitter.emit(40);
        assertList(reciever.retrieve(), 30, 40);

        emitter.emit(50);
        emitter.emit(60);
        assert reciever.retrieve() == null;
        emitter.emit(70);
        emitter.emit(80);
        assertList(reciever.retrieve(), 70, 80);

        unsubscribe.dispose();
        assert emitter.isUnsubscribed();

        emitter.emit(90);
        emitter.emit(100);
        emitter.emit(110);
        emitter.emit(120);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferInterval1() throws Exception {
        EventEmitter<List<Integer>> reciever = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().buffer(2, 1).to(reciever);

        emitter.emit(10);
        assert reciever.retrieve() == null;
        emitter.emit(20);
        assertList(reciever.retrieve(), 10, 20);

        emitter.emit(30);
        assertList(reciever.retrieve(), 20, 30);
        emitter.emit(40);
        assertList(reciever.retrieve(), 30, 40);

        unsubscribe.dispose();
        assert emitter.isUnsubscribed();

        emitter.emit(50);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferInterval2() throws Exception {
        EventEmitter<List<Integer>> reciever = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().buffer(2, 3).to(reciever);

        emitter.emit(10);
        assert reciever.retrieve() == null;
        emitter.emit(20);
        assert reciever.retrieve() == null;
        emitter.emit(30);
        assertList(reciever.retrieve(), 20, 30);
        emitter.emit(40);
        assert reciever.retrieve() == null;
        emitter.emit(50);
        assert reciever.retrieve() == null;
        emitter.emit(60);
        assertList(reciever.retrieve(), 50, 60);

        unsubscribe.dispose();
        assert emitter.isUnsubscribed();

        emitter.emit(70);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferTime() throws Exception {
        EventEmitter<List<Integer>> reciever = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().buffer(30, MILLISECONDS).to(reciever);

        emitter.emit(10);
        emitter.emit(20);
        // chronus.freeze(30);
        Thread.sleep(300);
        emitter.emit(30);
        emitter.emit(40);
        emitter.emit(50);
        // chronus.freeze(30);
        Thread.sleep(300);
        emitter.emit(60);

        assertList(reciever.retrieve(), 10, 20);
        assertList(reciever.retrieve(), 30, 40, 50);
    }

    @Test
    public void as() throws Exception {
        EventEmitter<Integer> reciever = new EventEmitter();
        EventEmitter<Number> emitter = new EventEmitter();
        emitter.observe().as(Integer.class).to(reciever);

        emitter.emit(10);
        assert reciever.retrieve() == 10;

        emitter.emit(2.1F);
        assert reciever.retrieve() == null;
        emitter.emit(-1.1D);
        assert reciever.retrieve() == null;
        emitter.emit(20L);
        assert reciever.retrieve() == null;
    }

    @Test
    public void diff() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().diff().to(emitter);

        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;

        unsubscribe.dispose();
        assert emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(10) == null;
    }

    @Test
    public void flatMap() {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().flatMap(value -> {
            return Events.just(value, value + 1);
        }).to(emitter);

        emitter.emit(10);
        assert emitter.retrieve() == 10;
        assert emitter.retrieve() == 11;

        emitter.emit(20);
        assert emitter.retrieve() == 20;
        assert emitter.retrieve() == 21;

        unsubscribe.dispose();
        emitter.emit(30);
        assert emitter.retrieve() == null;
    }

    @Test
    public void map() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().map((Function<Integer, Integer>) value -> {
            return value * 2;
        }).to(emitter);

        assert emitter.emitAndRetrieve(10) == 20;
        assert emitter.emitAndRetrieve(20) == 40;

        unsubscribe.dispose();
        assert emitter.emitAndRetrieve(30) == null;
    }

    @Test
    public void take() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().take(1).to(emitter);

        assert!emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(20) == null;
    }

    @Test
    public void takeUntil() throws Exception {
        EventEmitter<String> condition = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().takeUntil(condition.observe()).to(emitter);

        assert condition.isSubscribed();
        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;

        condition.emit("start");
        assert condition.isUnsubscribed() == true;
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(10) == null;
    }

    @Test
    public void takeUntilCondition() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().takeUntil(v -> {
            return v % 3 == 0;
        }).to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.emitAndRetrieve(30) == 30;

        assert emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(40) == null;
    }

    @Test
    public void takeUntilConditionRepeat() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skip(1).takeUntil(v -> {
            return v % 3 == 0;
        }).repeat().to(emitter);

        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.emitAndRetrieve(30) == 30;

        assert emitter.emitAndRetrieve(40) == null;
        assert emitter.emitAndRetrieve(60) == 60;
        assert emitter.emitAndRetrieve(70) == null;
        assert emitter.emitAndRetrieve(80) == 80;
        assert emitter.emitAndRetrieve(100) == 100;

        unsubscribe.dispose();
        assert emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(110) == null;
    }

    @Test
    public void skipAndTake() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().skip(1).take(1).to(emitter);

        assert!emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(30) == null;
    }

    @Test
    public void repeat() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skip(1).take(1).repeat().to(emitter);

        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(30) == null;
        assert emitter.emitAndRetrieve(40) == 40;
        assert emitter.isSubscribed();

        unsubscribe.dispose();
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(50) == null;
    }

    @Test
    public void repeatFinitely() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().skip(1).take(1).repeat(2).to(emitter);

        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(30) == null;
        assert emitter.emitAndRetrieve(40) == 40;
        assert emitter.isUnsubscribed();
        assert emitter.emitAndRetrieve(30) == null;
    }

    @Test
    public void repeatThen() throws Exception {
        EventEmitter<Integer> sub = new EventEmitter();
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().skip(1).take(2).repeat().merge(sub.observe()).to(emitter);

        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.emitAndRetrieve(30) == 30;
        assert emitter.isSubscribed();
        assert emitter.emitAndRetrieve(30) == null;
        assert emitter.emitAndRetrieve(40) == 40;
        assert emitter.emitAndRetrieve(50) == 50;
        assert emitter.isSubscribed();

        // from sub
        assert sub.isSubscribed();
        sub.emit(100);
        assert emitter.retrieve() == 100;
        sub.emit(200);
        assert emitter.retrieve() == 200;

        unsubscribe.dispose();
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(50) == null;

        // from sub
        sub.emit(300);
        assert emitter.retrieve() == null;
        assert sub.isUnsubscribed() == true;
    }

    @Test
    public void filter() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().filter(value -> {
            return value % 2 == 0;
        }).to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.emitAndRetrieve(25) == null;
    }

    @Test
    public void throttle() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().throttle(20, MILLISECONDS).to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(10) == null;

        Thread.sleep(20);
        assert emitter.emitAndRetrieve(10) == 10;
    }

    @Test
    public void debounce() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().debounce(10, MILLISECONDS).to(emitter);

        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(30) == null;

        chronus.await();
        assert emitter.retrieve() == 30;
    }

    @Test
    public void debounceRepeat() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().debounce(10, MILLISECONDS).skip(1).take(1).repeat().to(emitter);

        assert emitter.emitAndRetrieve(11) == null;
        assert emitter.emitAndRetrieve(22) == null;
        assert emitter.emitAndRetrieve(33) == null;

        chronus.await();
        assert emitter.retrieve() == null;

        emitter.emit(44);
        chronus.await();
        assert emitter.retrieve() == 44;

        emitter.emit(55);
        chronus.await();
        assert emitter.retrieve() == null;
        emitter.emit(66);
        chronus.await();
        assert emitter.retrieve() == 66;
    }

    @Test
    public void delay() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().delay(10, MILLISECONDS).to(emitter);

        assert emitter.emitAndRetrieve(10) == null;
        chronus.await();
        assert emitter.retrieve() == 10;

        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(30) == null;
        chronus.await();
        assert emitter.retrieve() == 20;
        assert emitter.retrieve() == 30;
    }

    @Test
    public void distinct() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().distinct().to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == null;
        assert emitter.emitAndRetrieve(30) == 30;
    }

    @Test
    public void distinctRepeat() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        Disposable unsubscribe = emitter.observe().distinct().take(2).repeat().to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(10) == null;
        assert emitter.emitAndRetrieve(20) == 20;

        unsubscribe.dispose();
        assert emitter.isUnsubscribed() == true;
        assert emitter.emitAndRetrieve(10) == null;
    }

    @Test
    public void merge() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        Disposable unsubscribe = emitter1.observe().merge(emitter2.observe()).to(emitter1);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();
        assert emitter1.emitAndRetrieve(10) == 10;
        assert emitter1.emitAndRetrieve(20) == 20;

        emitter2.emit(100);
        emitter2.emit(200);
        assert emitter1.retrieve() == 100;
        assert emitter1.retrieve() == 200;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed() == true;
        assert emitter2.isUnsubscribed() == true;
    }

    @Test
    public void mergeIterable() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<Integer> emitter3 = new EventEmitter();
        EventEmitter<Integer> emitter4 = new EventEmitter();

        List<Events<Integer>> list = new ArrayList();
        list.add(emitter2.observe());
        list.add(emitter3.observe());
        list.add(emitter4.observe());

        Disposable unsubscribe = emitter1.observe().merge(list).to(emitter1);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();
        assert emitter3.isSubscribed();
        assert emitter4.isSubscribed();
        assert emitter1.emitAndRetrieve(10) == 10;

        emitter2.emit(100);
        emitter3.emit(200);
        emitter4.emit(300);
        assert emitter1.retrieve() == 100;
        assert emitter1.retrieve() == 200;
        assert emitter1.retrieve() == 300;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed() == true;
        assert emitter2.isUnsubscribed() == true;
        assert emitter3.isUnsubscribed() == true;
        assert emitter4.isUnsubscribed() == true;
    }

    @Test
    public void mergeNull() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        Disposable unsubscribe = emitter1.observe().merge((Events) null).to(emitter1);

        assert emitter1.isSubscribed();
        assert emitter1.emitAndRetrieve(10) == 10;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed() == true;
    }

    @Test
    public void scan() {
        EventSource<Integer> source = new EventSource();
        EventRecorder<Integer> recorder = new EventRecorder();

        // create event stream
        Events<Integer> events = createEventsFrom(source);

        // **declare event handling**
        events.scan(10, (accumulated, value) -> accumulated + value).to(recorder);

        // verify
        assert recorder.retrieveValue() == null;

        source.publish(1);
        assert recorder.retrieveValue() == 11; // 10 + 1

        source.publish(2);
        assert recorder.retrieveValue() == 13; // 11 + 2

        source.publish(3);
        assert recorder.retrieveValue() == 16; // 13 + 3
    }

    @Test
    public void toProperty() {
        EventSource<Integer> source = new EventSource();

        // create event stream
        Property<Integer> property = createEventsFrom(source).to();

        // verify
        assert property.getValue() == null;

        source.publish(10);
        assert property.getValue() == 10;

        source.publish(20);
        assert property.getValue() == 20;
    }

    @Test
    public void all() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<Boolean> reciever = new EventEmitter();

        Disposable unsubscribe = Events.all(value -> {
            return 20 <= value;
        } , emitter1.observe(), emitter2.observe()).to(reciever);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();
        assert reciever.retrieve() == null;

        emitter1.emit(30);
        emitter2.emit(20);
        assert reciever.retrieveLast() == true;

        emitter1.emit(10);
        assert reciever.retrieveLast() == false;

        emitter1.emit(20);
        assert reciever.retrieveLast() == true;

        emitter2.emit(10);
        assert reciever.retrieveLast() == false;

        emitter2.emit(40);
        assert reciever.retrieveLast() == true;

        emitter1.emit(10);
        emitter2.emit(10);
        assert reciever.retrieveLast() == false;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed() == true;
        assert emitter2.isUnsubscribed() == true;
    }

    @Test
    public void any() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<Boolean> reciever = new EventEmitter();

        Disposable unsubscribe = Events.any(value -> {
            return 20 <= value;
        } , emitter1.observe(), emitter2.observe()).to(reciever);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();
        assert reciever.retrieve() == null;

        emitter1.emit(30);
        emitter2.emit(20);
        assert reciever.retrieveLast() == true;

        emitter1.emit(10);
        assert reciever.retrieveLast() == true;

        emitter2.emit(10);
        assert reciever.retrieveLast() == false;

        emitter2.emit(20);
        assert reciever.retrieveLast() == true;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed() == true;
        assert emitter2.isUnsubscribed() == true;
    }

    @Test
    public void none() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<Boolean> reciever = new EventEmitter();

        Disposable unsubscribe = Events.none(value -> {
            return 20 <= value;
        } , emitter1.observe(), emitter2.observe()).to(reciever);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();
        assert reciever.retrieve() == null;

        emitter1.emit(30);
        emitter2.emit(20);
        assert reciever.retrieveLast() == false;

        emitter1.emit(10);
        assert reciever.retrieveLast() == false;

        emitter2.emit(10);
        assert reciever.retrieveLast() == true;

        emitter2.emit(20);
        assert reciever.retrieveLast() == false;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed() == true;
        assert emitter2.isUnsubscribed() == true;
    }

    @Test
    public void onNext() throws Exception {
        List<Integer> list = new ArrayList<>();
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().on((observer, value) -> {
            list.add(value);
            observer.accept(value);
        }).to(emitter);

        assert emitter.emitAndRetrieve(10) == 10;
        assert emitter.emitAndRetrieve(20) == 20;
        assert list.get(0) == 10;
        assert list.get(1) == 20;
    }

    @Test
    public void never() throws Exception {
        Events.NEVER.to(value -> {
            // none
        });
    }

    @Test
    public void zip() {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<Integer> reciever = new EventEmitter();

        Disposable unsubscribe = Events.zip(emitter1.observe(), emitter2.observe()).map(values -> {
            return values.get(0) + values.get(1);
        }).to(reciever);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();

        emitter1.emit(10);
        emitter1.emit(20);
        emitter1.emit(30);
        assert reciever.retrieve() == null;

        emitter2.emit(100);
        assert reciever.retrieve() == 110;

        emitter2.emit(200);
        assert reciever.retrieve() == 220;

        emitter2.emit(300);
        assert reciever.retrieve() == 330;

        emitter2.emit(400);
        assert reciever.retrieve() == null;

        emitter1.emit(40);
        assert reciever.retrieve() == 440;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed();
        assert emitter2.isUnsubscribed();
    }

    @Test
    public void join() throws Exception {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<List<Integer>> reciever = new EventEmitter();

        Disposable unsubscribe = Events.join(emitter1.observe(), emitter2.observe()).to(reciever);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();
        assert reciever.retrieve() == null;

        emitter1.emit(30);
        assert reciever.retrieve() == null;
        emitter2.emit(20);
        assertList(reciever.retrieve(), 30, 20);
        emitter2.emit(10);
        assertList(reciever.retrieve(), 30, 10);

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed();
        assert emitter2.isUnsubscribed();
    }

    @Test
    public void joinComplete() {
        EventSource<Integer> source1 = new EventSource();
        EventSource<Integer> source2 = new EventSource();
        EventRecorder<List<Integer>> recorder = new EventRecorder();

        // create event stream
        Events<Integer> events1 = createEventsFrom(source1);
        Events<Integer> events2 = createEventsFrom(source2);

        // **declare event handling**
        Disposable unsubscribe = Events.join(events1, events2).to(recorder);

        // verify
        // no source publish
        assert recorder.retrieveValue() == null;

        // one source only publish
        source1.publish(1);
        assert recorder.retrieveValue() == null;

        // all sources publish
        source2.publish(10);
        List<Integer> values = recorder.retrieveValue();
        assert values.size() == 2;
        assert values.get(0) == 1;
        assert values.get(1) == 10;

        // finish one source only, then the next publish will fail because this source is dead
        source1.complete().publish(2);
        assert recorder.retrieveValue() == null;

        // success to publish
        source2.publish(20);
        values = recorder.retrieveValue();
        assert values.size() == 2;
        assert values.get(0) == 1;
        assert values.get(1) == 20;
    }

    @Test
    public void joinCombine() {
        EventEmitter<Integer> emitter1 = new EventEmitter();
        EventEmitter<Integer> emitter2 = new EventEmitter();
        EventEmitter<Integer> reciever = new EventEmitter();

        Disposable unsubscribe = emitter1.observe().join(emitter2.observe(), (v1, v2) -> {
            return v1 + v2;
        }).to(reciever);

        assert emitter1.isSubscribed();
        assert emitter2.isSubscribed();

        emitter1.emit(10);
        assert reciever.retrieve() == null;

        emitter2.emit(20);
        assert reciever.retrieve() == 30;

        unsubscribe.dispose();
        assert emitter1.isUnsubscribed();
        assert emitter2.isUnsubscribed();
    }

    @Test
    public void just() throws Exception {
        EventEmitter<Integer> reciever = new EventEmitter();

        Events.just(1, 2, 3).to(reciever);

        assert reciever.retrieve() == 1;
        assert reciever.retrieve() == 2;
        assert reciever.retrieve() == 3;
    }

    @Test
    public void startWith() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().startWith(0).to(emitter);
        assert emitter.retrieve() == 0;
        assert emitter.emitAndRetrieve(10) == 10;
    }

    @Test
    public void startWithTwice() throws Exception {
        EventEmitter<Integer> emitter = new EventEmitter();
        emitter.observe().startWith(0).startWith(1).to(emitter);
        assert emitter.retrieve() == 1;
        assert emitter.retrieve() == 0;
        assert emitter.emitAndRetrieve(10) == 10;
    }

    /**
     * Create {@link Events} from source.
     * 
     * @param source A source to observer.
     * @return An {@link Events} for the specified source event.
     */
    private static <T> Events<T> createEventsFrom(EventSource<T> source) {
        return new Events<>(observer -> {
            source.list.add(observer);

            return () -> {
                source.list.remove(observer);
            };
        });
    }

    /**
     * @version 2014/08/28 13:34:51
     */
    private static class EventSource<T> {

        /** The complete state. */
        private boolean completed;

        /** The event listeners. */
        private final List<Observer> list = new ArrayList();

        /**
         * Publish event.
         */
        public void publish(T value) {
            if (!completed) {
                for (Observer observer : list) {
                    observer.accept(value);
                }
            }
        }

        /**
         * This source will be disposed.
         */
        public EventSource complete() {
            completed = true;

            for (Observer observer : list) {
                observer.complete();
            }
            return this;
        }
    }

    /**
     * @version 2014/08/28 13:41:16
     */
    private static class EventRecorder<T> implements Observer<T> {

        /** The event holder. */
        private final Deque<T> events = new ArrayDeque();

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(T event) {
            events.add(event);
        }

        /**
         * Retrieve the latest event.
         */
        public T retrieveValue() {
            return events.pollFirst();
        }
    }
}
