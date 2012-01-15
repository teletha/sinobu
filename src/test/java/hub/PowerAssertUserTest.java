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

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2012/01/13 22:11:44
 */
public class PowerAssertUserTest {

    @Rule
    public static final PowerAssert rule = new PowerAssert();

    @Test
    public void testname() throws Exception {
        List list = new ArrayList();
        list.add(1);
        list.add(3);
        list.add("test");

        assert list.size() == 2;
    }

    @Test
    public void testname1() throws Exception {
        List list = new ArrayList();
        list.add(1);
        list.add(3);
        list.add("test");

        assert list.size() == 2;
    }
}
