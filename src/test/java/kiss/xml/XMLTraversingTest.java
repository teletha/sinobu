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

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class XMLTraversingTest {

    @Test
    public void first() {
        String text = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(text).find("Q").first().attr("class").equals("first");
    }

    @Test
    public void firstAtEmpty() {
        XML root = I.xml("<m/>");

        assert root.find("Q").size() == 0;
        assert root.find("Q").first().size() == 0;
    }

    @Test
    public void last() {
        String text = "<m><Q class='first'/><Q/><Q class='last'/></m>";

        assert I.xml(text).find("Q").last().attr("class").equals("last");
    }

    @Test
    public void lastAtEmpty() {
        XML root = I.xml("<m/>");

        assert root.find("Q").size() == 0;
        assert root.find("Q").last().size() == 0;
    }

    @Test
    public void parent() {
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
    public void children() {
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
    public void firstChild() {
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
    public void lastChild() {
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
    public void prev() {
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
    public void next() {
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