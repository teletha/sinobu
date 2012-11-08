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

import static antibug.AntiBug.*;
import kiss.I;
import kiss.XML;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version 2012/11/08 14:03:34
 */
public class XMLParseTest {

    @Test
    public void path() throws Exception {
        XML xml = I.build(note("<Q/>"));

        assert xml.toString().equals("<Q/>");
        assert xml.find("Q").size() == 0;
    }

    @Test
    public void pathWithFilter() throws Exception {
        XML xml = I.build(note("<Q/>"), new XMLFilterImpl() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                super.startElement(uri, "wrap", "wrap", atts);
                super.startElement(uri, localName, qName, atts);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                super.endElement(uri, "wrap", "wrap");
            }
        });
        assert xml.find("Q").size() == 1;
    }

    @Test
    public void nextUntil() throws Exception {
        XML xml = I.xml("<p><Q/><A/><A/><B/><Q/><A/><A/></p>");
        xml.find("Q").nextUntil("B").wrapAll("wrap");
        System.out.println(xml);
    }
}
