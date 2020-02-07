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

import kiss.Disposable;

class DisposeTest extends SignalTester {

    @Test
    void disposeOnComplete() {
        monitor(signal -> signal);

        assert main.emit(Complete, "This value and next error will be ignored", Error).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void disposeOnError() {
        monitor(signal -> signal);

        assert main.emit(Error, "This value and next complete will be ignored", Complete).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void disposedBy() {
        Disposable parent = Disposable.empty();
        monitor(signal -> signal.disposedBy(parent));
        assert main.isNotDisposed();

        parent.dispose();
        assert main.isDisposed();
    }

    @Test
    void disposedByNull() {
        monitor(signal -> signal.disposedBy(null));
        assert main.isNotDisposed();
    }
}
