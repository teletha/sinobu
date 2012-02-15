/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.xml;

import static antibug.xml.XML.*;

import java.util.EnumSet;

import org.junit.Test;

/**
 * @version 2012/02/15 1:18:03
 */
public class XMLTest {

    private EnumSet<XMLAmbiguity> ignorePrefix = EnumSet.of(XMLAmbiguity.Prefix);

    @Test
    public void elementLocalName() throws Exception {
        XML one = xml("<Q/>");
        XML other = xml("<P/>");

        assert !one.equals(other);
    }

    @Test
    public void elementURI() throws Exception {
        XML one = xml("<m xmlns='P'/>");
        XML other = xml("<m xmlns='Q'/>");

        assert !one.equals(other);
    }

    @Test
    public void elementName() throws Exception {
        XML one = xml("<Q:m xmlns:Q='ns'/>");
        XML other = xml("<P:m xmlns:P='ns'/>");

        assert !one.equals(other);
    }

    @Test
    public void elementNameIgnorePrefix() throws Exception {
        XML one = xml("<Q:m xmlns:Q='ns'/>");
        XML other = xml("<P:m xmlns:P='ns'/>");

        assert one.equals(other, ignorePrefix);
    }
}
