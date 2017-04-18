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
 * @version 2017/04/09 16:18:57
 */
public class FisrtTest extends SignalTester {

    @Test
    public void first() throws Exception {
        monitor(() -> signal(1, 2, 3).first());

        assert main.value(1);
        assert main.isCompleted();
    }
}
