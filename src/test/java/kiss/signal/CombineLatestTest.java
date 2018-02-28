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

import kiss.SignalTester;
import kiss.Ⅱ;
import kiss.Ⅲ;

/**
 * @version 2018/02/28 19:23:27
 */
public class CombineLatestTest extends SignalTester {

    private final Function<Ⅱ<String, String>, String> composer2 = v -> v.ⅰ + v.ⅱ;

    private final Function<Ⅲ<String, Integer, String>, String> composer3 = v -> v.ⅰ + v.ⅱ + v.ⅲ;

    @Test
    public void combineLatest() {
        monitor(signal -> signal.combineLatest(other.signal()).map(composer2));

        // from main
        assert main.emit("A").value();
        other.emit("a");
        assert main.value("Aa");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other
        other.emit("b");
        assert main.value("Ab");
        assert main.emit("B").value("Bb");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from main multiple
        assert main.emit("C", "D", "E").value("Cb", "Db", "Eb");
        other.emit("c", "d", "e");
        assert main.value("Ec", "Ed", "Ee");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other multiple
        other.emit("f", "g", "h");
        assert main.value("Ef", "Eg", "Eh");
        assert main.emit("F", "G", "H").value("Fh", "Gh", "Hh");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
    }

    @Test
    public void disposeByMain() {
        monitor(signal -> signal.combineLatest(other.signal()));

        // from main
        main.dispose();

        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    public void disposeByOther() {
        monitor(signal -> signal.combineLatest(other.signal()));

        // from other
        other.dispose();

        assert main.isDisposed();
        assert other.isDisposed();
    }

    @Test
    public void completeByMain() {
        monitor(signal -> signal.combineLatest(other.signal()));

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
        monitor(signal -> signal.combineLatest(other.signal()));

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
        monitor(signal -> signal.combineLatest(other.signal()).map(composer2));

        // from main
        assert main.emit((String) null).value();
        other.emit("a");
        assert main.value("nulla");
        assert main.isNotCompleted();
        assert other.isNotCompleted();

        // from other
        other.emit((String) null);
        assert main.value("nullnull");
        assert main.emit("B").value("Bnull");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
    }

    @Test
    public void ternary() throws Exception {
        monitor(signal -> signal.combineLatest(other.signal(), another.signal()).map(composer3));

        // from main
        assert main.emit("A").value();
        other.emit(1);
        assert main.value();
        another.emit("a");
        assert main.value("A1a");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
        assert another.isNotCompleted();

        // from other
        other.emit(2).value("A2a");
        main.emit("B");
        assert main.value("B2a");
        another.emit("b");
        assert main.value("B2b");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
        assert another.isNotCompleted();

        // from another
        another.emit("c").value("B2c");
        other.emit(3);
        assert main.value("B3c");
        main.emit("C");
        assert main.value("C3c");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
        assert another.isNotCompleted();
    }
}
