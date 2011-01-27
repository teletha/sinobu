/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.xml;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;


import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ezbean.xml.XMLScanner;

/**
 * DOCUMENT.
 * 
 * @version 2007/10/19 6:16:48
 */
public class SAXBuilderTest {

    /**
     * Test method for {@link ezbean.xml2.SAXBuilder#getDocument()}.
     */
    @Test
    public void testGetDocument1() throws SAXException {
        SAXBuilder builder = new SAXBuilder();

        // write xml
        builder.startDocument();
        builder.startElement("", "test", "test", EMPTY_ATTR);
        builder.endElement("", "test", "test");
        builder.endDocument();

        Document document = builder.getDocument();
        assertNotNull(document);
        assertEquals("test", document.getDocumentElement().getLocalName());
        assertEquals(null, document.getDocumentElement().getNamespaceURI());
        assertEquals(null, document.getDocumentElement().getPrefix());
    }

    /**
     * Test method for {@link ezbean.xml2.SAXBuilder#getDocument()}.
     */
    @Test
    public void testGetDocument2() throws SAXException {
        SAXBuilder builder = new SAXBuilder();

        // write xml
        builder.startDocument();
        builder.startPrefixMapping("o", "s");
        builder.startElement("s", "test", "o:test", EMPTY_ATTR);
        builder.endElement("s", "test", "o:test");
        builder.endDocument();

        Document document = builder.getDocument();
        assertNotNull(document);
        assertEquals("test", document.getDocumentElement().getLocalName());
        assertEquals("s", document.getDocumentElement().getNamespaceURI());
        assertEquals("o", document.getDocumentElement().getPrefix());
    }

    /**
     * Test method for {@link ezbean.xml2.SAXBuilder#getDocument()}.
     */
    @Test
    public void testGetDocument3() throws SAXException {
        SAXBuilder builder = new SAXBuilder();
        XMLScanner writer = new XMLScanner();
        writer.setContentHandler(builder);

        // write xml
        writer.startDocument();
        writer.startPrefixMapping("o", "s");
        writer.start("o:test");
        writer.start("o:child");
        writer.end();
        writer.startPrefixMapping("o", "change");
        writer.start("o:child");
        writer.end();
        writer.endPrefixMapping("o");
        writer.start("o:child");
        writer.end();
        writer.end();
        writer.endPrefixMapping("o");
        writer.endDocument();

        // assertion
        Document document = builder.getDocument();
        assertNotNull(document);

        Element root = document.getDocumentElement();
        assertEquals("test", root.getLocalName());
        assertEquals("s", root.getNamespaceURI());
        assertEquals("o", root.getPrefix());

        Element child = (Element) root.getFirstChild();
        assertEquals("child", child.getLocalName());
        assertEquals("s", child.getNamespaceURI());
        assertEquals("o", child.getPrefix());

        child = (Element) child.getNextSibling();
        assertEquals("child", child.getLocalName());
        assertEquals("change", child.getNamespaceURI());
        assertEquals("o", child.getPrefix());

        child = (Element) child.getNextSibling();
        assertEquals("child", child.getLocalName());
        assertEquals("s", child.getNamespaceURI());
        assertEquals("o", child.getPrefix());
    }

    /**
     * Test method for {@link ezbean.xml2.SAXBuilder#getDocument()}.
     */
    @Test
    public void testInvalidGetDocument1() throws SAXException {
        SAXBuilder builder = new SAXBuilder();

        Document document = builder.getDocument();
        assertNull(document);
    }
}
