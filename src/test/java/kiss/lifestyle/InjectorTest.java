/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lifestyle;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Lifestyle;

class InjectorTest {

    @Test
    void inject() {
        Lifestyle<InjectableParam> prototype = I.prototype(InjectableParam.class, type -> "name");
        assert prototype.get().name.equals("name");
    }

    static class InjectableParam {

        private String name;

        InjectableParam(String name) {
            this.name = name;
        }
    }
}
