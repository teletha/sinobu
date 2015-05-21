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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.Property;

import org.junit.ClassRule;
import org.junit.Test;

import antibug.Chronus;

/**
 * @version 2015/05/19 9:42:39
 */
public class EventsTest {

    @ClassRule
    public static final Chronus chronus = new Chronus(I.class);

    @Test
    public void to() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        disposer.dispose();
        assert facade.countDisposed() == 1;
        assert facade.emitAndRetrieve(30) == null;
    }

    @Test
    public void skip() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skip(1).to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        disposer.dispose();
        assert facade.countDisposed() == 1;
        assert facade.emitAndRetrieve(100) == null;
    }

    @Test
    public void skipWithMultiSources() {
        EventFacade<Integer> source = new EventFacade();
        Events<Integer> skipped = source.observe().skip(1);
        EventRecorder<Integer> recorder1 = record(skipped);
        EventRecorder<Integer> recorder2 = record(skipped);

        source.emit(10);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;

        source.emit(20);
        assert recorder1.retrieveValue() == 20;
        assert recorder2.retrieveValue() == 20;

        source.emit(30);
        assert recorder1.retrieveValue() == 30;
        assert recorder2.retrieveValue() == 30;
    }

    @Test
    public void skipUntil() {
        EventFacade<String> condition = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skipUntil(condition.observe()).to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        condition.emit("start");
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        disposer.dispose();
        assert facade.emitAndRetrieve(100) == null;
        assert facade.countDisposed() == 1;
        assert condition.countDisposed() == 1;
    }

    @Test
    public void skipUntilWithRepeat() throws Exception {
        EventFacade<String> condition = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skipUntil(condition.observe()).take(1).repeat().to(facade);

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

        disposer.dispose();
        assert facade.countDisposed() == 3;
        assert condition.countDisposed() == 3;
        assert facade.emitAndRetrieve(10) == null;
    }

    @Test
    public void skipUntilCondition() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skipUntil(value -> value % 3 == 0).to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        disposer.dispose();
        assert facade.countDisposed() == 1;
        assert facade.emitAndRetrieve(10) == null;
    }

    @Test
    public void skipUntilConditionWithRepeat() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skipUntil(value -> value % 3 == 0).take(2).repeat().to(facade);

        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == 60;
        assert facade.emitAndRetrieve(70) == 70;
        assert facade.emitAndRetrieve(80) == null;

        disposer.dispose();
        assert facade.countDisposed() == 3;
        assert facade.emitAndRetrieve(30) == null;
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
    public void buffer() {
        EventFacade<Integer> publisher = new EventFacade();
        EventFacade<List<Integer>> subscriber = new EventFacade();
        Disposable disposer = publisher.observe().buffer(2).to(subscriber);

        publisher.emit(10);
        assert subscriber.retrieveNull();
        publisher.emit(20);
        assert subscriber.retrieve(10, 20);

        publisher.emit(30);
        assert subscriber.retrieveNull();
        publisher.emit(40);
        assert subscriber.retrieve(30, 40);

        disposer.dispose();
        assert publisher.countDisposed() == 1;
        publisher.emit(50, 60);
        assert subscriber.retrieveNull();
    }

    @Test
    public void bufferRepeat() throws Exception {
        EventFacade<List<Integer>> reciever = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().buffer(2).skip(1).take(1).repeat().to(reciever);

        facade.emit(10);
        facade.emit(20);
        assert reciever.retrieve() == null;
        facade.emit(30);
        facade.emit(40);
        assertList(reciever.retrieve(), 30, 40);

        facade.emit(50);
        facade.emit(60);
        assert reciever.retrieve() == null;
        facade.emit(70);
        facade.emit(80);
        assertList(reciever.retrieve(), 70, 80);

        disposer.dispose();
        assert facade.isUnsubscribed();

        facade.emit(90);
        facade.emit(100);
        facade.emit(110);
        facade.emit(120);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferInterval1() throws Exception {
        EventFacade<List<Integer>> reciever = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().buffer(2, 1).to(reciever);

        facade.emit(10);
        assert reciever.retrieve() == null;
        facade.emit(20);
        assertList(reciever.retrieve(), 10, 20);

        facade.emit(30);
        assertList(reciever.retrieve(), 20, 30);
        facade.emit(40);
        assertList(reciever.retrieve(), 30, 40);

        disposer.dispose();
        assert facade.isUnsubscribed();

        facade.emit(50);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferInterval2() throws Exception {
        EventFacade<List<Integer>> reciever = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().buffer(2, 3).to(reciever);

        facade.emit(10);
        assert reciever.retrieve() == null;
        facade.emit(20);
        assert reciever.retrieve() == null;
        facade.emit(30);
        assertList(reciever.retrieve(), 20, 30);
        facade.emit(40);
        assert reciever.retrieve() == null;
        facade.emit(50);
        assert reciever.retrieve() == null;
        facade.emit(60);
        assertList(reciever.retrieve(), 50, 60);

        disposer.dispose();
        assert facade.isUnsubscribed();

        facade.emit(70);
        assert reciever.retrieve() == null;
    }

    @Test
    public void bufferTime() throws Exception {
        EventFacade<List<Integer>> reciever = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().buffer(30, MILLISECONDS).to(reciever);

        facade.emit(10);
        facade.emit(20);
        // chronus.freeze(30);
        Thread.sleep(300);
        facade.emit(30);
        facade.emit(40);
        facade.emit(50);
        // chronus.freeze(30);
        Thread.sleep(300);
        facade.emit(60);

        assertList(reciever.retrieve(), 10, 20);
        assertList(reciever.retrieve(), 30, 40, 50);
    }

    @Test
    public void as() throws Exception {
        EventFacade<Integer> reciever = new EventFacade();
        EventFacade<Number> facade = new EventFacade();
        facade.observe().as(Integer.class).to(reciever);

        facade.emit(10);
        assert reciever.retrieve() == 10;

        facade.emit(2.1F);
        assert reciever.retrieve() == null;
        facade.emit(-1.1D);
        assert reciever.retrieve() == null;
        facade.emit(20L);
        assert reciever.retrieve() == null;
    }

    @Test
    public void diff() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().diff().to(facade);

        assert facade.isSubscribed();
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        disposer.dispose();
        assert facade.isUnsubscribed();
        assert facade.emitAndRetrieve(10) == null;
    }

    @Test
    public void flatMap() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().flatMap(value -> facade.stream(value, value + 1)).to(facade);

        facade.emit(10);
        assert facade.retrieve() == 10;
        assert facade.retrieve() == 11;

        facade.emit(20);
        assert facade.retrieve() == 20;
        assert facade.retrieve() == 21;

        assert facade.countDisposed() == 0;
        disposer.dispose();
        facade.emit(30);
        assert facade.retrieve() == null;
        assert facade.countDisposed() == 3;
    }

    @Test
    public void map() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().map((Function<Integer, Integer>) value -> {
            return value * 2;
        }).to(facade);

        assert facade.emitAndRetrieve(10) == 20;
        assert facade.emitAndRetrieve(20) == 40;

        disposer.dispose();
        assert facade.emitAndRetrieve(30) == null;
    }

    @Test
    public void mapConstant() {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().map(100).to(facade);
        assert facade.emitAndRetrieve(10) == 100;
        assert facade.emitAndRetrieve(20) == 100;
        assert facade.emitAndRetrieve(30) == 100;
    }

    @Test
    public void mapWithPreviousValue() {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().map(1, (prev, now) -> prev + now).to(facade);
        assert facade.emitAndRetrieve(1) == 2;
        assert facade.emitAndRetrieve(2) == 3;
        assert facade.emitAndRetrieve(3) == 5;
    }

    @Test
    public void take() {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().take(1).to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
    }

    @Test
    public void takeWithMultiSources() throws Exception {
        EventFacade<Integer> source = new EventFacade();
        Events<Integer> taked = source.observe().take(1);
        EventRecorder<Integer> recorder1 = record(taked);
        EventRecorder<Integer> recorder2 = record(taked);

        assert recorder1.isCompleted == false;
        assert recorder1.isCompleted == false;

        // emit and validate
        source.emit(10);
        assert recorder1.retrieveValue() == 10;
        assert recorder2.retrieveValue() == 10;
        assert recorder1.isCompleted;
        assert recorder2.isCompleted;

        // emit and validate
        source.emit(20);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;
        assert recorder1.isCompleted;
        assert recorder2.isCompleted;

        // dispose
    }

    @Test
    public void takeUntil() throws Exception {
        EventFacade<String> condition = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().takeUntil(condition.observe()).to(facade);

        assert condition.isSubscribed();
        assert facade.isSubscribed();
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        condition.emit("start");
        assert condition.isUnsubscribed() == true;
        assert facade.isUnsubscribed() == true;
        assert facade.emitAndRetrieve(10) == null;
    }

    @Test
    public void takeUntilCondition() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().takeUntil(v -> {
            return v % 3 == 0;
        }).to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        assert facade.isUnsubscribed();
        assert facade.emitAndRetrieve(40) == null;
    }

    @Test
    public void takeUntilConditionRepeat() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skip(1).takeUntil(v -> {
            return v % 3 == 0;
        }).repeat().to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        assert facade.emitAndRetrieve(40) == null;
        assert facade.emitAndRetrieve(60) == 60;
        assert facade.emitAndRetrieve(70) == null;
        assert facade.emitAndRetrieve(80) == 80;
        assert facade.emitAndRetrieve(100) == 100;

        disposer.dispose();
        assert facade.isUnsubscribed();
        assert facade.emitAndRetrieve(110) == null;
    }

    @Test
    public void skipAndTake() {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().skip(2).take(2).to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == null;
    }

    @Test
    public void skipAndTakeWithMultiSources() {
        EventFacade<Integer> source = new EventFacade();
        Events<Integer> events = source.observe().skip(1).take(1);
        EventRecorder<Integer> recorder1 = record(events);
        EventRecorder<Integer> recorder2 = record(events);

        source.emit(10);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;

        source.emit(20);
        assert recorder1.retrieveValue() == 20;
        assert recorder2.retrieveValue() == 20;

        source.emit(30);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;
    }

    @Test
    public void repeat() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skip(1).take(1).repeat().to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == 60;

        disposer.dispose();
        assert facade.countDisposed() == 4;
        assert facade.emitAndRetrieve(100) == null;
        assert facade.emitAndRetrieve(200) == null;
    }

    @Test
    public void repeatWithMultiSources() {
        EventFacade<Integer> facade = new EventFacade();
        Events<Integer> repeated = facade.observe().skip(1).take(1).repeat();
        EventRecorder<Integer> recorder1 = record(repeated);
        EventRecorder<Integer> recorder2 = record(repeated);

        facade.emit(10);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;

        facade.emit(20);
        assert recorder1.retrieveValue() == 20;
        assert recorder2.retrieveValue() == 20;

        facade.emit(30);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;

        facade.emit(40);
        assert recorder1.retrieveValue() == 40;
        assert recorder2.retrieveValue() == 40;
    }

    @Test
    public void repeatFinitely() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().skip(1).take(1).repeat(2).to(facade);

        assert facade.countDisposed() == 0;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.countDisposed() == 1;
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;

        assert facade.countDisposed() == 2;
        assert facade.emitAndRetrieve(100) == null;
        assert facade.emitAndRetrieve(200) == null;
    }

    @Test
    public void repeatFinitelyWithMultiSource() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        Events<Integer> repeated = facade.observe().skip(1).take(1).repeat();
        EventRecorder<Integer> recorder1 = record(repeated);
        EventRecorder<Integer> recorder2 = record(repeated);

        facade.emit(10);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;

        facade.emit(20);
        assert recorder1.retrieveValue() == 20;
        assert recorder2.retrieveValue() == 20;

        facade.emit(30);
        assert recorder1.retrieveValue() == null;
        assert recorder2.retrieveValue() == null;

        facade.emit(40);
        assert recorder1.retrieveValue() == 40;
        assert recorder2.retrieveValue() == 40;
    }

    @Test
    public void repeatThen() throws Exception {
        EventFacade<Integer> sub = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().skip(1).take(2).repeat().merge(sub.observe()).to(facade);

        assert facade.isSubscribed();
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.isSubscribed();
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == 50;
        assert facade.isSubscribed();

        // from sub
        assert sub.isSubscribed();
        sub.emit(100);
        assert facade.retrieve() == 100;
        sub.emit(200);
        assert facade.retrieve() == 200;

        disposer.dispose();
        assert facade.isUnsubscribed() == true;
        assert facade.emitAndRetrieve(50) == null;

        // from sub
        sub.emit(300);
        assert facade.retrieve() == null;
        assert sub.isUnsubscribed() == true;
    }

    @Test
    public void filter() {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().filter(value -> value % 2 == 0).to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(15) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(25) == null;

        disposer.dispose();
        assert facade.countDisposed() == 1;
    }

    @Test
    public void filterByEvent() {
        EventFacade<Boolean> condition = new EventFacade();
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().filter(condition.observe()).to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        condition.emit(true);
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        condition.emit(false);
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        disposer.dispose();
        assert facade.countDisposed() == 1;
        assert condition.countDisposed() == 1;
    }

    @Test
    public void throttle() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().throttle(20, MILLISECONDS).to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(10) == null;

        Thread.sleep(20);
        assert facade.emitAndRetrieve(10) == 10;
    }

    @Test
    public void debounce() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().debounce(10, MILLISECONDS).to(facade);

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        chronus.await();
        assert facade.retrieve() == 30;
    }

    @Test
    public void debounceRepeat() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().debounce(10, MILLISECONDS).skip(1).take(1).repeat().to(facade);

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
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().delay(10, MILLISECONDS).to(facade);

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
    public void distinct() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().distinct().to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
    }

    @Test
    public void distinctRepeat() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        Disposable disposer = facade.observe().distinct().take(2).repeat().to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        disposer.dispose();
        assert facade.isUnsubscribed() == true;
        assert facade.emitAndRetrieve(10) == null;
    }

    @Test
    public void merge() throws Exception {
        EventFacade<Integer> facade1 = new EventFacade();
        EventFacade<Integer> facade2 = new EventFacade();
        Disposable disposer = facade1.observe().merge(facade2.observe()).to(facade1);

        assert facade1.isSubscribed();
        assert facade2.isSubscribed();
        assert facade1.emitAndRetrieve(10) == 10;
        assert facade1.emitAndRetrieve(20) == 20;

        facade2.emit(100);
        facade2.emit(200);
        assert facade1.retrieve() == 100;
        assert facade1.retrieve() == 200;

        disposer.dispose();
        assert facade1.isUnsubscribed() == true;
        assert facade2.isUnsubscribed() == true;
    }

    @Test
    public void mergeIterable() throws Exception {
        EventFacade<Integer> facade1 = new EventFacade();
        EventFacade<Integer> facade2 = new EventFacade();
        EventFacade<Integer> facade3 = new EventFacade();
        EventFacade<Integer> facade4 = new EventFacade();

        List<Events<Integer>> list = new ArrayList();
        list.add(facade2.observe());
        list.add(facade3.observe());
        list.add(facade4.observe());

        Disposable disposer = facade1.observe().merge(list).to(facade1);

        assert facade1.isSubscribed();
        assert facade2.isSubscribed();
        assert facade3.isSubscribed();
        assert facade4.isSubscribed();
        assert facade1.emitAndRetrieve(10) == 10;

        facade2.emit(100);
        facade3.emit(200);
        facade4.emit(300);
        assert facade1.retrieve() == 100;
        assert facade1.retrieve() == 200;
        assert facade1.retrieve() == 300;

        disposer.dispose();
        assert facade1.isUnsubscribed() == true;
        assert facade2.isUnsubscribed() == true;
        assert facade3.isUnsubscribed() == true;
        assert facade4.isUnsubscribed() == true;
    }

    @Test
    public void mergeNull() throws Exception {
        EventFacade<Integer> facade1 = new EventFacade();
        Disposable disposer = facade1.observe().merge((Events) null).to(facade1);

        assert facade1.isSubscribed();
        assert facade1.emitAndRetrieve(10) == 10;

        disposer.dispose();
        assert facade1.isUnsubscribed() == true;
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
        EventFacade<Integer> facade1 = new EventFacade();
        EventFacade<Integer> facade2 = new EventFacade();
        EventFacade<Boolean> reciever = new EventFacade();

        Disposable disposer = Events.all(value -> {
            return 20 <= value;
        } , facade1.observe(), facade2.observe()).to(reciever);

        assert facade1.isSubscribed();
        assert facade2.isSubscribed();
        assert reciever.retrieve() == null;

        facade1.emit(30);
        facade2.emit(20);
        assert reciever.retrieveLast() == true;

        facade1.emit(10);
        assert reciever.retrieveLast() == false;

        facade1.emit(20);
        assert reciever.retrieveLast() == true;

        facade2.emit(10);
        assert reciever.retrieveLast() == false;

        facade2.emit(40);
        assert reciever.retrieveLast() == true;

        facade1.emit(10);
        facade2.emit(10);
        assert reciever.retrieveLast() == false;

        disposer.dispose();
        assert facade1.isUnsubscribed() == true;
        assert facade2.isUnsubscribed() == true;
    }

    @Test
    public void any() throws Exception {
        EventFacade<Integer> facade1 = new EventFacade();
        EventFacade<Integer> facade2 = new EventFacade();
        EventFacade<Boolean> reciever = new EventFacade();

        Disposable disposer = Events.any(value -> {
            return 20 <= value;
        } , facade1.observe(), facade2.observe()).to(reciever);

        assert facade1.isSubscribed();
        assert facade2.isSubscribed();
        assert reciever.retrieve() == null;

        facade1.emit(30);
        facade2.emit(20);
        assert reciever.retrieveLast() == true;

        facade1.emit(10);
        assert reciever.retrieveLast() == true;

        facade2.emit(10);
        assert reciever.retrieveLast() == false;

        facade2.emit(20);
        assert reciever.retrieveLast() == true;

        disposer.dispose();
        assert facade1.isUnsubscribed() == true;
        assert facade2.isUnsubscribed() == true;
    }

    @Test
    public void none() throws Exception {
        EventFacade<Integer> facade1 = new EventFacade();
        EventFacade<Integer> facade2 = new EventFacade();
        EventFacade<Boolean> reciever = new EventFacade();

        Disposable disposer = Events.none(value -> {
            return 20 <= value;
        } , facade1.observe(), facade2.observe()).to(reciever);

        assert facade1.isSubscribed();
        assert facade2.isSubscribed();
        assert reciever.retrieve() == null;

        facade1.emit(30);
        facade2.emit(20);
        assert reciever.retrieveLast() == false;

        facade1.emit(10);
        assert reciever.retrieveLast() == false;

        facade2.emit(10);
        assert reciever.retrieveLast() == true;

        facade2.emit(20);
        assert reciever.retrieveLast() == false;

        disposer.dispose();
        assert facade1.isUnsubscribed() == true;
        assert facade2.isUnsubscribed() == true;
    }

    @Test
    public void onNext() throws Exception {
        List<Integer> list = new ArrayList<>();
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().on((observer, value) -> {
            list.add(value);
            observer.accept(value);
        }).to(facade);

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert list.get(0) == 10;
        assert list.get(1) == 20;
    }

    @Test
    public void never() throws Exception {
        Events.NEVER.to(value -> {
            // none
        });
    }

    // @Test
    // public void zip() {
    // EventFacade<Integer> facade1 = new EventFacade();
    // EventFacade<Integer> facade2 = new EventFacade();
    // EventFacade<Integer> reciever = new EventFacade();
    //
    // Disposable disposer = Events.zip(facade1.observe(), facade2.observe()).map(values -> {
    // return values.get(0) + values.get(1);
    // }).to(reciever);
    //
    // assert facade1.isSubscribed();
    // assert facade2.isSubscribed();
    //
    // facade1.emit(10);
    // facade1.emit(20);
    // facade1.emit(30);
    // assert reciever.retrieve() == null;
    //
    // facade2.emit(100);
    // assert reciever.retrieve() == 110;
    //
    // facade2.emit(200);
    // assert reciever.retrieve() == 220;
    //
    // facade2.emit(300);
    // assert reciever.retrieve() == 330;
    //
    // facade2.emit(400);
    // assert reciever.retrieve() == null;
    //
    // facade1.emit(40);
    // assert reciever.retrieve() == 440;
    //
    // disposer.dispose();
    // assert facade1.isUnsubscribed();
    // assert facade2.isUnsubscribed();
    // }

    // @Test
    // public void join() throws Exception {
    // EventFacade<Integer> facade1 = new EventFacade();
    // EventFacade<Integer> facade2 = new EventFacade();
    // EventFacade<List<Integer>> reciever = new EventFacade();
    //
    // Disposable disposer = Events.join(facade1.observe(), facade2.observe()).to(reciever);
    //
    // assert facade1.isSubscribed();
    // assert facade2.isSubscribed();
    // assert reciever.retrieve() == null;
    //
    // facade1.emit(30);
    // assert reciever.retrieve() == null;
    // facade2.emit(20);
    // assertList(reciever.retrieve(), 30, 20);
    // facade2.emit(10);
    // assertList(reciever.retrieve(), 30, 10);
    //
    // disposer.dispose();
    // assert facade1.isUnsubscribed();
    // assert facade2.isUnsubscribed();
    // }
    //
    // @Test
    // public void joinComplete() {
    // EventSource<Integer> source1 = new EventSource();
    // EventSource<Integer> source2 = new EventSource();
    // EventRecorder<List<Integer>> recorder = new EventRecorder();
    //
    // // create event stream
    // Events<Integer> events1 = createEventsFrom(source1);
    // Events<Integer> events2 = createEventsFrom(source2);
    //
    // // **declare event handling**
    // Disposable disposer = Events.join(events1, events2).to(recorder);
    //
    // // verify
    // // no source publish
    // assert recorder.retrieveValue() == null;
    //
    // // one source only publish
    // source1.publish(1);
    // assert recorder.retrieveValue() == null;
    //
    // // all sources publish
    // source2.publish(10);
    // List<Integer> values = recorder.retrieveValue();
    // assert values.size() == 2;
    // assert values.get(0) == 1;
    // assert values.get(1) == 10;
    //
    // // finish one source only, then the next publish will fail because this source is dead
    // source1.complete().publish(2);
    // assert recorder.retrieveValue() == null;
    //
    // // success to publish
    // source2.publish(20);
    // values = recorder.retrieveValue();
    // assert values.size() == 2;
    // assert values.get(0) == 1;
    // assert values.get(1) == 20;
    // }

    @Test
    public void joinCombine() {
        EventFacade<Integer> facade1 = new EventFacade();
        EventFacade<Integer> facade2 = new EventFacade();
        EventFacade<Integer> reciever = new EventFacade();

        Disposable disposer = facade1.observe().join(facade2.observe(), (v1, v2) -> {
            return v1 + v2;
        }).to(reciever);

        assert facade1.isSubscribed();
        assert facade2.isSubscribed();

        facade1.emit(10);
        assert reciever.retrieve() == null;

        facade2.emit(20);
        assert reciever.retrieve() == 30;

        disposer.dispose();
        assert facade1.isUnsubscribed();
        assert facade2.isUnsubscribed();
    }

    @Test
    public void just() throws Exception {
        EventFacade<Integer> reciever = new EventFacade();

        Events.just(1, 2, 3).to(reciever);

        assert reciever.retrieve() == 1;
        assert reciever.retrieve() == 2;
        assert reciever.retrieve() == 3;
    }

    @Test
    public void startWith() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().startWith(0).to(facade);
        assert facade.retrieve() == 0;
        assert facade.emitAndRetrieve(10) == 10;
    }

    @Test
    public void startWithTwice() throws Exception {
        EventFacade<Integer> facade = new EventFacade();
        facade.observe().startWith(0).startWith(1).to(facade);
        assert facade.retrieve() == 1;
        assert facade.retrieve() == 0;
        assert facade.emitAndRetrieve(10) == 10;
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
     * <p>
     * Helper method to create the listener of the specified {@link Events}.
     * </p>
     * 
     * @param stream
     * @return
     */
    private static <T> EventRecorder<T> record(Events<T> stream) {
        EventRecorder<T> recorder = new EventRecorder();
        recorder.disposer = stream.to(recorder);

        return recorder;
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

        /** The disposer. */
        private Disposable disposer;

        /** The event holder. */
        private final List<T> events = new ArrayList();

        /** The subscription flag. */
        private boolean isCompleted;

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(T event) {
            events.add(event);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            isCompleted = true;
        }

        /**
         * Retrieve the latest event.
         */
        public T retrieveValue() {
            return events.isEmpty() ? null : events.remove(0);
        }
    }
}
