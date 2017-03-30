/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import org.junit.Test;

import kiss.I;
import kiss.XML;

/**
 * @version 2017/03/30 12:11:35
 */
public class HTMLSoupTest {

    @Test
    public void noEndTag() throws Exception {
        XML xml = parse("<link>");
        assert xml.find("link").size() == 1;
    }

    @Test
    public void nameCaseInconsistency() throws Exception {
        XML xml = parse("<case>crazy</CASE>");
        assert xml.find("case").size() == 1;
    }

    /**
     * <p>
     * Parse as HTML.
     * </p>
     */
    private XML parse(String html) {
        return I.xml("<html>" + html + "</html>");
    }
}
