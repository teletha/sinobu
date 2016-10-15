/*
 * Copyright (C) 2016 Nameless Production Committee
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;

import org.junit.ClassRule;
import org.junit.Test;

import antibug.Chronus;

/**
 * @version 2015/10/18 16:43:51
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
    public void toAlternate() {
        EventFacade<Integer, Integer> facade = new EventFacade<>();
        SetProperty<Integer> set = facade.observe().toAlternate();

        facade.emit(10);
        assert set.size() == 1;

        facade.emit(20);
        assert set.size() == 2;

        // duplicate
        facade.emit(10);
        assert set.size() == 1;

        facade.emit(20);
        assert set.size() == 0;

        // again
        facade.emit(10);
        assert set.size() == 1;

        facade.emit(20);
        assert set.size() == 2;
    }

    @Test
    public void toBinary() {
        EventFacade<Integer, Integer> facade = new EventFacade<>();
        BooleanProperty binary = facade.observe().toBinary();

        facade.emit(10);
        assert binary.get() == true;

        facade.emit(20);
        assert binary.get() == false;

        facade.emit(30);
        assert binary.get() == true;

        facade.emit(10);
        assert binary.get() == false;
    }

    @Test
    public void toList() {
        EventFacade<Integer, Integer> facade = new EventFacade<>();
        ListProperty<Integer> list = facade.observe().toList();

        facade.emit(10);
        assert list.size() == 1;
        assert list.get(0) == 10;

        facade.emit(20);
        assert list.size() == 2;
        assert list.get(1) == 20;

        facade.emit(30);
        assert list.size() == 3;
        assert list.get(2) == 30;

        // duplicate
        facade.emit(10);
        assert list.size() == 4;
        assert list.get(3) == 10;
    }

    @Test
    public void toMultiList() {
        EventFacade<Integer, Integer> facade = new EventFacade<>();
        Events<Integer> events = facade.observe();
        ListProperty<Integer> list1 = events.toList();
        ListProperty<Integer> list2 = events.toList();

        facade.emit(10);
        assert list1.size() == 1;
        assert list1.get(0) == 10;
        assert list2.size() == 1;
        assert list2.get(0) == 10;

        facade.emit(20);
        assert list1.size() == 2;
        assert list1.get(1) == 20;
        assert list2.size() == 2;
        assert list2.get(1) == 20;
    }

    @Test
    public void toMap() {
        EventFacade<Integer, Integer> facade = new EventFacade<>();
        MapProperty<String, Integer> map = facade.observe().toMap(v -> String.valueOf(v));

        facade.emit(10);
        assert map.size() == 1;
        assert map.get("10") == 10;

        facade.emit(20);
        assert map.size() == 2;
        assert map.get("20") == 20;

        // duplicate
        facade.emit(10);
        assert map.size() == 2;
        assert map.get("10") == 10;
    }

    @Test
    public void toSet() {
        EventFacade<Integer, Integer> facade = new EventFacade<>();
        SetProperty<Integer> set = facade.observe().toSet();

        facade.emit(10);
        assert set.size() == 1;

        facade.emit(20);
        assert set.size() == 2;

        facade.emit(30);
        assert set.size() == 3;

        // duplicate
        facade.emit(10);
        assert set.size() == 3;
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
    public void bufferTime() {
        EventFacade<Integer, List<Integer>> facade = new EventFacade<>(events -> events.buffer(30, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        chronus.freeze(100);
        assert facade.retrieveAsList(10, 20);
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == null;
        assert facade.emitAndRetrieve(50) == null;
        chronus.freeze(100);
        assert facade.retrieveAsList(30, 40, 50);
    }

    @Test
    public void combine() {
        EventFacade<Integer, Integer> sub = new EventFacade<>();
        EventFacade<Integer, Integer> facade = new EventFacade<Integer, Integer>(
                events -> events.combine(sub.observe(), (base, other) -> base + other));

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
    public void combineBinary() throws Exception {
        EventFacade<Integer, Integer> other = new EventFacade<>();
        EventFacade<String, Ⅱ<String, Integer>> main = new EventFacade<>(events -> events.combine(other.observe()));

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
        EventFacade<Integer, Integer> other = new EventFacade<>();
        EventFacade<Double, Double> another = new EventFacade<>();
        EventFacade<String, Ⅲ<String, Integer, Double>> main = new EventFacade<>(
                events -> events.combine(other.observe(), another.observe()));

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
        EventFacade<Integer, Integer> sub = new EventFacade<>();
        EventFacade<Integer, Integer> facade = new EventFacade<Integer, Integer>(
                events -> events.combineLatest(sub.observe(), (base, other) -> base + other));

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
    public void combineLatestBinary() throws Exception {
        EventFacade<Integer, Integer> other = new EventFacade<>();
        EventFacade<String, Ⅱ<String, Integer>> main = new EventFacade<>(events -> events.combineLatest(other.observe()));

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
        EventFacade<Integer, Integer> other = new EventFacade<>();
        EventFacade<Double, Double> another = new EventFacade<>();
        EventFacade<String, Ⅲ<String, Integer, Double>> main = new EventFacade<>(
                events -> events.combineLatest(other.observe(), another.observe()));

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
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.debounce(10, MILLISECONDS).skip(1).take(1).repeat());

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
    public void flatMap() {
        EventFacade<String, String> emitA = new EventFacade();
        EventFacade<String, String> emitB = new EventFacade();
        EventFacade<Integer, String> facade = new EventFacade<>(events -> events.flatMap(x -> x == 1 ? emitA.observe() : emitB.observe()));

        facade.emit(1); // connect to emitA
        assert facade.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("1A");
        assert facade.retrieve() == "1A";
        emitB.emit("1B"); // emitB has no relation yet
        assert facade.retrieve() == null;

        facade.emit(2); // connect to emitB
        assert facade.retrieve() == null; // emitB doesn't emit value yet
        emitB.emit("2B");
        assert facade.retrieve() == "2B";
        emitA.emit("2A");
        assert facade.retrieve() == "2A";

        // test disposing
        facade.dispose();
        emitA.emit("Disposed");
        assert facade.retrieve() == null;
        emitB.emit("Disposed");
        assert facade.retrieve() == null;
    }

    @Test
    public void flatMapLatest() {
        EventFacade<String, String> emitA = new EventFacade();
        EventFacade<String, String> emitB = new EventFacade();
        EventFacade<Integer, String> facade = new EventFacade<>(
                events -> events.flatMapLatest(x -> x == 1 ? emitA.observe() : emitB.observe()));

        facade.emit(1); // connect to emitA
        assert facade.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("1A");
        assert facade.retrieve() == "1A";
        emitB.emit("1B"); // emitB has no relation yet
        assert facade.retrieve() == null;

        facade.emit(2); // connect to emitB and disconnect from emitA
        assert facade.retrieve() == null; // emitB doesn't emit value yet
        emitB.emit("2B");
        assert facade.retrieve() == "2B";
        emitA.emit("2A");
        assert facade.retrieve() == null;

        facade.emit(1); // reconnect to emitA and disconnect from emitB
        assert facade.retrieve() == null; // emitA doesn't emit value yet
        emitA.emit("3A");
        assert facade.retrieve() == "3A";
        emitB.emit("3B");
        assert facade.retrieve() == null;

        // test disposing
        facade.dispose();
        emitA.emit("Disposed");
        assert facade.retrieve() == null;
    }

    @Test
    public void map() throws Exception {
        EventFacade<Integer, Integer> facade = new EventFacade<Integer, Integer>(events -> events.map(value -> value * 2));

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
    public void repeatTakeUntil() {
        EventFacade<String, String> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1).take(1).repeat().takeUntil(condition.observe()));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;

        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;

        condition.emit("END");
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == null;

        assert facade.isCompleted();
        assert condition.isCompleted();
    }

    @Test
    public void repeatThen() {
        EventFacade<Integer, Integer> sub = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1).take(2).repeat().merge(sub.observe()));

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
    public void sampleBySamplerEvents() {
        EventFacade<String, String> sampler = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.sample(sampler.observe()));
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        sampler.emit("NOW");
        assert facade.retrieve() == 30;

        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(10) == null;

        sampler.emit("NOW");
        assert facade.retrieve() == 10;
        assert facade.dispose();
        assert sampler.isCompleted();
    }

    @Test
    public void scan() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.scan(10, (accumulated, value) -> accumulated + value));

        assert facade.emitAndRetrieve(1) == 11; // 10 + 1
        assert facade.emitAndRetrieve(2) == 13; // 11 + 2
        assert facade.emitAndRetrieve(3) == 16; // 13 + 3
        assert facade.dispose();
    }

    @Test
    public void sideEffect() {
        Set<Integer> list = new HashSet();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.sideEffect(list::add));

        assert facade.emitAndRetrieve(10) == 10;
        assert list.contains(10);
        assert facade.emitAndRetrieve(20) == 20;
        assert list.contains(20);
    }

    @Test
    public void skipByCondition() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(value -> value % 3 == 0));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == 50;
        assert facade.emitAndRetrieve(60) == null;
        assert facade.dispose();
    }

    @Test
    public void skipByConditionWithPreviousValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(e -> e.skip(0, (prev, value) -> value - prev > 5));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(11) == 11;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(22) == 22;
        assert facade.dispose();
    }

    @Test
    public void skipByConditionEvent() {
        EventFacade<Boolean, Boolean> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipWhile(condition.observe()));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;

        condition.emit(true);
        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;

        condition.emit(false);
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
        assert condition.isCompleted();
    }

    @Test
    public void skipByCount() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.dispose();
    }

    @Test
    public void skipByTime() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(10, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        chronus.freeze(100);
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
    }

    @Test
    public void skipByTimeWaitingAtFirst() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(10, MILLISECONDS));

        chronus.freeze(100);
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
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
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(condition.observe()).take(1).repeat());

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
    public void skipUntilValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(30));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.dispose();
    }

    @Test
    public void skipUntilNullValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil((Integer) null));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;
        assert facade.emitAndRetrieve(null) == null;
        assert facade.emitAndRetrieve(40) == 40;
    }

    @Test
    public void skipUntilValueWithRepeat() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(30).take(2).repeat());

        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == 40;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.disposeWithCountAlreadyDisposed(2);
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
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skipUntil(value -> value % 3 == 0).take(2).repeat());

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
    public void takeByCondition() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.take(value -> value % 3 == 0));

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == null;
        assert facade.emitAndRetrieve(50) == null;
        assert facade.emitAndRetrieve(60) == 60;
        assert facade.dispose();
    }

    @Test
    public void takeByConditionWithPreviousValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(e -> e.take(0, (prev, value) -> value - prev > 5));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(11) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(22) == null;
        assert facade.dispose();
    }

    @Test
    public void takeByConditionEvent() {
        EventFacade<Boolean, Boolean> condition = new EventFacade();
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.takeWhile(condition.observe()));

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
    public void takeByCount() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.take(1));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.isCompleted();
    }

    @Test
    public void takeByTime() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.take(30, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        chronus.freeze(30);
        assert facade.emitAndRetrieve(10) == null;
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
    public void takeUntilValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.takeUntil(30));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(40) == null;
        assert facade.isCompleted();
    }

    @Test
    public void takeUntilNullValue() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.takeUntil((Integer) null));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(null) == null;
        assert facade.emitAndRetrieve(30) == null;
        assert facade.isCompleted();
    }

    @Test
    public void takeUntilValueRepeat() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1).takeUntil(30).repeat());

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        assert facade.emitAndRetrieve(10) == null;
        assert facade.emitAndRetrieve(20) == 20;
        assert facade.emitAndRetrieve(30) == 30;

        assert facade.disposeWithCountAlreadyDisposed(2);
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
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.skip(1).takeUntil(value -> value % 3 == 0).repeat());

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
    public void toggle() {
        EventFacade<String, Boolean> facade = new EventFacade<>(events -> events.toggle());
        assert facade.emitAndRetrieve("1") == true;
        assert facade.emitAndRetrieve("2") == false;
        assert facade.emitAndRetrieve("3") == true;
        assert facade.emitAndRetrieve("4") == false;
    }

    @Test
    public void toggleWithInitialValue() {
        EventFacade<String, Boolean> facade = new EventFacade<>(events -> events.toggle(false));
        assert facade.emitAndRetrieve("1") == false;
        assert facade.emitAndRetrieve("2") == true;
        assert facade.emitAndRetrieve("3") == false;
        assert facade.emitAndRetrieve("4") == true;
    }

    @Test
    public void toggleWithValues() {
        EventFacade<Integer, String> facade = new EventFacade<>(events -> events.toggle("one", "other"));
        assert facade.emitAndRetrieve(1).equals("one");
        assert facade.emitAndRetrieve(2).equals("other");
        assert facade.emitAndRetrieve(3).equals("one");
        assert facade.emitAndRetrieve(4).equals("other");
    }

    @Test
    public void toggleWithValuesMore() {
        EventFacade<Integer, String> facade = new EventFacade<>(events -> events.toggle("one", "two", "three"));
        assert facade.emitAndRetrieve(1).equals("one");
        assert facade.emitAndRetrieve(2).equals("two");
        assert facade.emitAndRetrieve(3).equals("three");
        assert facade.emitAndRetrieve(4).equals("one");
        assert facade.emitAndRetrieve(5).equals("two");
        assert facade.emitAndRetrieve(6).equals("three");
    }

    @Test
    public void toggleWithNull() {
        EventFacade<Integer, String> facade = new EventFacade<>(events -> events.toggle("one", null));
        assert facade.emitAndRetrieve(1).equals("one");
        assert facade.emitAndRetrieve(2) == null;
        assert facade.emitAndRetrieve(3).equals("one");
        assert facade.emitAndRetrieve(4) == null;
    }

    @Test
    public void throttle() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> events.throttle(20, MILLISECONDS));

        assert facade.emitAndRetrieve(10) == 10;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(30) == null;

        chronus.freeze(20);

        assert facade.emitAndRetrieve(30) == 30;
        assert facade.emitAndRetrieve(20) == null;
        assert facade.emitAndRetrieve(10) == null;
    }

    @Test
    public void all() {
        EventFacade<Boolean, Boolean> facade1 = new EventFacade();
        EventFacade<Boolean, Boolean> facade2 = new EventFacade();
        EventFacade<Boolean, Boolean> base = new EventFacade(events -> Events.all(facade1.observe(), facade2.observe()));
        assert base.retrieve() == null;

        facade1.emit(true);
        facade2.emit(true);
        assert base.retrieve() == true;

        facade1.emit(false);
        assert base.retrieve() == false;

        facade2.emit(false);
        assert base.retrieve() == false;

        assert base.dispose();
        assert facade1.isCompleted();
        assert facade2.isCompleted();
    }

    @Test
    public void allNoArg() {
        EventFacade<Boolean, Boolean> base = new EventFacade(events -> Events.all());
        assert base.retrieve() == null;
        assert base.dispose();
    }

    @Test
    public void any() {
        EventFacade<Boolean, Boolean> facade1 = new EventFacade();
        EventFacade<Boolean, Boolean> facade2 = new EventFacade();
        EventFacade<Boolean, Boolean> base = new EventFacade(events -> Events.any(facade1.observe(), facade2.observe()));
        assert base.retrieve() == null;

        facade1.emit(true);
        facade2.emit(true);
        assert base.retrieve() == true;

        facade1.emit(false);
        assert base.retrieve() == true;

        facade2.emit(false);
        assert base.retrieve() == false;

        assert base.dispose();
        assert facade1.isCompleted();
        assert facade2.isCompleted();
    }

    @Test
    public void anyNoArg() {
        EventFacade<Boolean, Boolean> base = new EventFacade(events -> Events.any());
        assert base.retrieve() == null;
        assert base.dispose();
    }

    @Test
    public void none() {
        EventFacade<Boolean, Boolean> facade1 = new EventFacade();
        EventFacade<Boolean, Boolean> facade2 = new EventFacade();
        EventFacade<Boolean, Boolean> base = new EventFacade(events -> Events.none(facade1.observe(), facade2.observe()));
        assert base.retrieve() == null;

        facade1.emit(true);
        facade2.emit(true);
        assert base.retrieve() == false;

        facade1.emit(false);
        assert base.retrieve() == false;

        facade2.emit(false);
        assert base.retrieve() == true;

        assert base.dispose();
        assert facade1.isCompleted();
        assert facade2.isCompleted();
    }

    @Test
    public void noneNoArg() {
        EventFacade<Boolean, Boolean> base = new EventFacade(events -> Events.none());
        assert base.retrieve() == null;
        assert base.dispose();
    }

    @Test
    public void from() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> Events.from(1, 2, 3));

        assert facade.retrieve() == 1;
        assert facade.retrieve() == 2;
        assert facade.retrieve() == 3;
        assert facade.dispose();
    }

    @Test
    public void fromNoArg() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> Events.from());
        assert facade.retrieve() == null;
        assert facade.dispose();
    }

    @Test
    public void fromIterable() {
        EventFacade<Integer, Integer> facade = new EventFacade<>(events -> Events.from(Arrays.asList(1, 2, 3)));

        assert facade.retrieve() == 1;
        assert facade.retrieve() == 2;
        assert facade.retrieve() == 3;
        assert facade.dispose();
    }
}
