/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.html;

import org.junit.Test;

/**
 * @version 2012/02/22 22:57:01
 */
public class CSSTest {

    @Test
    public void css() throws Exception {
        CSS css = new CSS() {

            {
                indent(1, em);
            }
        };
    }
}
