/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import kiss.I;
import kiss.Manageable;
import kiss.Prototype;

import org.junit.jupiter.api.Test;

/**
 * @version 2011/03/22 16:26:42
 */
public class PrototypeTest {

    @Test
    public void resolve() {
        PrototypeClass instance1 = I.make(PrototypeClass.class);
        assert instance1 != null;

        PrototypeClass instance2 = I.make(PrototypeClass.class);
        assert instance2 != null;
        assert instance1 != instance2;
    }

    /**
     * @version 2011/03/22 16:30:07
     */
    @Manageable(lifestyle = Prototype.class)
    private static class PrototypeClass {
    }
}
