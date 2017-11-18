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

/**
 * @version 2017/11/15 23:26:54
 */
public class ShareTest {

    @Test
    public void noShare() throws Exception {
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
    public void share() throws Exception {
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
}
