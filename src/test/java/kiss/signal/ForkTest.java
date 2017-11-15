/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import kiss.Disposable;
import kiss.Observer;
import kiss.Signal;
import kiss.SignalTester;

/**
 * @version 2017/11/15 14:06:16
 */
public class ForkTest extends SignalTester {

    @Test
    public void noFork() {
        List<Observer<String>> observers = new ArrayList();
        List<String> result1 = new ArrayList();
        List<String> result2 = new ArrayList();

        Signal<String> source = new Signal(observers);
        Disposable observe1 = source.to(result1::add);
        Disposable observe2 = source.to(result2::add);

        assert observers.size() == 2;
        assert result1.size() == 0;
        assert result2.size() == 0;

        // publish
        observers.stream().forEach(o -> o.accept("OK"));

        assert observers.size() == 2;
        assert result1.size() == 1;
        assert result2.size() == 1;

        // dispose one
        observe1.dispose();
        observers.stream().forEach(o -> o.accept("OK"));

        assert observers.size() == 1;
        assert result1.size() == 1;
        assert result2.size() == 2;

        // dispose other
        observe2.dispose();
        observers.stream().forEach(o -> o.accept("OK"));

        assert observers.size() == 0;
        assert result1.size() == 1;
        assert result2.size() == 2;
    }

    @Test
    public void fork() {
        List<Observer<String>> observers = new ArrayList();
        List<String> result1 = new ArrayList();
        List<String> result2 = new ArrayList();

        Signal<String> source = new Signal(observers).fork();
        Disposable observe1 = source.to(result1::add);
        Disposable observe2 = source.to(result2::add);

        assert observers.size() == 1;
        assert result1.size() == 0;
        assert result2.size() == 0;

        // publish
        observers.stream().forEach(o -> o.accept("OK"));

        assert observers.size() == 1;
        assert result1.size() == 1;
        assert result2.size() == 1;

        // dispose one
        observe1.dispose();
        observers.stream().forEach(o -> o.accept("OK"));

        assert observers.size() == 1;
        assert result1.size() == 1;
        assert result2.size() == 2;

        // dispose other
        observe2.dispose();
        observers.stream().forEach(o -> o.accept("OK"));

        assert observers.size() == 0;
        assert result1.size() == 1;
        assert result2.size() == 2;
    }

    // @Test
    // public void forkComplete() {
    // List<Observer<String>> observers = new CopyOnWriteArrayList();
    // List<String> result1 = new CopyOnWriteArrayList();
    // List<String> result2 = new CopyOnWriteArrayList();
    //
    // Signal<String> source = new Signal(observers).fork();
    // source.to(result1::add);
    // source.to(result2::add);
    //
    // assert observers.size() == 1;
    // assert result1.size() == 0;
    // assert result2.size() == 0;
    //
    // // publish
    // observers.stream().forEach(o -> o.accept("OK"));
    //
    // assert observers.size() == 1;
    // assert result1.size() == 1;
    // assert result2.size() == 1;
    //
    // // complete
    // observers.stream().forEach(o -> o.complete());
    // assert observers.size() == 0;
    // }
    //
    // @Test
    // public void forkRepeat() {
    // List<Observer<String>> observers = new CopyOnWriteArrayList();
    // List<String> result1 = new CopyOnWriteArrayList();
    // List<String> result2 = new CopyOnWriteArrayList();
    //
    // Signal<String> source = new Signal(observers).fork();
    // source.repeat(2).to(result1::add);
    // source.to(result2::add);
    //
    // assert observers.size() == 1;
    // assert result1.size() == 0;
    // assert result2.size() == 0;
    //
    // // publish
    // observers.stream().forEach(o -> o.accept("ONE"));
    //
    // assert observers.size() == 1;
    // assert result1.size() == 1;
    // assert result2.size() == 1;
    //
    // // complete 1
    // observers.stream().forEach(o -> o.complete());
    // assert observers.size() == 1;
    // assert result1.size() == 1;
    // assert result2.size() == 1;
    //
    // // publish
    // observers.stream().forEach(o -> o.accept("TWO"));
    //
    // assert observers.size() == 1;
    // assert result1.size() == 2;
    // assert result2.size() == 1;
    //
    // // complete 2
    // observers.stream().forEach(o -> o.complete());
    // assert observers.size() == 0;
    // assert result1.size() == 1;
    // assert result2.size() == 1;
    // }
}
