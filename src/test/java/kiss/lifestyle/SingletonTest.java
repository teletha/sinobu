/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;

public class SingletonTest {

    @Test
    public void singleton() {
        @Managed(Singleton.class)
        class Tweet {
        }

        Tweet one = I.make(Tweet.class);
        Tweet other = I.make(Tweet.class);
        assert one == other; // same instance
    }
}