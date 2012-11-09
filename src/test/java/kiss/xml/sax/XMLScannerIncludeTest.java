/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.sax;

import static antibug.AntiBug.*;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import antibug.util.Note;
import antibug.xml.XML;

/**
 * @version 2012/02/18 10:16:01
 */
public class XMLScannerIncludeTest {

    private static final Note include = note("<included><child/></included>");

    @Test
    public void path() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "root")
            public void in() {
                start("root");
                include(include);
                end();
            }
        };

        XML xml = xml("<root/>", scanner);
        XML expect = xml("<root><included><child/></included></root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void pathWithFilter() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "root")
            public void in() {
                start("root");
                include(include, new StripChildElement());
                end();
            }
        };

        XML xml = xml("<root/>", scanner);
        XML expect = xml("<root><included/></root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void bit() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            private Bits bits;

            @Rule(match = "from")
            public void from(Bits bits) {
                this.bits = bits;

                element("from");
            }

            @Rule(match = "to")
            public void to() {
                start("to");
                include(bits);
                end();
            }
        };

        XML xml = xml("<root><from><a>text</a></from><to/></root>", scanner);
        XML expect = xml("<root><from/><to><a>text</a></to></root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void bitWithProceed() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            Bits bits;

            @Rule(match = "from")
            public void from(Bits bits) {
                this.bits = bits;

                start("from");
                proceed();
                end();
            }

            @Rule(match = "to")
            public void to() {
                start("to");
                include(bits);
                end();
            }
        };

        XML xml = xml("<root><from><a>text</a></from><to/></root>", scanner);
        XML expect = xml("<root><from><a>text</a></from><to><a>text</a></to></root>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * @version 2011/04/15 13:45:43
     */
    private static class StripChildElement extends XMLScanner {

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
         *      java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (!localName.equals("child")) {
                super.startElement(uri, localName, qName, atts);
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
         *      java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!localName.equals("child")) {
                super.endElement(uri, localName, qName);
            }
        }
    }
}
