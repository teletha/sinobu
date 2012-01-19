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

import static hub.Ezunit.*;
import hub.SAXBuilder;

import java.io.IOException;

import kiss.I;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version 2010/02/05 1:31:37
 */
public class XMLScannerTest {

    /**
     * Parse with filter. No {@link NullPointerException}.
     */
    @Test
    public void testXMLScanner() throws IOException {
        I.parse(locateSource("scanner/test001.xml"), new XMLScanner());
    }

    /**
     * Test XMLScanner.
     */
    @Test
    public void testXMLScanner1() throws Exception {
        assertXMLIdentical("scanner/expected001.xml", "scanner/test001.xml", new XMLScanner());
    }

    /**
     * Test XMLScanner.
     */
    @Test
    public void testXMLScanner2() throws Exception {
        XMLFilter first = new XMLScanner();

        XMLFilter second = new XMLScanner();
        second.setParent(first);

        XMLFilter third = new XMLScanner();
        third.setParent(second);

        // assertXMLIdentical("scanner/expected002.xml", "scanner/test001.xml", third);
        assert xml("scanner/expected002.xml").equals(xml("scanner/test001.xml", third));
    }

    /**
     * Test XMLScanner.
     */
    @Test
    public void testXMLScanner3() throws Exception {
        assertXMLIdentical("scanner/expected002.xml", "scanner/test002.xml", new Encloser("enclose"));
    }

    /**
     * Test XMLScanner.
     */
    @Test
    public void testXMLScanner4() throws Exception {
        XMLFilter first = new Encloser("first");

        XMLFilter second = new Encloser("second");
        second.setParent(first);

        XMLFilter third = new Encloser("third");
        third.setParent(second);

        assertXMLIdentical("scanner/expected003.xml", "scanner/test003.xml", third);
    }

    /**
     * Test writing xml with helper method.
     */
    @Test
    public void testWriting01() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.start("root");
        writer.end();
        writer.endDocument();

        // assert
        assertXMLIdentical(locateDOM("scanner/expected20.xml"), builder.getDocument());
    }

    /**
     * Test writing xml with helper method.
     */
    @Test
    public void testWriting02() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.start("root", "name1", "value1", "name2", "value2");
        writer.text("test");
        writer.end();
        writer.endDocument();

        // assert
        assertXMLIdentical(locateDOM("scanner/expected21.xml"), builder.getDocument());
    }

    /**
     * Test writing xml with helper method.
     */
    @Test
    public void testWriting03() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("", "default");
        writer.start("root");
        writer.end();
        writer.endPrefixMapping("");
        writer.endDocument();

        // assert
        assertXMLIdentical(locateDOM("scanner/expected22.xml"), builder.getDocument());
    }

    /**
     * Test writing xml with helper method.
     */
    @Test
    public void testWriting04() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("test", "first");
        writer.start("test:root");
        writer.startPrefixMapping("test", "second");
        writer.start("test:in");
        writer.end();
        writer.endPrefixMapping("test");
        writer.start("test:in");
        writer.end();
        writer.end();
        writer.endPrefixMapping("test");
        writer.endDocument();

        // assert
        Document document = builder.getDocument();
        assertXMLIdentical(locateDOM("scanner/expected23.xml"), document);
    }

    /**
     * Test writing xml with helper method.
     */
    @Test
    public void testWriting05() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("test", "first");
        writer.start("root", "test:name1", "value1");
        writer.text("test");
        writer.end();
        writer.endPrefixMapping("test");
        writer.endDocument();

        // assert
        assertXMLIdentical(locateDOM("scanner/expected24.xml"), builder.getDocument());
    }

    /**
     * Test writing xml with helper method.
     */
    @Test
    public void testWriting06() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("test", "ns");
        writer.start("root");

        for (int i = 0; i < 3; i++) {
            writer.start("test:item");
            writer.text(String.valueOf(i));
            writer.end();
        }

        writer.end();
        writer.endPrefixMapping("test");
        writer.endDocument();

        // assert
        assertXMLIdentical(locateDOM("scanner/expected27.xml"), builder.getDocument());
    }

    /**
     * Test xml include (not XInclude).
     */
    @Test
    public void testInclude01() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.start("root");
        I.parse(locateSource("scanner/include00.xml"), new DocumentStriper(), writer);
        writer.end();
        writer.endDocument();

        // assert
        assertXMLIdentical(locateDOM("scanner/expected28.xml"), builder.getDocument());
    }

    /**
     * Test to avoid the circular method calling.
     */
    @Test(expected = StackOverflowError.class)
    public void testOverrideMethodLoop01() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner() {

            /**
             * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
             *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
             */
            @Override
            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                start("add");
                end();
            }
        };
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startElement("", "root", "root", new AttributesImpl());
        writer.endElement("", "root", "root");
        writer.endDocument();

        // assert
        Document document = builder.getDocument();
        assertXMLIdentical(locateDOM("scanner/expected26.xml"), document);
    }

    /**
     * Test to avoid the circular method calling.
     */
    @Test(expected = StackOverflowError.class)
    public void testOverrideMethodLoop02() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner() {

            /**
             * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
             *      java.lang.String)
             */
            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {
                start("add");
                end();
                super.endElement(uri, localName, name);
            }
        };
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startElement("", "root", "root", new AttributesImpl());
        writer.endElement("", "root", "root");
        writer.endDocument();

        // assert
        Document document = builder.getDocument();
        assertXMLIdentical(locateDOM("scanner/expected26.xml"), document);
    }

    /**
     * Test to avoid the circular method calling.
     */
    @Test(expected = StackOverflowError.class)
    public void testOverrideMethodLoop03() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner() {

            /**
             * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
             */
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                text("@");
                super.characters(ch, start, length);
                text("@");
            }
        };
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startElement("", "root", "root", new AttributesImpl());
        writer.characters("test".toCharArray(), 0, 4);
        writer.endElement("", "root", "root");
        writer.endDocument();

        // assert
        Document document = builder.getDocument();
        assertXMLIdentical(locateDOM("scanner/expected25.xml"), document);
    }

    /**
     * Test method for 'org.trix.tidori.xml.DefaultXMLFilter.resolveEntity(String, String)'
     */
    @Test
    public void testResolveEntityWithoutAdditionalResolving() throws Exception {
        assertXMLIdentical("scanner/expected006.xml", "scanner/test006.xml", new XMLFilterImpl());
    }

    /**
     * Test method for 'org.trix.tidori.xml.DefaultXMLFilter.resolveEntity(String, String)'
     */
    @Test
    public void testResolveEntityWithAdditionalResolving() throws Exception {
        assertXMLIdentical("scanner/expected007.xml", "scanner/test007.xml", new EntityResolvableBuilder());
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/06/04 11:54:16
     */
    private static class EntityResolvableBuilder extends XMLFilterImpl {

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#resolveEntity(java.lang.String, java.lang.String)
         */
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return locateSource("scanner/entity01.dtd");
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/10/30 14:27:00
     */
    private static class DocumentStriper extends XMLFilterImpl {

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
         */
        @Override
        public void endDocument() throws SAXException {
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/10/22 16:06:21
     */
    @SuppressWarnings("unused")
    private static class RootStriper extends XMLFilterImpl {

        private int count = 0;

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
         *      java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
            count++;

            if (count > 1) {
                super.startElement(uri, localName, name, atts);
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
         *      java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (count > 1) {
                super.endElement(uri, localName, name);
            }
            count--;
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            // do nothing
        }
    }
}
