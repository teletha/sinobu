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

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;
import kiss.XML;

public class XMLWriterTest {

    static final String EOL = "\r\n";

    @RegisterExtension
    CleanRoom room = new CleanRoom(true);

    @Test
    public void write() throws Exception {
        Path file = room.locateFile("test.xml");
        I.xml("root").to(Files.newBufferedWriter(file), "");

        assert Files.exists(file);
        assert Files.size(file) != 0;
    }

    @Test
    public void format() {
        XML root = I.xml("<root><child/><child/></root>");

        assert root.toString().equals(normalize("""
                <root>
                    <child/>
                    <child/>
                </root>
                """));
    }

    @Test
    public void nested() {
        XML root = I.xml("<root><child><nested/></child></root>");

        assert root.toString().equals(normalize("""
                <root>
                    <child>
                        <nested/>
                    </child>
                </root>
                """));
    }

    @Test
    public void specialIndent() {
        StringBuilder out = new StringBuilder();
        I.xml("<root><child><nested/></child></root>").to(out, "*");

        assert out.toString().equals(normalize("""
                <root>
                *<child>
                **<nested/>
                *</child>
                </root>
                """));
    }

    private static String normalize(String expected) {
        // normalize line feed and leading tab
        String[] lines = expected.trim().split("\n");
        for (int i = 0; i < lines.length; i++) {
            int count = countLeadingSpaces(lines[i]);
            lines[i] = "\t".repeat(count / 4).concat(lines[i].trim());
        }
        return String.join("\r\n", lines);
    }

    private static int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    @Test
    public void attribute() {
        String expected = "<root name=\"value\"/>";

        assert I.xml("<root name='value'/>").toString().equals(expected);
    }

    @Test
    public void text() {
        String expected = "<root>text テキスト</root>";

        assert I.xml("<root>text テキスト</root>").toString().equals(expected);
    }

    @Test
    public void unsafeText1() {
        String expected = "<root attr=\"&quot;\">&lt;&amp;&quot;&apos;&gt;</root>";

        assert I.xml("<root attr='&quot;'>&lt;&amp;&quot;&apos;&gt;</root>").toString().equals(expected);
    }

    @Test
    public void unsafeText2() {
        String expected = "<root attr=\"&amp;\">&amp;</root>";

        assert I.xml("<root attr='&amp;'>&amp;</root>").toString().equals(expected);
    }

    @Test
    public void preserveWhitespace() {
        String expected = "<root> text\t</root>";

        assert I.xml("<root> text\t</root>").toString().equals(expected);
    }

    @Test
    public void inlineElement() {
        StringBuilder out = new StringBuilder();
        I.xml("<root><inline/></root>").to(out, "\t", "inline");

        assert out.toString().equals("<root><inline/></root>");
    }

    @Test
    public void nonEmptyElement() {
        StringBuilder out = new StringBuilder();
        I.xml("<root/>").to(out, "\t", "&root");

        assert out.toString().equals("<root></root>");
    }

    @Test
    public void withoutFormat() {
        StringBuilder out = new StringBuilder();
        I.xml("<root><child/></root>").to(out, null);

        assert out.toString().equals("<root><child/></root>");
    }
}