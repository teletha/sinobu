/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub;

import org.junit.Test;

/**
 * @version 2012/01/13 22:11:44
 */
public class PowerAssertUserTest {

    /** The tester. */
    private int intField = 11;

    /** The tester. */
    private static int intFieldStatic = 11;

    @Test
    public void fieldIntStaticAccess() throws Exception {
        int[] array = {0, 1, 2};

        assert array.length == 10;
    }

    @Test
    public void fieldIntStaticAcces1s() throws Exception {
        String value = "aaaa";

        assert new int[] {1, 2} == null;
    }
}
