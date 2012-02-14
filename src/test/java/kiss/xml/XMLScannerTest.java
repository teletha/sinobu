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

import static antibug.xml.XML.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

import kiss.I;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @version 2012/02/14 15:44:41
 */
public class XMLScannerTest {

    /**
     * <p>
     * Helper method to build input source.
     * </p>
     * 
     * @param text
     * @return
     */
    private InputSource source(String text) {
        return new InputSource(new StringReader(text));
    }

    @Test
    public void dontThrowNPE() throws IOException {
        I.parse(source("<text/>"), new XMLScanner());
    }

    @Test(expected = NullPointerException.class)
    public void requireFilter() throws IOException {
        I.parse(source("<text/>"), (XMLFilter) null);
    }

    @Test(expected = NullPointerException.class)
    public void requireXML() throws IOException {
        I.parse((InputSource) null, new XMLScanner());
    }

    @Test(expected = NullPointerException.class)
    public void requirePath() throws IOException {
        I.parse((Path) null, new XMLScanner());
    }

    @Test
    public void dontModify() throws Exception {
        String text = "<root/>";
        String expect = "<root/>";

        assert xml(text, new XMLScanner()).isIdenticalTo(expect);
    }

    @Test
    public void chainFilter() throws Exception {
        XMLFilter first = new XMLScanner();

        XMLFilter second = new XMLScanner();
        second.setParent(first);

        XMLFilter third = new XMLScanner();
        third.setParent(second);

        String text = "<root/>";
        String expect = "<root/>";

        assert xml(text, third).isIdenticalTo(expect);
    }

    @Test
    public void modifyByFilter() throws Exception {
        String text = "<root/>";
        String expect = "<top><root/></top>";

        assert xml(text, new Encloser("top")).isIdenticalTo(expect);
    }

    @Test
    public void modifyByChinedFilters() throws Exception {
        XMLFilter first = new Encloser("first");

        XMLFilter second = new Encloser("second");
        second.setParent(first);

        XMLFilter third = new Encloser("third");
        third.setParent(second);

        String text = "<root/>";
        String expect = "<third><second><first><root/></first></second></third>";

        assert xml(text, third).isIdenticalTo(expect);
    }

    @Test
    public void writeXML() throws Exception {
        XMLScanner writer = new XMLScanner();
        Document doc = build(writer);

        // write xml
        writer.startDocument();
        writer.start("root");
        writer.end();
        writer.endDocument();

        String expect = "<root/>";

        assert xml(doc).isIdenticalTo(expect);
    }

    @Test
    public void writeAttributeAndText() throws Exception {
        XMLScanner writer = new XMLScanner();
        Document doc = build(writer);

        // write xml
        writer.startDocument();
        writer.start("root", "name1", "value1", "name2", "value2");
        writer.text("text");
        writer.end();
        writer.endDocument();

        String expect = "<root name1='value1' name2='value2'>text</root>";

        assert xml(doc).isIdenticalTo(expect);
    }

    @Test
    public void writeNS() throws Exception {
        XMLScanner writer = new XMLScanner();
        Document doc = build(writer);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("", "default");
        writer.start("root");
        writer.end();
        writer.endPrefixMapping("");
        writer.endDocument();

        String expect = "<root xmlns='default'/>";

        assert xml(doc).isIdenticalTo(expect);
    }

    @Test
    public void writeNSElement() throws Exception {
        XMLScanner writer = new XMLScanner();
        Document doc = build(writer);

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

        String expect = "<test:root xmlns:test='first'><test:in xmlns:test='second'/>  <test:in/></test:root>";

        assert xml(doc).isIdenticalTo(expect);
    }

    @Test
    public void writeNSAttribute() throws Exception {
        XMLScanner writer = new XMLScanner();
        Document doc = build(writer);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("test", "first");
        writer.start("root", "test:name1", "value1");
        writer.text("text");
        writer.end();
        writer.endPrefixMapping("test");
        writer.endDocument();

        String expect = "<root xmlns:test='first' test:name1='value1'>text</root>";

        assert xml(doc).isIdenticalTo(expect);
    }

    @Test
    public void writeLoop() throws Exception {
        XMLScanner writer = new XMLScanner();
        Document doc = build(writer);

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

        String expect = "<root xmlns:ns='ns'><ns:m>0</ns:m><ns:m>1</ns:m><ns:m>2</ns:m></root>";

        assert xml(doc).isIdenticalTo(expect);
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
