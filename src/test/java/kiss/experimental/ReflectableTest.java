/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import org.junit.jupiter.api.Test;

import kiss.experimental.Reflectable.ReflectableConsumer;

class ReflectableTest {

    @Test
    void reflect() {
        ReflectableConsumer consumer = value -> {
        };

        assert consumer.method().getDeclaringClass() == ReflectableTest.class;
        assert Reflectable.reflect(consumer).getDeclaringClass() == ReflectableTest.class;
    }
}