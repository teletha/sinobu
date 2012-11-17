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
import kiss.XML;

import org.junit.Test;

/**
 * @version 2012/11/16 12:45:36
 */
public class XMLParseTest {

    @Test
    public void url() throws Exception {
        XML xml = I.xml("http://ja.wikipedia.org/wiki/%E3%82%AF%E3%83%9E");

        assert xml.find("#firstHeading").text().equals("クマ");
    }

    @Test
    public void taa() throws Exception {
        XML xml = I.xml("http://mtgwiki.com/wiki/%E7%86%8A");
        System.out.println(xml);
        assert xml.find("#firstHeading").text().equals("クマ");
    }
}
