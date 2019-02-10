/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.WiseFunction;
import kiss.Ⅱ;
import kiss.Ⅲ;

/**
 * @version 2018/03/20 22:40:52
 */
class CombineLatestTest extends SignalTester {

    private final WiseFunction<Ⅱ<String, String>, String> composer2 = v -> v.ⅰ + v.ⅱ;

    private final WiseFunction<Ⅲ<String, Integer, String>, String> composer3 = v -> v.ⅰ + v.ⅱ + v.ⅲ;

    @Test
    void combineLatest() {
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
    void disposeByMain() {
        monitor(signal -> signal.combineLatest(other.signal()));

        main.dispose();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void disposeByOther() {
        monitor(signal -> signal.combineLatest(other.signal()));

        other.dispose();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void completeByMain() {
        monitor(signal -> signal.combineLatest(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        // from main
        assert main.emit(Complete, "Main is completed so this value will be ignored.").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isNotDisposed();
        assert other.emit("Other is not completed.").value(I.pair("MAIN", "Other is not completed."));

        // from other
        assert other.emit(Complete, "Other is completed so this value will be ignored.").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void completeByOther() {
        monitor(signal -> signal.combineLatest(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        // from other
        assert other.emit(Complete, "Other is completed so this value will be ignored.").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
        assert main.emit("Main is not completed.").value(I.pair("Main is not completed.", "OTHER"));

        // from main
        assert main.emit(Complete, "Main is completed so this value will be ignored.").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void errorByMain() {
        monitor(signal -> signal.combineLatest(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        assert main.emit(Error, "Main is errored so this value will be ignored.").value();
        assert other.emit("Other is also disposed.").value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void errorByOther() {
        monitor(signal -> signal.combineLatest(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        assert other.emit(Error, "Other is errored so this value will be ignored.").value();
        assert main.emit("Main is also disposed.").value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isError();
        assert other.isDisposed();
    }

    @Test
    void acceptNull() {
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
    void ternary() throws Exception {
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
