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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/09/13 10:34:48
 */
class TypeTest extends SignalTester {

    @Test
    void type() {
        monitor(signal -> signal.type(Integer.class));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void sub() {
        monitor(signal -> signal.type(Number.class));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(10350150106L).value(10350150106L);
        assert main.emit(0.0141516285D).value(0.0141516285D);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void primitive() {
        monitor(signal -> signal.type(double.class));

        assert main.emit(-1.1D).value(-1.1D);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void nullType() {
        monitor(signal -> signal.type((Class) null));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value("5000");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void nullTypes() {
        monitor(signal -> signal.type((Class[]) null));

        assert main.emit(10).value(10);
        assert main.emit(2.1F).value(2.1F);
        assert main.emit(-1.1D).value(-1.1D);
        assert main.emit(20L).value(20L);
        assert main.emit("5000").value("5000");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }
}
