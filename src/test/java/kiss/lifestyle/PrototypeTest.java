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

public class PrototypeTest {

    @Test
    public void prototype() {
        class Person {
        }

        Person someone = I.make(Person.class);
        Person anyone = I.make(Person.class);
        assert someone != anyone; // two different instances
    }
}