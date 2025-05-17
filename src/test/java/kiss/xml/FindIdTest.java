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

import static kiss.xml.FindAssetion.*;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class FindIdTest {

    @Test
    public void id() {
        XML xml = I.xml("""
                <root>
                    <div id="best-content"></div>
                    <span id="title-element"></span>
                    <a id="link-item"></a>
                    <p id="another"></p>
                    <p id=""></p> <!-- Empty ID attribute -->
                    <p no-id-attr="true"></p> <!-- No ID attribute -->
                </root>
                """);
        assert select(xml, 1, "#best-content");
        assert select(xml, 1, "#title-element");
        assert select(xml, 1, "#link-item");
        assert select(xml, 1, "#another");
        assert select(xml, 0, "#nonexistent");
    }

    @Test
    public void idWithHyphen() {
        XML xml = I.xml("""
                <m>
                    <e id='A-A'/>
                    <e id='A--A'/>
                    <e id='--C'/>
                    <e id='-D'/>
                </m>
                """);
        assert select(xml, 1, "#A-A");
        assert select(xml, 1, "#A--A");
        assert select(xml, 1, "#--C");
        assert select(xml, 1, "#-D");
    }

    @Test
    public void idWithEscapedHyphenInSelector() {
        XML xml = I.xml("<m><e id='A-A'/><e id='A--A'/></m>");

        assert select(xml, 1, "#A\\-A");
        assert select(xml, 1, "#A\\-\\-A");
    }

    @Test
    public void idCombinedWithTag() {
        XML xml = I.xml("""
                <root>
                    <div id="elem1">Element 1</div>
                    <span id="elem1">This should not happen (duplicate ID) but test selector</span>
                    <div id="elem2">Element 2</div>
                </root>
                """);

        assert select(xml, 1, "div#elem1");
        assert select(xml, 1, "span#elem1");
        assert select(xml, 1, "div#elem2");
        assert select(xml, 0, "p#elem1"); // No p with this ID
        assert select(xml, 2, "*#elem1"); // Any element with ID elem1
    }

    @Test
    public void idCaseSensitivity() {
        XML xml = I.xml("""
                <root>
                    <div id="myId"></div>
                    <div id="myid"></div>
                    <div id="MYID"></div>
                </root>
                """);
        // ID attributes in XML are case-sensitive. CSS ID selectors are also case-sensitive.
        assert select(xml, 1, "#myId");
        assert select(xml, 1, "#myid");
        assert select(xml, 1, "#MYID");
        assert select(xml, 0, "#MyId"); // No match
    }

    @Test
    public void idWithNumbers() {
        XML xml = I.xml("""
                <root>
                    <div id="item1"></div>
                    <div id="item-2"></div>
                    <div id="i4d"></div>
                    <div id="4id"></div> <!-- ID starting with a digit -->
                </root>
                """);
        assert select(xml, 1, "#item1");
        assert select(xml, 1, "#item-2");
        assert select(xml, 1, "#i4d");
        // CSS2: an ID cannot start with a digit unless escaped.
        // CSS3: more lenient. Test direct selection.
        assert select(xml, 1, "#4id");
        // If strict CSS2 rules for identifiers applied without automatic handling:
        // assert validateSelector(xml, 1, "#\\34 id"); // Escaped '4'
    }

    @Test
    public void idStartingWithDigitOrHyphenRequiresEscapeInCSS2ButNotNecessarilyHere() {
        XML xml = I.xml("""
                <m>
                    <e id='1digit-start'/>
                    <e id='-hype-start'/>
                    <e id='--double-hype-start'/>
                </m>
                """);
        // Test if direct selection works, assuming flexibility or CSS3-like behavior.
        assert select(xml, 1, "#1digit-start");
        assert select(xml, 1, "#-hype-start");
        assert select(xml, 1, "#--double-hype-start");

        // For CSS2 strictness (if needed):
        // assert validateSelector(xml, 1, "#\\31 digit-start");
        // assert validateSelector(xml, 1, "#\\-hyphen-start"); // A single hyphen at start might
        // need
        // escaping
    }

    @Test
    public void idWithSpecialCharactersRequiringEscapingInSelector() {
        // For ID values like "a.b", "a:b"
        // XML attribute: id="a.b" -> Selector: "#a\\.b"
        // XML attribute: id="a:b" -> Selector: "#a\\:b"
        XML xml = I.xml("<m><e id='dot.char'/><e id='colon:char'/><e id='space char'/></m>");
        assert select(xml, 1, "#dot\\.char");
        assert select(xml, 1, "#colon\\:char");
        // IDs with spaces are invalid in HTML, but XML allows them.
        // CSS selectors require spaces to be escaped: #space\ char or #space\\ char
        assert select(xml, 1, "#space\\ char"); // or "#space\\000020char"
    }

    @Test
    public void findByIdOnElementWithNoId() {
        XML xml = I.xml("<root><item>text</item></root>");
        assert select(xml, 0, "#anyId");
    }

    @Test
    public void findByIdOnEmptyDocument() {
        // This depends on how I.xml() handles empty or invalid input.
        // Assuming I.xml("") or I.xml(null) might return an empty XML object or throw error.
        // Let's test on an XML object that is empty but valid.
        XML root = I.xml("<root/>");
        XML empty = root.find("nonexistent"); // empty XML set
        assert select(empty, 0, "#anyId");
    }
}