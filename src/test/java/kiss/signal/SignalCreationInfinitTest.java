/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/06/21 9:30:33
 */
public class SignalCreationInfinitTest {

    @Test
    void infinite() {
        I.signal(1, v -> v + 1).to(v -> {
            if (v % 100 == 0) {
                System.out.println(v);
            }
        });
    }
}
