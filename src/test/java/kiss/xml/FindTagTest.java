/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class FindTagTest extends FindTestBase {

    @Test
    public void tag() {
        XML xml = I.xml("""
                <root>
                    <article></article>
                    <article></article>
                    <h1></h1>
                    <h1></h1>
                    <lonely-child></lonely-child>
                    <div></div>
                </root>
                """);

        validateSelector(xml, 2, "article");
        validateSelector(xml, 2, "h1");
        validateSelector(xml, 1, "lonely-child");
        validateSelector(xml, 1, "div");
        validateSelector(xml, 0, "nonexistent");
        validateSelector(xml, 0, "root");
    }

    @Test
    public void universalSelector() {
        XML xml = I.xml("""
                <root>
                    <main>
                        <item1/>
                        <item2/>
                        <!-- comment -->
                        <item3/>
                        text
                    </main>
                    <other/>
                </root>
                """);
        // xml is <root>. xml.find("*") finds descendants: main, item1, item2, item3, other. (5
        // elements)
        validateSelector(xml, 5, "*");

        // Children of <root> are <main> and <other>. (2 elements)
        validateSelector(xml, 2, ">", "*");

        // Children of <main> are <item1>, <item2>, <item3>. (3 elements)
        validateSelector(xml, 3, "main", ">", "*");

        // Descendants of <main> are <item1>, <item2>, <item3>. (3 elements)
        validateSelector(xml, 3, "main", " ", "*");
    }

    @Test
    public void multipleTagsCommaSeparated() {
        XML xml = I.xml("""
                <m>
                    <E/>
                    <F/>
                    <e>
                        <G/>
                    </e>
                    <H/>
                </m>
                """);

        assert xml.find("E,F").size() == 2;
        assert xml.find("E, F").size() == 2; // Space after comma
        assert xml.find("E ,F").size() == 2; // Space before comma
        assert xml.find("E , F").size() == 2; // Space around comma

        // Test with validateSelector for the base case "E,F"
        validateSelector(xml, 2, "E", ",", "F");

        // Other combinations
        assert xml.find("E,G").size() == 2; // E (child of m), G (grandchild of m)
        assert xml.find(" E, G ").size() == 2; // With surrounding spaces
        assert xml.find("E, H, G").size() == 3;
        assert xml.find("e, G").size() == 2; // e (child of m), G (child of e)
    }

    @Test
    public void tagWithDot() {
        XML xml = I.xml("<m><E.E.E/></m>");
        validateSelector(xml, 1, "E\\.E\\.E"); // Dots need to be escaped for literal match
    }

    @Test
    public void tagWithHyphen() {
        XML xml = I.xml("<m><E-E/><E--E/></m>");
        validateSelector(xml, 1, "E-E");
        validateSelector(xml, 1, "E--E");
    }

    @Test
    public void tagWithEscapedHyphen() {
        XML xml = I.xml("<m><E-E/><E--E/></m>");
        // Standard CSS doesn't require escaping hyphens in type names unless ambiguous.
        // E-E is a valid type selector. E\-E should also select <E-E>.
        validateSelector(xml, 1, "E\\-E");
        validateSelector(xml, 1, "E\\-\\-E");
    }

    @Test
    public void tagCaseSensitivity() {
        XML xml = I.xml("<m><item/><Item/><ITEM/></m>");
        validateSelector(xml, 1, "item");
        validateSelector(xml, 1, "Item");
        validateSelector(xml, 1, "ITEM");
        validateSelector(xml, 0, "iTeM"); // No match due to case difference
    }

    @Test
    public void tagWithNumbers() {
        XML xml = I.xml("<m><h1/><el2/><e3e/></m>");
        validateSelector(xml, 1, "h1");
        validateSelector(xml, 1, "el2");
        validateSelector(xml, 1, "e3e");
    }

    @Test
    public void findOnEmptyXMLSet() {
        XML root = I.xml("<root/>"); // A root element exists

        XML emptyChildrenSet = root.children(); // This will be an empty XML set
        assert emptyChildrenSet.size() == 0;
        validateSelector(emptyChildrenSet, 0, "anyTag");
        validateSelector(emptyChildrenSet, 0, "*");

        XML nonExistentSet = root.find("nonexistent"); // This is also an empty XML set
        assert nonExistentSet.size() == 0;
        validateSelector(nonExistentSet, 0, "anyTag");
        validateSelector(nonExistentSet, 0, "*");
    }
}