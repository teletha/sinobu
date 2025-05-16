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

public class FindIdTest extends FindTestBase {

    @Test
    public void id() {
        XML xml = I.xml("""
                <root>
                    <div id="main-content"></div>
                    <span id="title-element"></span>
                    <a id="link-item"></a>
                    <p id="another"></p>
                    <p id=""></p> <!-- Empty ID attribute -->
                    <p no-id-attr="true"></p> <!-- No ID attribute -->
                </root>
                """);
        validateSelector(xml, 1, "#main-content");
        validateSelector(xml, 1, "#title-element");
        validateSelector(xml, 1, "#link-item");
        validateSelector(xml, 1, "#another");
        validateSelector(xml, 0, "#nonexistent");
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
        validateSelector(xml, 1, "#A-A");
        validateSelector(xml, 1, "#A--A");
        validateSelector(xml, 1, "#--C");
        validateSelector(xml, 1, "#-D");
    }

    @Test
    public void idWithEscapedHyphenInSelector() {
        XML xml = I.xml("<m><e id='A-A'/><e id='A--A'/></m>");
        
        validateSelector(xml, 1, "#A\\-A");
        validateSelector(xml, 1, "#A\\-\\-A");
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
        
        validateSelector(xml, 1, "div#elem1");
        validateSelector(xml, 1, "span#elem1");
        validateSelector(xml, 1, "div#elem2");
        validateSelector(xml, 0, "p#elem1"); // No p with this ID
        validateSelector(xml, 2, "*#elem1"); // Any element with ID elem1
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
        validateSelector(xml, 1, "#myId");
        validateSelector(xml, 1, "#myid");
        validateSelector(xml, 1, "#MYID");
        validateSelector(xml, 0, "#MyId"); // No match
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
        validateSelector(xml, 1, "#item1");
        validateSelector(xml, 1, "#item-2");
        validateSelector(xml, 1, "#i4d");
        // CSS2: an ID cannot start with a digit unless escaped.
        // CSS3: more lenient. Test direct selection.
        validateSelector(xml, 1, "#4id");
        // If strict CSS2 rules for identifiers applied without automatic handling:
        // validateSelector(xml, 1, "#\\34 id"); // Escaped '4'
    }

    @Test
    public void idStartingWithDigitOrHyphenRequiresEscapeInCSS2ButNotNecessarilyHere() {
        XML xml = I.xml("""
                <m>
                    <e id='1digit-start'/>
                    <e id='-hyphen-start'/>
                    <e id='--double-hyphen-start'/>
                </m>
                """);
        // Test if direct selection works, assuming flexibility or CSS3-like behavior.
        validateSelector(xml, 1, "#1digit-start");
        validateSelector(xml, 1, "#-hyphen-start");
        validateSelector(xml, 1, "#--double-hyphen-start");

        // For CSS2 strictness (if needed):
        // validateSelector(xml, 1, "#\\31 digit-start");
        // validateSelector(xml, 1, "#\\-hyphen-start"); // A single hyphen at start might need
        // escaping
    }

    @Test
    public void idWithSpecialCharactersRequiringEscapingInSelector() {
        // For ID values like "a.b", "a:b"
        // XML attribute: id="a.b" -> Selector: "#a\\.b"
        // XML attribute: id="a:b" -> Selector: "#a\\:b"
        XML xml = I.xml("<m><e id='dot.char'/><e id='colon:char'/><e id='space char'/></m>");
        validateSelector(xml, 1, "#dot\\.char");
        validateSelector(xml, 1, "#colon\\:char");
        // IDs with spaces are invalid in HTML, but XML allows them.
        // CSS selectors require spaces to be escaped: #space\ char or #space\\ char
        validateSelector(xml, 1, "#space\\ char"); // or "#space\\000020char"
    }

    @Test
    public void findByIdOnElementWithNoId() {
        XML xml = I.xml("<root><item>text</item></root>");
        validateSelector(xml, 0, "#anyId");
    }

    @Test
    public void findByIdOnEmptyDocument() {
        // This depends on how I.xml() handles empty or invalid input.
        // Assuming I.xml("") or I.xml(null) might return an empty XML object or throw error.
        // Let's test on an XML object that is empty but valid.
        XML root = I.xml("<root/>");
        XML empty = root.find("nonexistent"); // empty XML set
        validateSelector(empty, 0, "#anyId");
    }
}