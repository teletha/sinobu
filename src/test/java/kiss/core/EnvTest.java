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
        assert I.env("TEMP", "default value").equals("default value") == false;
    }

    /**
     * @see I#env(String, String)
     */
    @Test
    void unknown() {
        assert I.env("unknown", "default value").equals("default value");
    }

    /**
     * @see I#env(String, String)
     */
    @Test
    void setValue() {
        assert I.env("unknown_name") == null;
        assert I.env("unknown_name", "set new environment variable") == "set new environment variable";
        assert I.env("unknown_name") == "set new environment variable";
    }

    @Test
    void dontOverride() {
        assert I.env("dont_override", "initial value").equals("initial value");
        assert I.env("dont_override", "this is ignored").equals("initial value");
    }

    @Test
    void primitives() {
        assert I.env("primitive_int", 10) == 10;
        assert I.env("primitive_long", 10L) == 10L;
        assert I.env("primitive_float", 10f) == 10f;
        assert I.env("primitive_double", 10d) == 10d;
        assert I.env("primitive_booelan", true) == true;
        assert I.env("primitive_char", 'c') == 'c';
        assert I.env("primitive_short", (short) 10) == (short) 10;
        assert I.env("primitive_byte", (byte) 10) == (byte) 10;
    }
}
