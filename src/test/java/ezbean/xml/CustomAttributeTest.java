/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.xml;

import static ezunit.Ezunit.*;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import ezbean.I;

/**
 * @version 2011/04/13 19:04:00
 */
public class CustomAttributeTest {

    @Test
    public void custom() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Custom custom) throws SAXException {
                assert custom != null;
            }
        };

        I.parse(locateSource("rule/test02.xml"), scanner);
    }

    /**
     * @version 2011/04/13 19:05:19
     */
    protected static class Custom extends AttributesImpl {
    }
}
