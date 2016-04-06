/*
 * Copyright (C) 2016 Nameless Production Committee
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
 * @version 2012/11/18 2:55:32
 */
public class XMLTraversingTest {

    @Test
    public void first() throws Exception {
        String xml = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(xml).find("Q").first().attr("class").equals("first");
    }

    @Test
    public void firstAtEmpty() throws Exception {
        XML xml = I.xml("<m/>");

        assert xml.find("Q").size() == 0;
        assert xml.find("Q").first().size() == 0;
    }

    @Test
    public void last() throws Exception {
        String xml = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(xml).find("Q").last().attr("class").equals("last");
    }

    @Test
    public void lastAtEmpty() throws Exception {
        XML xml = I.xml("<m/>");

        assert xml.find("Q").size() == 0;
        assert xml.find("Q").last().size() == 0;
    }

    @Test
    public void parent() throws Exception {
        XML xml = I.xml("<m><Q><P/></Q><Q><P/></Q></m>");

        assert xml.find("P").parent().size() == 2;
        assert xml.find("P").parent().parent().size() == 1;
        assert xml.find("P").parent().parent().parent().size() == 1;
        assert xml.find("P").parent().parent().parent().parent().size() == 1;
    }

    @Test
    public void children() throws Exception {
        XML xml = I.xml("<m><Q><P/><R><T/></R></Q><Q><P/></Q></m>");

        assert xml.find("Q").size() == 2;
        assert xml.find("Q").children().size() == 3;
    }

    @Test
    public void nextUntil() throws Exception {
        XML xml = I.xml("<p><Q/><A/><B/><Q/><A class='stop'/><B/><Q/><A/><B/></p>");

        assert xml.find("Q").nextUntil("A").size() == 3;
        assert xml.find("Q").nextUntil("B").size() == 6;
        assert xml.find("Q").nextUntil("A.stop").size() == 7;
    }

    @Test
    public void next() throws Exception {
        XML xml = I.xml("<p><Q/><A>here</A></p>");

        assert xml.find("Q").next().text().equals("here");
    }

    @Test
    public void nextNone() throws Exception {
        XML xml = I.xml("<p><Q/></p>");

        assert xml.find("Q").next().size() == 0;
    }

    @Test
    public void nextMulti() throws Exception {
        XML xml = I.xml("<p><Q/><A/><Q/><B/></p>");

        assert xml.find("Q").next().size() == 2;
    }

    @Test
    public void nextMultiNone() throws Exception {
        XML xml = I.xml("<p><Q/><A/><Q/></p>");

        assert xml.find("Q").next().size() == 1;
    }
}
