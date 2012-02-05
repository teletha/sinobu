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

import org.junit.Test;

/**
 * @version 2012/02/05 17:08:12
 */
public class ElementFindTest {

    /** The test document. */
    private static final String document = ""
            + "<root>"
            + "     <person class='fire sisters' id='tukihi'>"
            + "         <age>14</age>"
            + "         <person class='brother'>"
            + "             <name>Araragi Koyomi</name>"
            + "         </person>"
            + "         <friend>"
            + "             <person class='friend'>"
            + "                 <name>Hanekawa Tubasa</name>"
            + "             </person>"
            + "         </friend>"
            + "     </person>"
            + "     <person class='fire sisters' id='karen'>"
            + "         <age>14</age>"
            + "         <person class='brother'>"
            + "             <name>Araragi Koyomi</name>"
            + "         </person>"
            + "     </person>"
            + "     <person class='snake' id='nadeko'>"
            + "         <age>14</age>"
            + "     </person>"
            + "</root>";

    @Test
    public void type() throws Exception {
        assert $(document).find("person").size() == 6;
    }

    @Test
    public void clazz() throws Exception {
        assert $(document).find(".fire").size() == 2;
    }

    @Test
    public void clazzMultiple() throws Exception {
        assert $(document).find(".sisters.fire").size() == 2;
    }

    @Test
    public void id() throws Exception {
        assert $(document).find("#nadeko").size() == 1;
    }

    @Test
    public void child() throws Exception {
        assert $(document).find("person > person").size() == 2;
    }

    @Test
    public void next() throws Exception {
        assert $(document).find("person + person").size() == 2;
    }
}
