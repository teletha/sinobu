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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/09/23 16:37:54
 */
class SwitchOnTest extends SignalTester {

    @Test
    void on() {
        monitor(signal -> signal.switchOn(other.signal().startWith(true)));

        assert main.emit("Success").value("Success");
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert main.countObservers() == 1;

        // stop
        other.emit(false);
        assert main.emit("Fail").value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert main.countObservers() == 0;

        // restart
        other.emit(true);
        assert main.emit("Success").value("Success");
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert main.countObservers() == 1;

        // restop
        other.emit(false);
        assert main.emit("Fail").value();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert main.countObservers() == 0;

        // restart
        other.emit(true);
        assert main.emit("Success").value("Success");
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        assert main.countObservers() == 1;

        // dispose
        main.dispose();
        assert main.emit("Failed").value();
        assert main.isNotCompleted();
        assert main.isDisposed();
        assert main.countObservers() == 0;
    }
}