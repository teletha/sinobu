/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Path;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import antibug.AntiBug;
import kiss.I;
import kiss.XML;

/**
 * @version 2017/03/30 16:50:34
 */
public class ReadTest {

    @Test
    public void elementName() throws Exception {
        XML xml = I.xml("test");
        assert xml.size() == 1;
        assert xml.name().equals("test");
    }

    @Test
    public void xmlLiteral() throws Exception {
        XML xml = I.xml("<test/>");
        assert xml.size() == 1;
        assert xml.name() == "test";
    }

    @Test
    public void htmlLiteral() throws Exception {
        XML xml = I.xml("<html/>");
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    public void inputStream() throws Exception {
        XML xml = I.xml(new ByteArrayInputStream("<html/>".getBytes()));
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    public void reader() throws Exception {
        XML xml = I.xml(new StringReader("<html/>"));
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    public void file() throws Exception {
        Path memo = AntiBug.memo("<html/>");
        XML xml = I.xml(memo.toFile());
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    public void url() throws Exception {
        Path memo = AntiBug.memo("<html/>");
        XML xml = I.xml(memo.toUri().toURL());
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    public void uri() throws Exception {
        Path memo = AntiBug.memo("<html/>");
        XML xml = I.xml(memo.toUri());
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test(expected = NullPointerException.class)
    public void inputNull() throws Exception {
        I.xml((File) null);
    }

    @Test(expected = SAXParseException.class)
    public void invalidLiteral() throws Exception {
        I.xml("<m><></m>");
    }
}
