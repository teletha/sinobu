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

class XMLWriterTest {

    static final String EOL = "\r\n";

    @RegisterExtension
    CleanRoom room = new CleanRoom(true);

    @Test
    void write() throws Exception {
        Path file = room.locateFile("test.xml");
        I.xml("root").to(Files.newBufferedWriter(file), "");

        assert Files.exists(file);
        assert Files.size(file) != 0;
    }

    @Test
    void format() {
        String expected = "" + //
                "<root>" + EOL + //
                "\t<child/>" + EOL + //
                "</root>";

        assert I.xml("<root><child/></root>").toString().equals(expected);
    }

    @Test
    void format2() {
        checkXMLFormat("""
                <root>
                    <child/>
                    <child/>
                </root>
                """);
    }

    private boolean checkXMLFormat(String xml) {
        // normalize line feed and leading tab
        String[] lines = xml.trim().split("\n");
        for (int i = 0; i < lines.length; i++) {
            int count = countLeadingSpaces(lines[i]);
            lines[i] = "\t".repeat(count / 4).concat(lines[i].trim());
        }
        xml = String.join(System.lineSeparator(), lines);

        // validate
        String formatted = I.xml(xml).toString().trim();
        assert formatted.equals(xml);

        return true;
    }

    private static int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    @Test
    void nested() {
        String expected = "" + //
                "<root>" + EOL + //
                "\t<child>" + EOL + //
                "\t\t<nested/>" + EOL + //
                "\t</child>" + EOL + //
                "</root>";

        assert I.xml("<root><child><nested/></child></root>").toString().equals(expected);
    }

    @Test
    void attribute() {
        String expected = "<root name=\"value\"/>";

        assert I.xml("<root name='value'/>").toString().equals(expected);
    }

    @Test
    void text() {
        String expected = "<root>text テキスト</root>";

        assert I.xml("<root>text テキスト</root>").toString().equals(expected);
    }

    @Test
    void unsafeText1() {
        String expected = "<root attr=\"&#34;\">&#60;&#38;&#34;&#39;&#62;</root>";

        assert I.xml("<root attr='&quot;'>&lt;&amp;&quot;&apos;&gt;</root>").toString().equals(expected);
    }

    @Test
    void unsafeText2() {
        String expected = "<root attr=\"&#38;\">&#38;</root>";

        assert I.xml("<root attr='&amp;'>&amp;</root>").toString().equals(expected);
    }

    @Test
    void preserveWhitespace() {
        String expected = "<root> text\t</root>";

        assert I.xml("<root> text\t</root>").toString().equals(expected);
    }

    @Test
    void inlineElement() {
        String expected = "<root><child/></root>";

        StringBuilder output = new StringBuilder();
        I.xml("<root><child/></root>").to(output, "\t", "child");
        assert output.toString().equals(expected);
    }

    @Test
    void nonEmpty() {
        String expected = "<root></root>";

        StringBuilder output = new StringBuilder();
        I.xml("<root/>").to(output, "\t", "&root");
        assert output.toString().equals(expected);
    }

    @Test
    void noIndent() {
        String expected = "<root><child/></root>";

        StringBuilder output = new StringBuilder();
        I.xml("<root><child/></root>").to(output, null);
        assert output.toString().equals(expected);
    }
}