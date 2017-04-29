/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.Test;

/**
 * @version 2017/04/29 16:42:57
 */
public class ATest {

    @Test
    public void testname() throws Exception {
        Package[] packages = Package.getPackages();

        for (Package package1 : packages) {
            System.out.println(package1);
        }
    }
}
