/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xml.sax.SAXParseException;

import antibug.CleanRoom;
import kiss.I;
import kiss.XML;

/**
 * @version 2018/09/28 13:35:29
 */
public class ReadTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void elementName() {
        XML xml = I.xml("test");
        assert xml.size() == 1;
        assert xml.name().equals("test");
    }

    @Test
    void xmlLiteral() {
        XML xml = I.xml("<test/>");
        assert xml.size() == 1;
        assert xml.name() == "test";
    }

    @Test
    void htmlLiteral() {
        XML xml = I.xml("<html/>");
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void inputStream() {
        XML xml = I.xml(new ByteArrayInputStream("<html/>".getBytes()));
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void reader() {
        XML xml = I.xml(new StringReader("<html/>"));
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void file() {
        XML xml = I.xml(room.locateFile("temp", "<html/>").toFile());
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void url() throws MalformedURLException {
        XML xml = I.xml(room.locateFile("temp", "<html/>").toUri().toURL());
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void uri() {
        XML xml = I.xml(room.locateFile("temp", "<html/>").toUri());
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void inputNull() {
        assertThrows(NullPointerException.class, () -> {
            I.xml((File) null);
        });
    }

    @Test
    void invalidLiteral() {
        assertThrows(SAXParseException.class, () -> {
            I.xml("<m><></m>");
        });
    }
}