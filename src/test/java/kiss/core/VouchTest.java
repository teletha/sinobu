/*
 * Copyright (C) 2024 Nameless Production Committee
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

class VouchTest {

    @Test
    void vouch() {
        assert I.vouch("DEF", "A").equals("A");
        assert I.vouch("DEF", "A", "B", "C").equals("C");
        assert I.vouch("DEF", "A", "B", null).equals("B");
        assert I.vouch("DEF", "A", null, "C").equals("C");
    }

    @Test
    void empty() {
        assert I.vouch("DEF", new String[0]).equals("DEF");
    }

    @Test
    void nullValues() {
        assert I.vouch("DEF", (String[]) null).equals("DEF");
    }

    @Test
    void nullDefault() {
        assert I.vouch(null, (String[]) null) == null;
    }
}
