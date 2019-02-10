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

/**
 * @version 2018/09/19 16:09:03
 */
class SkipCompleteTest extends SignalTester {

    @Test
    void complete() {
        monitor(signal -> signal.skipComplete());

        assert main.emit(Complete, 1, 2).value(1, 2);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }
}
