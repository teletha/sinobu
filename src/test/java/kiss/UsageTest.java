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
        class PrototypeClass {
        }

        PrototypeClass prototype1 = I.make(PrototypeClass.class);
        PrototypeClass prototype2 = I.make(PrototypeClass.class);
        assert prototype1 != prototype2;
    }

    /**
     * Create singleton instance. (managed lifestyle)
     */
    @Test
    void createSingleton() {
        @Managed(Singleton.class)
        class SingletonClass {
        }

        SingletonClass singleton1 = I.make(SingletonClass.class);
        SingletonClass singleton2 = I.make(SingletonClass.class);

        assert singleton1 == singleton2;
    }
}
