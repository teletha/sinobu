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

import static antibug.AntiBug.*;
import kiss.Element;

import org.junit.Test;

import antibug.xml.XML;

/**
 * @version 2012/02/22 15:45:44
 */
public class DocTest {

    XML html = xml("" +
    /**/"<html>" +
    /**/"   <body>" +
    /**/"   </body>" +
    /**/"</html>");

    @Test
    public void testname() throws Exception {
        Element e = Element.$(html);
        assert e.find("body").size() == 1;
    }
}
