/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import org.junit.Test;

import kiss.I;
import kiss.XML;

/**
 * @version 2017/02/05 21:17:41
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
}
