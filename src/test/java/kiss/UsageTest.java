/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import org.junit.jupiter.api.Test;

class UsageTest {

    /**
     * Create instance.
     */
    @Test
    void createInstance() {
        class Some {
        }

        Some prototype1 = I.make(Some.class);
        Some prototype2 = I.make(Some.class);
        assert prototype1 != prototype2;
    }

    /**
     * Create singleton instance. (managed lifestyle)
     */
    @Test
    void createSingleton() {
        @Managed(Singleton.class)
        class Some {
        }

        Some singleton1 = I.make(Some.class);
        Some singleton2 = I.make(Some.class);

        assert singleton1 == singleton2;
    }

    /**
     * Dependency injection. (No configuration)
     */
    @Test
    @SuppressWarnings("unused")
    void dependencyInjection() {
        class Injected {
        }

        class Injectable {
            Injected injected;

            Injectable(Injected injected) {
                this.injected = injected;
            }
        }

        Injectable Injected = I.make(Injectable.class);

        assert Injected.injected != null;
    }
}
