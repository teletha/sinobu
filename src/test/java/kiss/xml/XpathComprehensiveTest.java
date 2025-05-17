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

class XpathComprehensiveTest {

    @Test
    public void userProvidedMultipleCommaMixed() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                    <d class='value'/>
                    <e class='value'/>
                </root>
                """);

        assert xml.find("a, b").size() == 2;
        assert xml.find("a, .value").size() == 3;
        assert xml.find("a:first-child, .value").size() == 3;
        assert xml.find("b:first-child, .value:last-child").size() == 1;
    }

    // OrderAndMixing

    @Test
    public void multipleAttributesAndClasses() {
        XML xml = I.xml("""
                <root>
                    <input type="radio" name="gender" value="female" checked="checked" class="form-input important"></input>
                    <input type="radio" name="gender" value="male" class="form-input"></input>
                    <li class="external item" id="link2" title="External Link Two"></li>
                </root>
                """);
        assert xml.find("input[type='radio'][name='gender'][checked].form-input").size() == 1;
        assert xml.find("li.external.item[id='link2'][title*='External']").size() == 1;
    }

    @Test
    public void userProvidedOrder() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c class='value'/>
                    <d class='value' id='key'/>
                    <e class='value' id='test'/>
                </root>
                """);

        assert xml.find("#key.value").size() == 1;
        assert xml.find(".value#test").size() == 1;
        assert xml.find(".value[id]").size() == 2;
        assert xml.find("[id].value").size() == 2;
    }

    @Test
    public void classAndIdWithSpecialChars() {
        XML xml = I.xml("""
                <root>
                    <div id="id.with.dot">ID with dot</div>
                    <div id="id-with-hyphen">ID with hyphen</div>
                    <div class="class-with-hyphen">Class with hyphen</div>
                    <div class="class.with.dot">Class with dot</div>
                </root>
                """);
        assert xml.find("#id\\.with\\.dot").size() == 1;
        assert xml.find("#id-with-hyphen").size() == 1;
        assert xml.find(".class-with-hyphen").size() == 1;
        assert xml.find(".class\\.with\\.dot").size() == 1;
    }

    // PseudoClassesStructural
    @Test
    public void pseudoEmpty() { // Renamed from "empty"
        XML xml = I.xml("""
                <root>
                    <figure></figure>
                    <p class="empty-p"></p>
                    <p class="extra-info">  </p> <!-- Whitespace means not empty for XPath 'not(node())' -->
                    <p class="extra-info-really-empty"></p>
                    <div><span>Not empty</span></div>
                    <data/>
                </root>
                """);
        assert xml.find("figure:empty").size() == 1;
        assert xml.find("p.empty-p:empty").size() == 1;
        assert xml.find("p.extra-info:empty").size() == 0; // Has whitespace text node
        assert xml.find("p.extra-info-really-empty:empty").size() == 1;
        assert xml.find("data:empty").size() == 1;
        assert xml.find("div:empty").size() == 0;
        assert xml.find("p:empty").size() == 2; // .empty-p, .extra-info-really-empty
    }

    @Test
    public void pseudoFirstChild() { // Renamed from "firstChild"
        XML xml = I.xml("""
                <section>
                    <article id="a1"></article> <!-- first-child of section -->
                    <article id="a2"></article>
                    <div id="d1">
                        <p id="p1"></p> <!-- first-child of div -->
                        <p id="p2"></p>
                    </div>
                </section>
                """);
        assert xml.find("article:first-child").size() == 1; // a1
        assert xml.find("#a1:first-child").size() == 1;
        assert xml.find("#a2:first-child").size() == 0;
        assert xml.find("div > p:first-child").size() == 1; // p1
        assert xml.find("#p2:first-child").size() == 0;
    }

    @Test
    public void pseudoLastChild() { // Renamed from "lastChild"
        XML xml = I.xml("""
                <section>
                    <article id="a1"></article>
                    <article id="a2"></article> <!-- last-child of section -->
                    <div id="d1">
                        <p id="p1"></p>
                        <p id="p2"></p> <!-- last-child of div -->
                    </div>
                    <span id="s1"></span> <!-- also a last child of section, but different type -->
                </section>
                """);
        assert xml.find("article:last-child").size() == 0; // a2 is not last, d1 and s1 follow
        assert xml.find("#a2:last-child").size() == 0;
        assert xml.find("span:last-child").size() == 1; // s1
        assert xml.find("#s1:last-child").size() == 1;
        assert xml.find("div > p:last-child").size() == 1; // p2
        assert xml.find("#p1:last-child").size() == 0;
    }

    @Test
    public void pseudoOnlyChild() { // Renamed from "onlyChild"
        XML xml = I.xml("""
                <root>
                    <parent1><child1/></parent1> <!-- child1 is only-child -->
                    <parent2><childA/><childB/></parent2>
                    <parent3><item id="i1"/></parent3> <!-- item is only-child -->
                </root>
                """);
        assert xml.find("child1:only-child").size() == 1;
        assert xml.find("item:only-child").size() == 1;
        assert xml.find("#i1:only-child").size() == 1;
        assert xml.find("childA:only-child").size() == 0;
    }

    @Test
    public void pseudoFirstOfType() { // Renamed from "firstOfType"
        XML xml = I.xml("""
                <root>
                    <section>
                        <h1>H1.1 (first h1)</h1>
                        <p>P1.1 (first p)</p>
                        <h2>H2.1 (first h2)</h2>
                        <p>P1.2 (second p)</p>
                        <h1>H1.2 (second h1)</h1>
                        <p>P1.3 (third p)</p>
                    </section>
                </root>
                """);
        assert xml.find("h1:first-of-type").size() == 1;
        assert xml.find("p:first-of-type").size() == 1;
        assert xml.find("h2:first-of-type").size() == 1;
        assert xml.find("section > h1:first-of-type").size() == 1; // H1.1
    }

    @Test
    public void pseudoLastOfType() { // Renamed from "lastOfType"
        XML xml = I.xml("""
                <root>
                    <section>
                        <h1>H1.1</h1>
                        <p>P1.1</p>
                        <h2>H2.1 (last h2)</h2>
                        <p>P1.2</p>
                        <h1>H1.2 (last h1)</h1>
                        <p>P1.3 (last p)</p>
                    </section>
                </root>
                """);
        assert xml.find("h1:last-of-type").size() == 1;
        assert xml.find("p:last-of-type").size() == 1;
        assert xml.find("h2:last-of-type").size() == 1;
        assert xml.find("section > p:last-of-type").size() == 1; // P1.3
    }

    @Test
    public void pseudoOnlyOfType() { // Renamed from "onlyOfType"
        XML xml = I.xml("""
                <section>
                    <h1>H1 (only h1)</h1>
                    <p>P1</p>
                    <p>P2</p>
                    <span>S1 (only span)</span>
                </section>
                """);
        assert xml.find("h1:only-of-type").size() == 1;
        assert xml.find("span:only-of-type").size() == 1;
        assert xml.find("p:only-of-type").size() == 0;
    }

    // PseudoClassesNth
    @Test
    public void pseudoNthChild() { // Renamed from "nthChild"
        XML xml = I.xml("""
                <ul>
                    <li>1</li> <!-- nth-child(1), odd -->
                    <li>2</li> <!-- nth-child(2), even -->
                    <li>3</li> <!-- nth-child(3), odd -->
                    <li>4</li> <!-- nth-child(4), even -->
                    <li>5</li> <!-- nth-child(5), odd -->
                </ul>
                """);
        assert xml.find("li:nth-child(2)").size() == 1;
        assert xml.find("li:nth-child(odd)").size() == 3;
        assert xml.find("li:nth-child(even)").size() == 2;
        assert xml.find("li:nth-child(2n+1)").size() == 3; // Same as odd
        assert xml.find("li:nth-child(2n)").size() == 2; // Same as even
        assert xml.find("li:nth-child(3n)").size() == 1; // 3rd child
        assert xml.find("li:nth-child(n+3)").size() == 3; // 3rd, 4th, 5th
        assert xml.find("li:nth-child(-n+2)").size() == 2; // 1st, 2nd
    }

    @Test
    public void pseudoNthLastChild() { // Renamed from "nthLastChild"
        XML xml = I.xml("""
                <ul>
                    <li>1</li> <!-- nth-last-child(5), odd -->
                    <li>2</li> <!-- nth-last-child(4), even -->
                    <li>3</li> <!-- nth-last-child(3), odd -->
                    <li>4</li> <!-- nth-last-child(2), even -->
                    <li>5</li> <!-- nth-last-child(1), odd -->
                </ul>
                """);
        assert xml.find("li:nth-last-child(1)").size() == 1;
        assert xml.find("li:nth-last-child(odd)").size() == 3;
        assert xml.find("li:nth-last-child(even)").size() == 2;
        assert xml.find("li:nth-last-child(3)").size() == 1;
    }

    @Test
    public void pseudoNthOfType() { // Renamed from "nthOfType"
        XML xml = I.xml("""
                <section>
                    <p>P1</p> <!-- p:nth-of-type(1) -->
                    <h1>H1</h1> <!-- h1:nth-of-type(1) -->
                    <p>P2</p> <!-- p:nth-of-type(2) -->
                    <h2>H2</h2> <!-- h2:nth-of-type(1) -->
                    <p>P3</p> <!-- p:nth-of-type(3) -->
                    <h1>H3</h1> <!-- h1:nth-of-type(2) -->
                </section>
                """);
        assert xml.find("p:nth-of-type(2)").size() == 1; // P2
        assert xml.find("p:nth-of-type(odd)").size() == 2; // P1, P3
        assert xml.find("h1:nth-of-type(even)").size() == 1; // H3
    }

    @Test
    public void pseudoNthLastOfType() { // Renamed from "nthLastOfType"
        XML xml = I.xml("""
                <section>
                    <p>P1</p> <!-- p:nth-last-of-type(3) -->
                    <h1>H1</h1> <!-- h1:nth-last-of-type(2) -->
                    <p>P2</p> <!-- p:nth-last-of-type(2) -->
                    <h2>H2</h2> <!-- h2:nth-last-of-type(1) -->
                    <p>P3</p> <!-- p:nth-last-of-type(1) -->
                    <h1>H3</h1> <!-- h1:nth-last-of-type(1) -->
                </section>
                """);
        assert xml.find("p:nth-last-of-type(1)").size() == 1; // P3
        assert xml.find("h1:nth-last-of-type(odd)").size() == 1; // H3 (1st from last)
        assert xml.find("p:nth-last-of-type(even)").size() == 1; // P2 (2nd from last)
    }

    // PseudoClassesLogicalAndMisc
    @Test
    public void pseudoNot() { // Renamed from "not"
        XML xml = I.xml("""
                <root>
                    <item class="a" id="i1"></item>
                    <item class="b" id="i2"></item>
                    <item class="a b" id="i3"></item>
                    <item id="i4"></item>
                    <item class="c"></item>
                </root>
                """);
        assert xml.find("item:not(.a)").size() == 3; // i2, i4, .c
        assert xml.find("item:not(#i1)").size() == 4;
        assert xml.find("item:not(.a):not(.b)").size() == 2; // i4, .c
        assert xml.find("item:not([class])").size() == 1; // i4
    }

    @Test
    public void pseudoContainsText() {
        XML xml = I.xml("""
                <root>
                    <p>Hello World</p>
                    <span>Introduction to topic</span>
                    <h1>Main Title</h1>
                    <p>Another hello</p>
                </root>
                """);
        assert xml.find("p:contains('Hello')").size() == 1; // Case-sensitive
        assert xml.find("p:contains('hello')").size() == 1;
        assert xml.find(":contains('Hello')").size() == 1; // p containing "Hello World"
        assert xml.find(":contains('hello')").size() == 1; // p containing "Another hello"
        assert xml.find("span:contains('Intro')").size() == 1;
        assert xml.find("h1:contains('Title')").size() == 1;
        assert xml.find(":contains('XYZ')").size() == 0;
    }
}