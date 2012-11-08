/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import kiss.I;

import org.junit.Test;

/**
 * @version 2012/02/07 19:01:50
 */
public class XMLFilterTest {

    @Test
    public void first() throws Exception {
        String xml = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(xml).find("Q").first().attr("class").equals("first");
    }

    @Test
    public void last() throws Exception {
        String xml = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(xml).find("Q").last().attr("class").equals("last");
    }
}
