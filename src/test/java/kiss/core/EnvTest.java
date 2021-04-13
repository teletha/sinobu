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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import kiss.I;

class EnvTest {

    /**
     * @see I#env(String)
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "TEMP", matches = ".*")
    void env() {
        assert I.env("TEMP") != null; // The value varies depending on the execution environment.
        assert I.env("not-exist") == null;
    }

    /**
     * @see I#env(String, String)
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "TEMP", matches = ".*")
    void known() {
        assert I.env("TEMP", "default value") != "default value";
    }

    /**
     * @see I#env(String, String)
     */
    @Test
    void unknown() {
        assert I.env("unknown", "default value") == "default value";
    }

    /**
     * @see I#env(String, String)
     */
    @Test
    void set() {
        assert I.env("unknown_name") == null;
        assert I.env("unknown_name", "set new environment variable") == "set new environment variable";
        assert I.env("unknown_name") == "set new environment variable";
    }
}
