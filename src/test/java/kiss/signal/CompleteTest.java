/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.Test;

import kiss.SignalTester;

/**
 * @version 2017/04/14 10:18:28
 */
public class CompleteTest extends SignalTester {

    @Test
    public void complete() throws Exception {
        monitor(signal -> signal);

        assert emit(1, 2, 3, Complete, 4, 5).value(1, 2, 3);
    }
}
