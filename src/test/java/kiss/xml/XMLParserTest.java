/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import kiss.I;
import kiss.XML;

public class XMLParserTest {

    @Test
    public void html() {
        XML root = I.xml("""
                <html>
                    <head/>
                    <body/>
                </html>
                """);

        assert root.find("*").size() == 2;
    }

    @Test
    public void htmlWithHeadSpaces() {
        XML root = I.xml(" \r\n\t<html><head></head><body></body></html>");

        assert root.find("*").size() == 2;
    }

    @Test
    public void htmlWithTailSapces() {
        XML root = I.xml("<html><head></head><body></body></html> \r\n\t");

        assert root.find("*").size() == 2;
    }

    @Test
    public void htmlWithDoctype() {
        XML root = I.xml("<!DOCTYPE html><html><body/></html>");

        assert root.find("body").size() == 1;
    }

    @Test
    public void htmlWithDoctypeWithHeadSpaces() {
        XML root = I.xml(" \r\n\t<!DOCTYPE html><html><body/></html>");

        assert root.find("body").size() == 1;
    }

    @Test
    public void htmlWithDoctypeWithTailSpaces() {
        XML root = I.xml("<!DOCTYPE html><html><body/></html> \r\n\t");

        assert root.find("body").size() == 1;
    }

    @Test
    public void xml() {
        XML root = I.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><item/></html>");

        assert root.find("item").size() == 1;
    }

    @Test
    public void emptyElement() {
        XML root = I.xml("<html><item/></html>");

        assert root.find("item").size() == 1;
    }

    @Test
    public void emptyWithoutSlash() {
        XML root = I.xml("<html><meta><meta></html>");

        assert root.find("> meta").size() == 2;
    }

    @Test
    public void attribute() {
        XML root = I.xml("<html><item name=\"value\"/></html>");

        assert root.find("item[name=value]").size() == 1;
    }

    @Test
    public void attributeMultiple() {
        XML root = I.xml("<html><item name=\"value\" content-type=\"some\"/></html>");

        assert root.find("item[name=value][content-type=some]").size() == 1;
    }

    @Test
    public void attributeApostrophe() {
        XML root = I.xml("<html><item name='value'/></html>");

        assert root.find("item[name=value]").size() == 1;
    }

    @Test
    public void attributeNaked() {
        XML root = I.xml("<html><item name=value/></html>");

        assert root.find("item").attr("name").equals("value");
    }

    @Test
    public void attributeNakedURI() {
        XML root = I.xml("<html><item name=http://test.org/index.html /></html>");

        assert root.find("item").attr("name").equalsIgnoreCase("http://test.org/index.html");
    }

    @Test
    public void attributeNakedMultiples() {
        XML root = I.xml("<html><item name=value one=other/></html>");

        XML item = root.find("item");
        assert item.attr("name").equals("value");
        assert item.attr("one").equals("other");
    }

    @Test
    public void attributeNoValue() {
        XML root = I.xml("<html><item disabled/></html>");

        assert root.find("item").attr("disabled").equals("disabled");
    }

    @Test
    public void attributeWithSpace() {
        XML root = I.xml("<html><item  name = 'value' /></html>");

        assert root.find("item").attr("name").equals("value");
    }

    @Test
    public void comment() {
        XML root = I.xml("<html><!-- comment -><a/><!-- comment -></html>");

        assert root.find("a").size() == 1;
    }

    @Test
    public void text() {
        XML root = I.xml("""
                <html>
                    <body>
                        <p>text</p>
                    </body>
                </html>
                """);

        assert root.find("p").text().equals("text");
    }

    @Test
    public void reserveWhitespace() {
        XML root = I.xml("<html> remaining <em>all</em> whitespaces </html>");

        assert root.text().equals(" remaining all whitespaces ");
    }

    @Test
    public void inline() {
        XML root = I.xml("<html><p>b<span>o</span>o<span>o</span>k</p></html>");

        assert root.find("p").text().equals("boook");
        assert root.find("span").size() == 2;
    }

    @Test
    public void script() {
        XML root = I.xml("<html><script>var test;</script></html>");

        assert root.find("script").text().equals("var test;");
    }

    @Test
    public void scriptEscape() {
        XML root = I.xml("<html><script>var test = '<test/>';</script></html>");

        assert root.find("script").text().equals("var test = '<test/>';");
        assert root.find("test").size() == 0;
    }

    @Test
    public void upperCase() {
        XML root = I.xml("<html><SCRIPT></SCRIPT></html>");

        assert root.find("script").size() == 1;
        assert root.find("script").text().length() == 0;
    }

    @Test
    public void processingInstruction() {
        XML root = I.xml("<?xml-stylesheet type=\"text/xsl\" href=\"test.xsl\"?><html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void doctype() {
        XML root = I.xml("<!DOCTYPE html><html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithWhitespace() {
        XML root = I.xml("<!DOCTYPE html>\r\n<html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithComment() {
        XML root = I.xml("<!DOCTYPE html><!-- comment --><html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithCommentAndWhitespace() {
        XML root = I.xml("<!DOCTYPE html> <!-- comment --> <html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void doctypeWith() {
        XML root = I.xml("<!DOCTYPE html  \"-//W3C//DTD XHTML 1.0 Transitional//EN\"><html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithAndSystem() {
        XML root = I
                .xml("<!DOCTYPE html  \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head/></html>");

        assert root.find("head").size() == 1;
        assert root.parent().text().length() == 0;
    }

    @Test
    public void whitespace() {
        XML root = I.xml("   <xml>   </xml>   ");
        assert root.name().equals("xml");
    }

    @Test
    public void lineFeed() {
        XML root = I.xml("\r<xml>\r\n</xml>\n");
        assert root.name().equals("xml");
    }

    @Test
    public void tab() {
        XML root = I.xml("\t<xml>\t</xml>\t");
        assert root.name().equals("xml");
    }

    @Test
    public void invalidSlashPosition() {
        XML root = I.xml("<html><img height='0' / width='64'></html>");

        assert root.find("img").attr("height").equals("0");
    }

    @Test
    public void invalidQuotePosition() {
        XML root = I.xml("<html><img alt=\"value\"\"></html>");

        assert root.find("img").attr("alt").equals("value");
    }

    @Test
    public void invalidSingleQuotePosition() {
        XML root = I.xml("<html><img alt='value''></html>");

        assert root.find("img").attr("alt").equals("value");
    }

    @Test
    public void invalidAttribute() {
        XML root = I.xml("<html><img alt=\"value\"(0)\"></html>");

        assert root.find("img").attr("alt").equals("value");
    }

    @Test
    public void illegal() {
        XML root = I.xml("<html><Q/><Q/><Q><p/><Q><p/></html>");

        assert root.children().size() == 3;
    }

    @Test
    public void variousNullInputs() {
        assertThrows(NullPointerException.class, () -> I.xml((String) null));
        assertThrows(NullPointerException.class, () -> I.xml((Path) null));
        assertThrows(NullPointerException.class, () -> I.xml((InputStream) null));
        assertThrows(NullPointerException.class, () -> I.xml((Reader) null));
        assertThrows(NullPointerException.class, () -> I.xml((Node) null));
    }

    @Test
    public void empty() {
        assertThrows(DOMException.class, () -> I.xml(""));
    }

    @Test
    public void whitespaceOnly() {
        assertThrows(DOMException.class, () -> I.xml("  \t\r\n  "));
    }

    @Test
    public void parserCreateElementNS() {
        Node e = I.xml("<Q/>").to();
        assert e.getLocalName().equals("Q");
        assert e.getNodeName().equals("Q");
    }
}