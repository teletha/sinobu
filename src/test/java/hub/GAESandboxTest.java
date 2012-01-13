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

import java.io.File;
import java.security.AccessControlException;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2010/02/09 13:17:15
 */
public class GAESandboxTest {

    @Rule
    public static GAESandbox sandbox = new GAESandbox();

    @Test(expected = AccessControlException.class)
    public void list() throws Exception {
        new File("").list();
    }
}
