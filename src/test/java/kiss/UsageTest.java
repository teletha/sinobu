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

        assert I.make(Some.class) != I.make(Some.class);;
    }

    /**
     * Create singleton instance. (managed lifestyle)
     */
    @Test
    void createSingleton() {
        @Managed(Singleton.class)
        class Some {
        }

        assert I.make(Some.class) == I.make(Some.class);
    }

    /**
     * Enable dependency injection without configuration. (constructor injection)
     */
    @Test
    @SuppressWarnings("unused")
    void constructorInjection() {
        class Injected {
        }

        class Injectable {
            Injected injected;

            Injectable(Injected injected) {
                this.injected = injected;
            }
        }

        Injectable Injectable = I.make(Injectable.class);
        assert Injectable.injected != null;
    }
}
