/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.jupiter.api.Test;

import kiss.I;

class EnvTest {

    /**
     * @see I#env(String)
     */
    @Test
    void env() {
        assert I.env("TEMP") != null; // The value varies depending on the execution environment.
        assert I.env("NotExist") == null;
    }

    /**
     * @see I#env(String, String)
     */
    @Test
    void defaultValue() {
        assert I.env("TEMP", "default value") != "default value";

        // If it does not exist, it will be set.
        assert I.env("NotFound") == null;
        assert I.env("NotFound", "default value") == "default value";
        assert I.env("NotFound") == "default value";
    }
}
