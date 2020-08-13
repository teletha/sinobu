/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

import kiss.Disposable;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2018/09/22 14:21:19
 */
class ShareTest extends SignalTester {

    @Test
    void noShare() {
        List<Observer<String>> observers = new ArrayList();
        List<String> result1 = new ArrayList();
        List<String> result2 = new ArrayList();

        Signal<String> source = new Signal(observers);
        Disposable disposable1 = source.to(result1::add);
        Disposable disposable2 = source.to(result2::add);

        // publish
        observers.stream().forEach(o -> o.accept("ONE"));
        assert observers.size() == 2;
        assert result1.size() == 1;
        assert result2.size() == 1;

        // dispose 1
        disposable1.dispose();
        observers.stream().forEach(o -> o.accept("TWO"));
        assert observers.size() == 1;
        assert result1.size() == 1;
        assert result2.size() == 2;

        // dispose 2
        disposable2.dispose();
        observers.stream().forEach(o -> o.accept("THREE"));
        assert observers.size() == 0;
        assert result1.size() == 1;
        assert result2.size() == 2;
    }

    @Test
    void shareMultiple() {
        List<Observer<String>> observers = new ArrayList();
        List<String> result1 = new ArrayList();
        List<String> result2 = new ArrayList();

        Signal<String> source = new Signal(observers).share();
        Disposable disposable1 = source.to(result1::add);
        Disposable disposable2 = source.to(result2::add);

        // publish
        observers.stream().forEach(o -> o.accept("ONE"));
        assert observers.size() == 1;
        assert result1.size() == 1;
        assert result2.size() == 1;

        // dispose 1
        disposable1.dispose();
        observers.stream().forEach(o -> o.accept("TWO"));
        assert observers.size() == 1;
        assert result1.size() == 1;
        assert result2.size() == 2;

        // dispose 2
        disposable2.dispose();
        observers.stream().forEach(o -> o.accept("THREE"));
        assert observers.size() == 0;
        assert result1.size() == 1;
        assert result2.size() == 2;
    }

    @Test
    void shareTwiceByDispose() {
        List<Observer<String>> observers = new ArrayList();
        List<String> result1 = new ArrayList();

        Signal<String> source = new Signal(observers).share();
        Disposable disposable1 = source.to(result1::add);

        // publish
        observers.stream().forEach(o -> o.accept("Success"));
        assert observers.size() == 1;
        assert result1.size() == 1;

        // dispose 1
        disposable1.dispose();
        observers.stream().forEach(o -> o.accept("Fail"));
        assert observers.size() == 0;
        assert result1.size() == 1;

        // restart
        Disposable disposable2 = source.to(result1::add);
        observers.stream().forEach(o -> o.accept("Success"));
        assert observers.size() == 1;
        assert result1.size() == 2;

        // dispose again
        disposable2.dispose();
        observers.stream().forEach(o -> o.accept("Fail"));
        assert observers.size() == 0;
        assert result1.size() == 2;
    }

    @Test
    void shareTwiceByComplete() {
        List<Observer<String>> observers = new CopyOnWriteArrayList();
        List<String> result1 = new ArrayList();

        Signal<String> source = new Signal(observers).share();
        Disposable disposable1 = source.to(result1::add);

        // publish
        observers.stream().forEach(o -> o.accept("Success"));
        assert observers.size() == 1;
        assert result1.size() == 1;
        assert disposable1.isDisposed() == false;

        // complete 1
        observers.stream().forEach(o -> o.complete());
        observers.stream().forEach(o -> o.accept("Fail"));
        assert observers.size() == 0;
        assert result1.size() == 1;
        assert disposable1.isDisposed();

        // restart
        Disposable disposable2 = source.to(result1::add);
        observers.stream().forEach(o -> o.accept("Success"));
        assert observers.size() == 1;
        assert result1.size() == 2;
        assert disposable2.isDisposed() == false;

        // complete again
        observers.stream().forEach(o -> o.complete());
        observers.stream().forEach(o -> o.accept("Fail"));
        assert observers.size() == 0;
        assert result1.size() == 2;
        assert disposable2.isDisposed();
    }

    @Test
    void error() {
        monitor(Integer.class, signal -> signal.share().map(v -> 24 / v));

        assert main.emit(3, 4).value(8, 6);
        assert main.emit(0).value();
        assert main.isError();
        assert main.isDisposed();
    }

}