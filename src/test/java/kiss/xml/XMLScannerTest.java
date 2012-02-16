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

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;

import antibug.xml.XML;

/**
 * @version 2012/02/14 15:44:41
 */
public class XMLScannerTest {

    @Test
    public void dontModify() throws Exception {
        XML text = xml("<root/>", new XMLScanner());
        XML expect = xml("<root/>");

        assert text.isIdenticalTo(expect);
    }

    @Test
    public void chainFilter() throws Exception {
        XMLFilter first = new XMLScanner();

        XMLFilter second = new XMLScanner();
        second.setParent(first);

        XMLFilter third = new XMLScanner();
        third.setParent(second);

        XML text = xml("<root/>", third);
        XML expect = xml("<root/>");

        assert text.isIdenticalTo(expect);
    }

    @Test
    public void modifyByFilter() throws Exception {
        XML text = xml("<root/>", new Encloser("top"));
        XML expect = xml("<top><root/></top>");

        assert text.isIdenticalTo(expect);
    }

    @Test
    public void modifyByChinedFilters() throws Exception {
        XMLFilter first = new Encloser("first");

        XMLFilter second = new Encloser("second");
        second.setParent(first);

        XMLFilter third = new Encloser("third");
        third.setParent(second);

        XML text = xml("<root/>", third);
        XML expect = xml("<third><second><first><root/></first></second></third>");

        assert text.isIdenticalTo(expect);
    }

    @Test
    public void writeXML() throws Exception {
        XMLScanner writer = new XMLScanner();
        XML doc = xml(writer);

        // write xml
        writer.startDocument();
        writer.start("root");
        writer.end();
        writer.endDocument();

        XML expect = xml("<root/>");

        assert doc.isIdenticalTo(expect);
    }

    @Test
    public void writeAttributeAndText() throws Exception {
        XMLScanner writer = new XMLScanner();
        XML doc = xml(writer);

        // write xml
        writer.startDocument();
        writer.start("root", "name1", "value1", "name2", "value2");
        writer.text("text");
        writer.end();
        writer.endDocument();

        XML expect = xml("<root name1='value1' name2='value2'>text</root>");

        assert doc.isIdenticalTo(expect);
    }

    @Test
    public void writeNS() throws Exception {
        XMLScanner writer = new XMLScanner();
        XML doc = xml(writer);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("", "default");
        writer.start("root");
        writer.end();
        writer.endPrefixMapping("");
        writer.endDocument();

        XML expect = xml("<root xmlns='default'/>");

        assert doc.isIdenticalTo(expect);
    }

    @Test
    public void writeNSElement() throws Exception {
        XMLScanner writer = new XMLScanner();
        XML doc = xml(writer);

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

        XML expect = xml("<test:root xmlns:test='first'><test:in xmlns:test='second'/><test:in/></test:root>");

        assert doc.isIdenticalTo(expect);
    }

    @Test
    public void writeNSAttribute() throws Exception {
        XMLScanner writer = new XMLScanner();
        XML doc = xml(writer);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("test", "first");
        writer.start("root", "test:name1", "value1");
        writer.text("text");
        writer.end();
        writer.endPrefixMapping("test");
        writer.endDocument();

        XML expect = xml("<root xmlns:test='first' test:name1='value1'>text</root>");

        assert doc.isIdenticalTo(expect);
    }

    @Test
    public void writeLoop() throws Exception {
        XMLScanner writer = new XMLScanner();
        XML doc = xml(writer);
        XML expect = xml("<root xmlns:ns='ns'><ns:m>0</ns:m><ns:m>1</ns:m><ns:m>2</ns:m></root>");

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("ns", "ns");
        writer.start("root");
        for (int i = 0; i < 3; i++) {
            writer.start("ns:m");
            writer.text(String.valueOf(i));
            writer.end();
        }
        writer.end();
        writer.endPrefixMapping("ns");
        writer.endDocument();

        assert doc.isIdenticalTo(expect);
    }

    /**
     * Test to avoid the circular method calling.
     */
    @Test(expected = StackOverflowError.class)
    public void overrideStartElement() throws Exception {
        XMLScanner writer = new XMLScanner() {

            @Override
            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                start("add");
            }
        };

        // write xml
        writer.startDocument();
        writer.startElement("", "root", "root", new AttributesImpl());
        writer.endElement("", "root", "root");
        writer.endDocument();
    }

    /**
     * Test to avoid the circular method calling.
     */
    @Test(expected = StackOverflowError.class)
    public void overrideEndElement() throws Exception {
        XMLScanner writer = new XMLScanner() {

            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {
                start("add");
                end();
            }
        };

        // write xml
        writer.startDocument();
        writer.startElement("", "root", "root", new AttributesImpl());
        writer.endElement("", "root", "root");
        writer.endDocument();
    }

    /**
     * Test to avoid the circular method calling.
     */
    @Test(expected = StackOverflowError.class)
    public void overrideCharacters() throws Exception {
        XMLScanner writer = new XMLScanner() {

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                text("@");
            }
        };

        // write xml
        writer.startDocument();
        writer.startElement("", "root", "root", new AttributesImpl());
        writer.characters("test".toCharArray(), 0, 4);
        writer.endElement("", "root", "root");
        writer.endDocument();
    }
}
