/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model.external;

import org.junit.jupiter.api.Test;

import kiss.model.Model;

class InExternalPackageModelTest {

    @Test
    void fieldPropertyInPrivateClass() {
        Model model = Model.of(PrivateClass.class);
        assert model != null;
        assert model.property("property") != null;
    }

    private static final class PrivateClass {

        @SuppressWarnings("unused")
        public String property;
    }
}