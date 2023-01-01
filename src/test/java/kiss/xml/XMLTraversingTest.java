/*
 * Copyright (C) 2023 The SINOBU Development Team
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

class XMLTraversingTest {

    @Test
    void first() {
        String xml = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(xml).find("Q").first().attr("class").equals("first");
    }

    @Test
    void firstAtEmpty() {
        XML xml = I.xml("<m/>");

        assert xml.find("Q").size() == 0;
        assert xml.find("Q").first().size() == 0;
    }

    @Test
    void last() {
        String xml = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(xml).find("Q").last().attr("class").equals("last");
    }

    @Test
    void lastAtEmpty() {
        XML xml = I.xml("<m/>");

        assert xml.find("Q").size() == 0;
        assert xml.find("Q").last().size() == 0;
    }

    @Test
    void parent() {
        // traverse to parent element
        XML root = I.xml("<root><first/><center/><last/></root>");
        assert root.find("first").parent().name() == "root";
        assert root.find("center").parent().name() == "root";
        assert root.find("last").parent().name() == "root";

        // traverse to parent element from nested element
        root = I.xml("<root><child><grand/></child></root>");
        assert root.find("grand").parent().name() == "child";
    }

    @Test
    void children() {
        // traverse to child elements
        XML root = I.xml("<root><first/><center/><last/></root>");
        assert root.children().size() == 3;

        // skip text node
        root = I.xml("<root>text<first/>is<child><center/></child>ignored<last/>!!</root>");
        assert root.children().size() == 3;

        // can't traverse
        root = I.xml("<root/>");
        assert root.children().size() == 0;
    }

    @Test
    void firstChild() {
        // traverse to first child element
        XML root = I.xml("<root><first/><center/><last/></root>");
        assert root.firstChild().name() == "first";

        // skip text node
        root = I.xml("<root>text is ignored<first/><center/><last/></root>");
        assert root.firstChild().name() == "first";

        // can't traverse
        root = I.xml("<root/>");
        assert root.firstChild().size() == 0;
    }

    @Test
    void lastChild() {
        // traverse to last child element
        XML root = I.xml("<root><first/><center/><last/></root>");
        assert root.lastChild().name() == "last";

        // skip text node
        root = I.xml("<root><first/><center/><last/>text is ignored</root>");
        assert root.lastChild().name() == "last";

        // can't traverse
        root = I.xml("<root/>");
        assert root.lastChild().size() == 0;
    }

    @Test
    void prev() {
        XML root = I.xml("<root><first/>text is ignored<center/><last/></root>");

        // traverse to previous element
        XML next = root.find("last").prev();
        assert next.name() == "center";

        // skip previous text node
        next = root.find("center").prev();
        assert next.name() == "first";

        // can't traverse
        next = root.find("first").prev();
        assert next.size() == 0;
    }

    @Test
    void next() {
        XML root = I.xml("<root><first/><center/>text is ignored<last/></root>");

        // traverse to next element
        XML next = root.find("first").next();
        assert next.name() == "center";

        // skip next text node
        next = root.find("center").next();
        assert next.name() == "last";

        // can't traverse
        next = root.find("last").next();
        assert next.size() == 0;
    }
}