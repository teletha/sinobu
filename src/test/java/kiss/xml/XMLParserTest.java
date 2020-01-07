/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

/**
 * @version 2017/03/31 18:57:46
 */
class XMLParserTest {

    @Test
    void html() {
        XML xml = I.xml("<html><head></head><body></body></html>");

        assert xml.find("> *").size() == 2;
    }

    @Test
    void htmlWithDoctype() {
        XML xml = I.xml("<!DOCTYPE html><html><body/></html>");

        assert xml.find("body").size() == 1;
    }

    @Test
    void xml() {
        XML xml = I.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><item/></html>");

        assert xml.find("item").size() == 1;
    }

    @Test
    void emptyElement() {
        XML xml = I.xml("<html><item/></html>");

        assert xml.find("item").size() == 1;
    }

    @Test
    void emptyWithoutSlash() {
        XML xml = I.xml("<html><meta><meta></html>");

        assert xml.find("> meta").size() == 2;
    }

    @Test
    void attribute() {
        XML xml = I.xml("<html><item name=\"value\"/></html>");

        assert xml.find("item[name=value]").size() == 1;
    }

    @Test
    void attributeMultiple() {
        XML xml = I.xml("<html><item name=\"value\" content-type=\"some\"/></html>");

        assert xml.find("item[name=value][content-type=some]").size() == 1;
    }

    @Test
    void attributeApostrophe() {
        XML xml = I.xml("<html><item name='value'/></html>");

        assert xml.find("item[name=value]").size() == 1;
    }

    @Test
    void attributeNaked() {
        XML xml = I.xml("<html><item name=value/></html>");

        assert xml.find("item").attr("name").equals("value");
    }

    @Test
    void attributeNakedURI() {
        XML xml = I.xml("<html><item name=http://test.org/index.html /></html>");

        assert xml.find("item").attr("name").equalsIgnoreCase("http://test.org/index.html");
    }

    @Test
    void attributeNakedMultiples() {
        XML xml = I.xml("<html><item name=value one=other/></html>");

        XML item = xml.find("item");
        assert item.attr("name").equals("value");
        assert item.attr("one").equals("other");
    }

    @Test
    void attributeNoValue() {
        XML xml = I.xml("<html><item disabled/></html>");

        assert xml.find("item").attr("disabled").equals("disabled");
    }

    @Test
    void attributeWithSpace() {
        XML xml = I.xml("<html><item  name = 'value' /></html>");

        assert xml.find("item").attr("name").equals("value");
    }

    @Test
    void comment() {
        XML xml = I.xml("<html><!-- comment -><a/><!-- comment -></html>");

        assert xml.find("a").size() == 1;
    }

    @Test
    void text() {
        XML xml = I.xml("<html><p>text</p></html>");

        assert xml.find("p").text().equals("text");
    }

    @Test
    void reserveWhitespace() {
        XML xml = I.xml("<html> remaining <em>all</em> whitespaces </html>");

        assert xml.text().equals(" remaining all whitespaces ");
    }

    @Test
    void inline() {
        XML xml = I.xml("<html><p>b<span>o</span>o<span>o</span>k</p></html>");

        assert xml.find("p").text().equals("boook");
        assert xml.find("span").size() == 2;
    }

    @Test
    void script() {
        XML xml = I.xml("<html><script>var test;</script></html>");

        assert xml.find("script").text().equals("var test;");
    }

    @Test
    void scriptEscape() {
        XML xml = I.xml("<html><script>var test = '<test/>';</script></html>");

        assert xml.find("script").text().equals("var test = '<test/>';");
        assert xml.find("test").size() == 0;
    }

    @Test
    void upperCase() {
        XML xml = I.xml("<html><SCRIPT></SCRIPT></html>");

        assert xml.find("script").size() == 1;
        assert xml.find("script").text().length() == 0;
    }

    @Test
    void processingInstruction() {
        XML xml = I.xml("<?xml-stylesheet type=\"text/xsl\" href=\"test.xsl\"?><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void doctype() {
        XML xml = I.xml("<!DOCTYPE html><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void doctypeWithWhitespace() {
        XML xml = I.xml("<!DOCTYPE html>\r\n<html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void doctypeWithComment() {
        XML xml = I.xml("<!DOCTYPE html><!-- comment --><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void doctypeWithCommentAndWhitespace() {
        XML xml = I.xml("<!DOCTYPE html> <!-- comment --> <html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void doctypeWith() {
        XML xml = I.xml("<!DOCTYPE html  \"-//W3C//DTD XHTML 1.0 Transitional//EN\"><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void doctypeWithAndSystem() {
        XML xml = I
                .xml("<!DOCTYPE html  \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    void whitespace() {
        XML xml = I.xml("   <xml>   </xml>   ");
        assert xml.name().equals("xml");
    }

    @Test
    void lineFeed() {
        XML xml = I.xml("\r<xml>\r\n</xml>\n");
        assert xml.name().equals("xml");
    }

    @Test
    void tab() {
        XML xml = I.xml("\t<xml>\t</xml>\t");
        assert xml.name().equals("xml");
    }

    @Test
    void invalidSlashPosition() {
        XML xml = I.xml("<html><img height='0' / width='64'></html>");

        assert xml.find("img").attr("height").equals("0");
    }

    @Test
    void invalidQuotePosition() {
        XML xml = I.xml("<html><img alt=\"value\"\"></html>");

        assert xml.find("img").attr("alt").equals("value");
    }

    @Test
    void invalidSingleQuotePosition() {
        XML xml = I.xml("<html><img alt='value''></html>");

        assert xml.find("img").attr("alt").equals("value");
    }

    @Test
    void invalidAttribute() {
        XML xml = I.xml("<html><img alt=\"value\"(0)\"></html>");

        assert xml.find("img").attr("alt").equals("value");
    }

    @Test
    void illegal() {
        XML xml = I.xml("<html><Q/><Q/><Q><p/><Q><p/></html>");

        assert xml.children().size() == 3;
    }
}
