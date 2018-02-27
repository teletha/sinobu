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

import java.util.function.Function;

import org.junit.Test;

import kiss.I;
import kiss.SignalTester;
import kiss.Ⅱ;
import kiss.Ⅲ;

/**
 * @version 2018/02/27 18:05:32
 */
public class CombineTest extends SignalTester {

    private final Function<Ⅱ<String, String>, String> composer = v -> v.ⅰ + v.ⅱ;

    @Test
    public void combine() {
        monitor(signal -> signal.combine(other.signal()).map(composer));

        // from main
        assert main.emit("A").value();
        other.emit("a");
        assert main.value("Aa");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other
        other.emit("b");
        assert main.value();
        assert main.emit("B").value("Bb");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from main multiple
        assert main.emit("C", "D", "E").value();
        other.emit("c", "d", "e");
        assert main.value("Cc", "Dd", "Ee");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other multiple
        other.emit("f", "g", "h");
        assert main.value();
        assert main.emit("F", "G", "H").value("Ff", "Gg", "Hh");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
    }

    @Test
    public void disposeByMain() {
        monitor(signal -> signal.combine(other.signal()));

        // from main
        main.dispose();

        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    public void disposeByOther() {
        monitor(signal -> signal.combine(other.signal()));

        // from other
        other.dispose();

        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    public void completeByMain() {
        monitor(signal -> signal.combine(other.signal()));

        // from main
        main.emit(Complete);
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other
        other.emit(Complete);
        assert main.isCompleted();
        assert other.isCompleted();
    }

    @Test
    public void completeByOther() {
        monitor(signal -> signal.combine(other.signal()));

        // from other
        other.emit(Complete);
        assert main.isNotCompleted();
        assert other.isCompleted();

        // from main
        main.emit(Complete);
        assert main.isCompleted();
        assert other.isCompleted();
    }

    @Test
    public void acceptNull() {
        monitor(signal -> signal.combine(other.signal()).map(composer));

        // from main
        assert main.emit((String) null).value();
        other.emit("a");
        assert main.value("nulla");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other
        other.emit((String) null);
        assert main.value();
        assert main.emit("B").value("Bnull");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
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
}
