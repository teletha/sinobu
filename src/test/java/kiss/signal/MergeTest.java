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

import kiss.Signal;

/**
 * @version 2018/03/20 23:50:24
 */
class MergeTest extends SignalTester {

    @Test
    void merge() {
        monitor(signal -> signal.merge(other.signal(), another.signal()));

        assert main.emit(1, 2).value(1, 2);
        assert other.emit(10, 20).value(10, 20);
        assert another.emit(100, 200).value(100, 200);
        assert main.emit(3, 4).value(3, 4);
        assert other.emit(30, 40).value(30, 40);
        assert another.emit(300, 400).value(300, 400);

        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotDisposed();
        assert another.isNotCompleted();
        assert another.isNotDisposed();

        // dispose
        main.dispose();
        assert main.isNotCompleted();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isDisposed();
        assert another.isNotCompleted();
        assert another.isDisposed();
    }

    @Test
    void acceptNull() {
        monitor(signal -> signal.merge((Signal) null));

        assert main.emit("accept null").isEmmitted();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void disposeByMain() {
        monitor(signal -> signal.merge(other.signal()));

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
        monitor(signal -> signal.merge(other.signal()));

        other.dispose();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void complete() {
        monitor(signal -> signal.merge(other.signal()).take(3));

        assert main.emit("OK").value("OK");
        assert other.emit("OK").value("OK");
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        assert main.emit("This value will complete merged signal.").value("This value will complete merged signal.");
        assert main.isCompleted();
        assert main.isDisposed();
        assert other.isNotCompleted();
        assert other.isDisposed();
    }

    @Test
    void completeFromMain() {
        monitor(signal -> signal.merge(other.signal()));

        // complete main
        assert main.emit(Complete, "Main is completed so this value will be ignored.").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
        assert other.isNotCompleted();
        assert other.isNotError();
        assert other.isNotDisposed();
        assert other.emit("Other is not completed.").value("Other is not completed.");

        // complete other
        assert other.emit(Complete, "Other is completed so this value will be ignored.").value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
    }

    @Test
    void completeFromOther() {
        monitor(signal -> signal.merge(other.signal()));

        // complete other
        assert other.emit(Complete, "Other is completed so this value will be ignored.").value();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
        assert other.isCompleted();
        assert other.isNotError();
        assert other.isDisposed();
        assert main.emit("Main is not completed.").value("Main is not completed.");

        // complete main
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
        monitor(signal -> signal.merge(other.signal()));

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
    void errorByOther() {
        monitor(signal -> signal.merge(other.signal()));

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
    void completeFromOuterSignal() {
        monitor(() -> signal(1).merge(signal(10, 20, 30)).take(2));

        assert main.value(1, 10);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void completeFromInnerSignal() {
        monitor(1, () -> signal(1).merge(signal(10, 20, 30).take(2)));

        assert main.value(1, 10, 20);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
