/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

class MapErrorTest extends SignalTester {

    @Test
    void mapError() {
        monitor(signal -> signal.mapError(v -> new IllegalArgumentException(v)));

        main.emit(Error, "IGNORED").value();
        assert main.isNotCompleted();
        assert main.isError(IllegalArgumentException.class);
        assert main.isDisposed();
    }

    @Test
    void mapErrorComplete() {
        monitor(signal -> signal.mapError(v -> new IllegalArgumentException(v)));

        assert main.emit(1, Complete).value(1);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}