/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.sax;

import static antibug.AntiBug.*;
import kiss.I;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @version 2012/02/18 13:10:03
 */
public class CustomAttributeTest {

    @Test
    public void custom() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "root")
            public void root(Custom custom) throws SAXException {
                assert custom != null;
            }
        };

        I.parse(note("<root/>"), scanner);
    }

    /**
     * @version 2011/04/13 19:05:19
     */
    protected static class Custom extends AttributesImpl {
    }
}
