/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.Test;

import antibug.AntiBug;

/**
 * @version 2012/11/18 2:56:12
 */
public class XMLParserTest {

    @Test
    public void html() throws Exception {
        XML xml = parse("<html><head></head><body></body></html>");

        assert xml.find("> *").size() == 2;
    }

    @Test
    public void xml() throws Exception {
        XML xml = I.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><item/></html>");

        assert xml.find("item").size() == 1;
    }

    @Test
    public void emptyElement() throws Exception {
        XML xml = parse("<html><item/></html>");

        assert xml.find("item").size() == 1;
    }

    @Test
    public void emptyWithoutSlash() throws Exception {
        XML xml = parse("<html><meta><meta></html>");

        assert xml.find("> meta").size() == 2;
    }

    @Test
    public void attribute() throws Exception {
        XML xml = parse("<html><item name=\"value\"/></html>");

        assert xml.find("item[name=value]").size() == 1;
    }

    @Test
    public void attributeMultiple() throws Exception {
        XML xml = parse("<html><item name=\"value\" content-type=\"some\"/></html>");

        assert xml.find("item[name=value][content-type=some]").size() == 1;
    }

    @Test
    public void attributeApostrophe() throws Exception {
        XML xml = parse("<html><item name='value'/></html>");

        assert xml.find("item[name=value]").size() == 1;
    }

    @Test
    public void attributeNaked() throws Exception {
        XML xml = parse("<html><item name=value/></html>");

        assert xml.find("item").attr("name").equals("value");
    }

    @Test
    public void attributeNakedURI() throws Exception {
        XML xml = parse("<html><item name=http://test.org/index.html /></html>");

        assert xml.find("item").attr("name").equalsIgnoreCase("http://test.org/index.html");
    }

    @Test
    public void attributeNakedMultiples() throws Exception {
        XML xml = parse("<html><item name=value one=other/></html>");

        XML item = xml.find("item");
        assert item.attr("name").equals("value");
        assert item.attr("one").equals("other");
    }

    @Test
    public void attributeNoValue() throws Exception {
        XML xml = parse("<html><item disabled/></html>");

        assert xml.find("item").attr("disabled").equals("disabled");
    }

    @Test
    public void attributeWithSpace() throws Exception {
        XML xml = parse("<html><item  name = 'value' /></html>");

        assert xml.find("item").attr("name").equals("value");
    }

    @Test
    public void comment() throws Exception {
        XML xml = parse("<html><!-- comment -><a/><!-- comment -></html>");

        assert xml.find("a").size() == 1;
    }

    @Test
    public void text() throws Exception {
        XML xml = parse("<html><p>text</p></html>");

        assert xml.find("p").text().equals("text");
    }

    @Test
    public void inline() throws Exception {
        XML xml = parse("<html><p>b<span>o</span>o<span>o</span>k</p></html>");

        assert xml.find("p").text().equals("boook");
        assert xml.find("span").size() == 2;
    }

    @Test
    public void script() throws Exception {
        XML xml = parse("<html><script>var test;</script></html>");

        assert xml.find("script").text().equals("var test;");
    }

    @Test
    public void scriptEscape() throws Exception {
        XML xml = parse("<html><script>var test = '<test/>';</script></html>");

        assert xml.find("script").text().equals("var test = '<test/>';");
        assert xml.find("test").size() == 0;
    }

    @Test
    public void upperCase() throws Exception {
        XML xml = parse("<html><SCRIPT></SCRIPT></html>");

        assert xml.find("script").size() == 1;
        assert xml.find("script").text().length() == 0;
    }

    @Test
    public void upperLower() throws Exception {
        XML xml = parse("<html><SCRIPT></script></html>");

        assert xml.find("script").size() == 1;
        assert xml.find("script").text().length() == 0;
    }

    @Test
    public void lowerUpper() throws Exception {
        XML xml = parse("<html><script></SCRIPT></html>");

        assert xml.find("script").size() == 1;
        assert xml.find("script").text().length() == 0;
    }

    @Test
    public void processingInstruction() throws Exception {
        XML xml = parse("<?xml-stylesheet type=\"text/xsl\" href=\"test.xsl\"?><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    public void doctype() throws Exception {
        XML xml = parse("<!DOCTYPE html><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithWhitespace() throws Exception {
        XML xml = parse("<!DOCTYPE html>\r\n<html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithPublic() throws Exception {
        XML xml = parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    public void doctypeWithPublicAndSystem() throws Exception {
        XML xml = parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head/></html>");

        assert xml.find("head").size() == 1;
        assert xml.parent().text().length() == 0;
    }

    @Test
    public void whitespace() throws Exception {
        XML xml = parse("   <html>\r\n\t<in/> \n\n</html>   ");

        assert xml.find("in").size() == 1;
    }

    @Test
    public void encoding() throws Exception {
        XML xml = parse("<html><head><meta charset='euc-jp'><title>てすと</title></head></html>", "euc-jp");

        assert xml.find("title").text().equals("てすと");
    }

    @Test
    public void encodingMultiple() throws Exception {
        XML xml = parse("<html><head><meta no/><meta charset='euc-jp'/><meta charset='shift_jis'><title>てすと</title></head></html>", "euc-jp");

        assert xml.find("title").text().equals("てすと");
    }

    @Test
    public void encodingInvalid() throws Exception {
        XML xml = parse("<html><head><meta charset='uft-3'><title>てすと</title></head></html>", "utf-8");

        assert xml.find("title").text().equals("てすと");
    }

    @Test
    public void invalidSlashPosition() throws Exception {
        XML xml = parse("<html><img height='0' / width='64'></html>");

        assert xml.find("img").attr("height").equals("0");
    }

    @Test
    public void invalidQuotePosition() throws Exception {
        XML xml = parse("<html><img alt=\"value\"\"></html>");

        assert xml.find("img").attr("alt").equals("value");
    }

    @Test
    public void invalidSingleQuotePosition() throws Exception {
        XML xml = parse("<html><img alt='value''></html>");

        assert xml.find("img").attr("alt").equals("value");
    }

    @Test
    public void invalidAttribute() throws Exception {
        XML xml = parse("<html><img alt=\"value\"(0)\"></html>");

        assert xml.find("img").attr("alt").equals("value");
    }

    @Test
    public void illegal() throws Exception {
        XML xml = parse("<html><Q/><Q/><Q><p/><Q><p/></html>");

        assert xml.children().size() == 3;
    }

    /**
     * <p>
     * Parse as HTML.
     * </p>
     */
    private XML parse(String html) {
        try {
            XMLUtil parser = new XMLUtil(Files.newInputStream(AntiBug.note(html)));
            return parser.parse(I.$encoding);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Parse as HTML.
     * </p>
     */
    private XML parse(String html, String encoding) {
        try {
            ByteBuffer buffer = Charset.forName(encoding).encode(html);
            XMLUtil parser = new XMLUtil(new ByteArrayInputStream(buffer.array(), 0, buffer.limit()));
            return parser.parse(I.$encoding);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
