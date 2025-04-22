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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xml.sax.SAXParseException;

import antibug.CleanRoom;
import kiss.I;
import kiss.XML;

public class ReadTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom(true);

    @Test
    public void elementName() {
        XML root = I.xml("test");

        assert root.size() == 1;
        assert root.name().equals("test");
    }

    @Test
    public void xmlLiteral() {
        XML root = I.xml("<test/>");

        assert root.size() == 1;
        assert root.name() == "test";
    }

    @Test
    public void htmlLiteral() {
        XML root = I.xml("<html/>");

        assert root.size() == 1;
        assert root.name() == "html";
    }

    @Test
    public void inputStream() {
        XML root = I.xml(new ByteArrayInputStream("<html/>".getBytes()));

        assert root.size() == 1;
        assert root.name() == "html";
    }

    @Test
    public void reader() {
        XML root = I.xml(new StringReader("<html/>"));

        assert root.size() == 1;
        assert root.name() == "html";
    }

    @Test
    public void path() {
        XML root = I.xml(room.locateFile("temp", "<html/>"));

        assert root.size() == 1;
        assert root.name() == "html";
    }

    @Test
    public void inputNull() {
        assertThrows(NullPointerException.class, () -> {
            I.xml((Path) null);
        });
    }

    @Test
    public void invalidLiteral() {
        assertThrows(SAXParseException.class, () -> {
            I.xml("<m><></m>");
        });
    }
}