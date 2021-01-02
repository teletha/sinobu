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

class PrototypeTest {

    @Test
    void resolve() {
        PrototypeClass instance1 = I.make(PrototypeClass.class);
        assert instance1 != null;

        PrototypeClass instance2 = I.make(PrototypeClass.class);
        assert instance2 != null;
        assert instance1 != instance2;
    }

    @Managed
    private static class PrototypeClass {
    }
}