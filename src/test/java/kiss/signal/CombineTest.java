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

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Ⅱ;
import kiss.Ⅲ;

/**
 * @version 2018/03/20 22:40:11
 */
public class CombineTest extends SignalTester {

    private final Function<Ⅱ<String, String>, String> composer2 = v -> v.ⅰ + v.ⅱ;

    private final Function<Ⅲ<String, Integer, String>, String> composer3 = v -> v.ⅰ + v.ⅱ + v.ⅲ;

    @Test
    public void combine() {
        monitor(signal -> signal.combine(other.signal()).map(composer2));

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

        main.dispose();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    public void disposeByOther() {
        monitor(signal -> signal.combine(other.signal()));

        other.dispose();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    public void completeByMain() {
        monitor(signal -> signal.combine(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        assert main.emit(Complete, "Main is completed so this value will be ignored.").value();
        assert other.emit("Other is also disposed").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    public void completeByOther() {
        monitor(signal -> signal.combine(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        assert other.emit(Complete, "Other is completed so this value will be ignored.").value();
        assert main.emit("Main is also disposed").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    public void errorByMain() {
        monitor(signal -> signal.combine(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        assert main.emit(Error, "Main is errored so this value will be ignored.").value();
        assert other.emit("Other is also disposed").value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    public void errorByOther() {
        monitor(signal -> signal.combine(other.signal()));

        main.emit("MAIN");
        other.emit("OTHER");
        assert main.value(I.pair("MAIN", "OTHER"));

        assert other.emit(Error, "Other is errored so this value will be ignored.").value();
        assert main.emit("Main is also disposed").value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isError();
        assert other.isDisposed();
    }

    @Test
    public void acceptNull() {
        monitor(signal -> signal.combine(other.signal()).map(composer2));

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
    public void ternary() throws Exception {
        monitor(signal -> signal.combine(other.signal(), another.signal()).map(composer3));

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
        other.emit(2).value();
        main.emit("B");
        assert main.value();
        another.emit("b");
        assert main.value("B2b");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
        assert another.isNotCompleted();

        // from another
        another.emit("c").value();
        other.emit(3);
        assert main.value();
        main.emit("C");
        assert main.value("C3c");
        assert main.isNotCompleted();
        assert other.isNotCompleted();
        assert another.isNotCompleted();
    }
}
