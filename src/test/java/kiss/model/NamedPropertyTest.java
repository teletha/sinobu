/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import org.junit.jupiter.api.Test;

import kiss.Managed;
import kiss.Model;

class NamedPropertyTest {

    @Test
    void field() {
        Model model = Model.of(NamedField.class);
        assert model.property("named") != null;
        assert model.property("value") == null;
    }

    private static class NamedField {
        @Managed(name = "named")
        private String value;
    }
}