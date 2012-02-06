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

import static kiss.xml.Element.*;

import java.io.StringReader;

import kiss.I;

import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * @version 2012/02/05 17:08:12
 */
public class ElementFindTest {

    @Test
    public void type() throws Exception {
        String xml = "<m><E/><E/><e><E/></e></m>";

        assert $(xml).find("E").size() == 3;
    }

    @Test
    public void clazz() throws Exception {
        String xml = "<m><e class='C'/><e class='none'/></m>";

        assert $(xml).find(".C").size() == 1;
    }

    @Test
    public void clazzWithMultipleValue() throws Exception {
        String xml = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert $(xml).find(".A").size() == 1;
        assert $(xml).find(".B").size() == 1;
        assert $(xml).find(".C").size() == 1;
    }

    @Test
    public void clazzMultiple() throws Exception {
        String xml = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert $(xml).find(".A.B").size() == 1;
        assert $(xml).find(".B.C").size() == 1;
        assert $(xml).find(".C.A").size() == 1;
    }

    @Test
    public void id() throws Exception {
        String xml = "<m><e id='A'/><e id='AA'/></m>";

        assert $(xml).find("#A").size() == 1;
        assert $(xml).find("#AA").size() == 1;
    }

    @Test
    public void attribute() throws Exception {
        String xml = "<m><e A='A' B='B'/><e A='B' B='A'/></m>";

        assert $(xml).find("[A]").size() == 2;
        assert $(xml).find("[B]").size() == 2;
    }

    @Test
    public void attributeValue() throws Exception {
        String xml = "<m><e A='A'/></m>";

        // variants for white space
        assert $(xml).find("[A=\"A\"]").size() == 1;
        assert $(xml).find("[A = \"A\"]").size() == 1;
        assert $(xml).find("[A       =    \"A\"]").size() == 1;
        assert $(xml).find("[ A = \"A\" ]").size() == 1;
    }

    @Test
    public void attributeConatainValue() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert $(xml).find("[A ~= \"A\"]").size() == 1;
        assert $(xml).find("[A ~= \"B\"]").size() == 1;
        assert $(xml).find("[A ~= \"C\"]").size() == 1;
    }

    @Test
    public void attributeConatainText() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AB'/></m>";

        assert $(xml).find("[A *= \"A\"]").size() == 2;
        assert $(xml).find("[A *= \"B\"]").size() == 2;
        assert $(xml).find("[A *= \"C\"]").size() == 1;
    }

    @Test
    public void attributeStartWith() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert $(xml).find("[A ^= \"A\"]").size() == 2;
        assert $(xml).find("[A ^= \"B\"]").size() == 0;
        assert $(xml).find("[A ^= \"C\"]").size() == 0;
    }

    @Test
    public void attributeEndWith() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert $(xml).find("[A $= \"A\"]").size() == 0;
        assert $(xml).find("[A $= \"B\"]").size() == 0;
        assert $(xml).find("[A $= \"C\"]").size() == 2;
    }

    @Test
    public void child() throws Exception {
        String xml = xml("<m><P><Q/><z><Q/></z><Q/></P><Q/></m>");

        assert $(xml).find("P>Q").size() == 2;
        assert $(xml).find("P > Q").size() == 2;
        assert $(xml).find("P   >    Q").size() == 2;
    }

    @Test
    public void sibling() throws Exception {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert $(xml).find("P+Q").size() == 2;
        assert $(xml).find("P + Q").size() == 2;
        assert $(xml).find("P   +   Q").size() == 2;
    }

    @Test
    public void siblings() throws Exception {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert $(xml).find("P~Q").size() == 3;
        assert $(xml).find("P ~ Q").size() == 3;
        assert $(xml).find("P   ~   Q").size() == 3;
    }

    /**
     * <p>
     * Format to human-redable xml for display when assertion is fail..
     * </p>
     * 
     * @param text
     * @return
     */
    private static final String xml(String text) {
        StringBuilder builder = new StringBuilder();

        // format
        I.parse(new InputSource(new StringReader(text)), new XMLWriter(builder));

        return builder.toString();
    }
}
