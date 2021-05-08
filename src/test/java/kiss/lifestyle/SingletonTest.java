/*
 * Copyright (C) 2021 Nameless Production Committee
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
        class Earth {
        }

        Earth one = I.make(Earth.class);
        Earth other = I.make(Earth.class);
        assert one == other; // same instance
    }
}