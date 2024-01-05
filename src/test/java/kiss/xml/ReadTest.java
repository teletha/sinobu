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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xml.sax.SAXParseException;

import antibug.CleanRoom;
import kiss.I;
import kiss.XML;

class ReadTest {

    @RegisterExtension
    static CleanRoom room = new CleanRoom();

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
    void path() {
        XML xml = I.xml(room.locateFile("temp", "<html/>"));
        assert xml.size() == 1;
        assert xml.name() == "html";
    }

    @Test
    void inputNull() {
        assertThrows(NullPointerException.class, () -> {
            I.xml((Path) null);
        });
    }

    @Test
    void invalidLiteral() {
        assertThrows(SAXParseException.class, () -> {
            I.xml("<m><></m>");
        });
    }
}