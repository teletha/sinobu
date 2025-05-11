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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

class XpathComprehensiveTest {

    @Nested
    class BasicSelectors {
        @Test
        void tagSelector() {
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
            assert xml.find("article").size() == 2;
            assert xml.find("h1").size() == 2;
            assert xml.find("lonely-child").size() == 1;
            assert xml.find("nonexistent").size() == 0;
        }

        @Test
        void universalSelector() {
            XML xml = I.xml("""
                    <root>
                        <main>
                            <item1/>
                            <item2/>
                            <item3/>
                        </main>
                        <other/>
                    </root>
                    """);
            assert xml.find("*").size() == 5; // root, main, item1, item2, item3, other
            assert xml.find("main > *").size() == 3;
            assert xml.find("main *").size() == 3; // same as child here, no deeper nesting
        }

        @Test
        void idSelector() {
            XML xml = I.xml("""
                    <root>
                        <div id="main-content"></div>
                        <span id="title-element"></span>
                        <a id="link-item"></a>
                        <p id="another"></p>
                    </root>
                    """);
            assert xml.find("#main-content").size() == 1;
            assert xml.find("#title-element").size() == 1;
            assert xml.find("#link-item").size() == 1;
            assert xml.find("#nonexistent").size() == 0;
        }

        @Test
        void classSelector() {
            XML xml = I.xml("""
                    <root>
                        <div class="post featured"></div>
                        <article class="post"></article>
                        <span class="post icon"></span>
                        <a class="featured"></a>
                        <p class="external"></p>
                        <p class="external large"></p>
                    </root>
                    """);
            assert xml.find(".post").size() == 3;
            assert xml.find(".featured").size() == 2;
            assert xml.find(".external").size() == 2;
            assert xml.find(".nonexistent").size() == 0;
        }
    }

    @Nested
    class AttributeSelectors {
        @Test
        void existence() {
            XML xml = I.xml("""
                    <root>
                        <div data-id="1"></div>
                        <span data-id="test"></span>
                        <item index="A"></item>
                        <input type="checkbox" checked="checked"></input>
                        <option selected="selected"></option>
                        <div data-foo="bar" data-bar="baz"></div>
                        <p/>
                    </root>
                    """);
            assert xml.find("[data-id]").size() == 2;
            assert xml.find("[index]").size() == 1;
            assert xml.find("input[checked]").size() == 1;
            assert xml.find("[selected]").size() == 1;
            assert xml.find("[data-foo][data-bar]").size() == 1;
            assert xml.find("[nonexistent-attr]").size() == 0;
        }

        @Test
        void exactValue() {
            XML xml = I.xml("""
                    <root>
                        <item data-id="1"></item>
                        <item data-id='2'></item>
                        <item data-id="value"></item>
                        <item data-name="test-name"></item>
                        <item data-foo="bar" data-bar='baz'></item>
                    </root>
                    """);
            assert xml.find("[data-id=1]").size() == 1;
            assert xml.find("[data-id='2']").size() == 1;
            assert xml.find("[data-id=\"value\"]").size() == 1;
            assert xml.find("item[data-name=test-name]").size() == 1;
            assert xml.find("[data-foo=\"bar\"][data-bar='baz']").size() == 1;
            assert xml.find("[data-id=nonexistent]").size() == 0;
        }

        @Test
        void whitespaceSeparatedList() { // [attr~=value]
            XML xml = I.xml("""
                    <root>
                        <div class="alpha beta gamma"></div>
                        <div class="beta delta"></div>
                        <div class="epsilon"></div>
                        <span class="alpha"></span>
                    </root>
                    """);
            assert xml.find("[class~=alpha]").size() == 2;
            assert xml.find("[class~=beta]").size() == 2;
            assert xml.find("[class~=gamma]").size() == 1;
            assert xml.find("[class~=epsilon]").size() == 1;
            assert xml.find("[class~=zeta]").size() == 0;
        }

        @Test
        void hyphenSeparatedList() { // [attr|=value]
            XML xml = I.xml("""
                    <root>
                        <p lang="en"></p>
                        <p lang="en-US"></p>
                        <p lang="en-GB"></p>
                        <p lang="fr"></p>
                        <p lang="de-DE"></p>
                    </root>
                    """);
            assert xml.find("[lang|=en]").size() == 3;
            assert xml.find("[lang|=fr]").size() == 1;
            assert xml.find("[lang|=de]").size() == 1;
            assert xml.find("[lang|=de-DE]").size() == 1;
            assert xml.find("[lang|=es]").size() == 0;
        }

        @Test
        void substringContains() { // [attr*=value]
            XML xml = I.xml("""
                    <root>
                        <a title="main link"></a>
                        <a title="secondary link here"></a>
                        <a title="another unrelated"></a>
                    </root>
                    """);
            assert xml.find("[title*=link]").size() == 2;
            assert xml.find("[title*=main]").size() == 1;
            assert xml.find("[title*=secondary]").size() == 1;
            assert xml.find("[title*=other]").size() == 1;
            assert xml.find("[title*=xyz]").size() == 0;
        }

        @Test
        void startsWith() { // [attr^=value]
            XML xml = I.xml("""
                    <root>
                        <a href="#top"></a>
                        <a href="#bottom"></a>
                        <a href="http://example.com"></a>
                        <a href="https://example.org"></a>
                    </root>
                    """);
            assert xml.find("a[href^=#]").size() == 2;
            assert xml.find("a[href^=http://]").size() == 1;
            assert xml.find("a[href^=https]").size() == 1;
            assert xml.find("a[href^=mailto]").size() == 0;
        }

        @Test
        void endsWith() { // [attr$=value]
            XML xml = I.xml("""
                    <root>
                        <img src="image.png"></img>
                        <img src="photo.jpeg"></img>
                        <img src="document.pdf"></img>
                        <img src="archive.png.zip"></img>
                    </root>
                    """);
            assert xml.find("img[src$=.png]").size() == 1;
            assert xml.find("img[src$=.jpeg]").size() == 1;
            assert xml.find("img[src$=.zip]").size() == 1;
            assert xml.find("img[src$=.gif]").size() == 0;
        }

        @Test
        void multipleAttributes() {
            XML xml = I.xml("""
                    <root>
                        <item class="widget" data-type="A" lang="en"></item>
                        <item class="widget" data-type="B" lang="fr"></item>
                        <item class="tool" data-type="A" lang="en"></item>
                        <item class="widget" data-type="A"></item> <!-- No lang -->
                    </root>
                    """);
            assert xml.find("item[class=widget][data-type=A]").size() == 2;
            assert xml.find("item[class=widget][lang=en]").size() == 1;
            assert xml.find("item[data-type=A][lang=en]").size() == 2;
            assert xml.find("item[class=widget][data-type=A][lang=en]").size() == 1;
        }
    }

    @Nested
    class Combinators {

        @Test
        void multipleSelectorsComma() {
            XML xml = I.xml("""
                    <root>
                        <h1>H1</h1>
                        <h2>H2</h2>
                        <p id="p1">P1</p>
                        <span class="info">Info1</span>
                        <span class="info">Info2</span>
                        <h3>H3</h3>
                    </root>
                    """);
            assert xml.find("h1, h2, h3").size() == 3;
            assert xml.find("#p1, .info").size() == 3;
            assert xml.find("h1, .nonexistent, #p1").size() == 2;
        }

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
    }

    @Nested
    class OrderAndMixing {
        @Test
        void idAndClass() {
            XML xml = I.xml("""
                    <root>
                        <article id="art1" class="post featured"></article>
                        <section id="main" class="content"></section>
                        <h1 id="title1"></h1>
                        <div class="post"></div>
                    </root>
                    """);
            assert xml.find("article#art1").size() == 1;
            assert xml.find("section#main").size() == 1;
            assert xml.find(".post#art1").size() == 1; // ID on article.post
            assert xml.find("article.featured").size() == 1;
            assert xml.find(".content#main").size() == 1;

            assert xml.find("article#main").size() == 0; // ID main is on section
            assert xml.find(".post#title1").size() == 0; // ID title1 is on h1
        }

        @Test
        void idClassAttributeMixed() {
            XML xml = I.xml("""
                    <root>
                        <article class="post" data-id="100" id="featured-post"></article>
                        <item class="widget external" id="link-item-2" data-ref="xyz"></item>
                    </root>
                    """);
            assert xml.find("article.post[data-id='100']").size() == 1;
            assert xml.find("article[data-id='100'].post").size() == 1;
            assert xml.find(".post[data-id='100']#featured-post").size() == 1;
            assert xml.find("#featured-post.post[data-id='100']").size() == 1;
            assert xml.find("item.external#link-item-2[data-ref]").size() == 1;
            assert xml.find("#link-item-2.widget[data-ref=xyz].external").size() == 1;
        }

        @Test
        void multipleClasses() {
            XML xml = I.xml("""
                    <root>
                        <div class="a b c"></div>
                        <div class="a b"></div>
                        <div class="a c"></div>
                        <div class="a"></div>
                    </root>
                    """);
            assert xml.find(".a.b.c").size() == 1;
            assert xml.find(".a.b").size() == 2;
            assert xml.find(".b.a").size() == 2; // Order doesn't matter
            assert xml.find(".a.c").size() == 2;
            assert xml.find(".a.d").size() == 0;
        }

        @Test
        void multipleAttributesAndClasses() {
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
        void classAndIdWithSpecialChars() {
            // CSS requires escaping for dots, e.g., #id\.with\.dot or .class\.with\.dot
            // The Xpath.java convert methods current regex `[\\w\\-\\\\]+` implies it might
            // try to consume `id.with.dot` as one token IF a backslash was present for regex meta
            // char.
            // However, if input is `Xpath.convert("#id.with.dot", ...)` it will parse `#id` and
            // then `.with` and `.dot`.
            // To test an ID literally named "id.with.dot", CSS selector is `#id\\.with\\.dot`.
            // The current Xpath.java removes `\` so `#id\\.with\\.dot` becomes `id.with.dot`.
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
    }

    @Nested
    class PseudoClassesStructural {

        @Test
        void empty() {
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
        void firstChild() {
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
        void lastChild() {
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
            // The implementation for :last-child is `[not(following-sibling::*)]`
            // This means #a2 is NOT last-child because #d1 and #s1 follow.
            // #s1 is a last-child. #p2 is a last-child.
            assert xml.find("article:last-child").size() == 0; // a2 is not last, d1 and s1 follow
            assert xml.find("#a2:last-child").size() == 0;
            assert xml.find("span:last-child").size() == 1; // s1
            assert xml.find("#s1:last-child").size() == 1;
            assert xml.find("div > p:last-child").size() == 1; // p2
            assert xml.find("#p1:last-child").size() == 0;
        }

        @Test
        void onlyChild() {
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
        void firstOfType() {
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
        void lastOfType() {
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
        void onlyOfType() {
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
    }

    @Nested
    class PseudoClassesNth {
        @Test
        void nthChild() {
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
        void nthLastChild() {
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
        void nthOfType() {
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
        void nthLastOfType() {
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
    }

    @Nested
    class PseudoClassesLogicalAndMisc {
        @Test
        void not() {
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
        void pseudoContainsText() { // Non-standard CSS, but supported by this Xpath.java
            XML xml = I.xml("""
                    <root>
                        <p>Hello World</p>
                        <span>Introduction to topic</span>
                        <h1>Main Title</h1>
                        <p>Another hello</p>
                    </root>
                    """);
            assert xml.find("p:contains(Hello)").size() == 1; // Case-sensitive
            assert xml.find("p:contains(hello)").size() == 1;
            assert xml.find(":contains(Hello)").size() == 1; // p containing "Hello World"
            assert xml.find(":contains(hello)").size() == 1; // p containing "Another hello"
            assert xml.find("span:contains(Intro)").size() == 1;
            assert xml.find("h1:contains(Title)").size() == 1;
            assert xml.find(":contains(XYZ)").size() == 0;
        }
    }

    @Test
    void combinedStressTest() {
        XML xml = I.xml("""
                <test-root>
                    <section id="main">
                        <article class="post featured" data-id="1">
                            <h1 id="title1">Title One</h1>
                            <p class="intro">An Introduction paragraph.</p>
                        </article>
                    </section>
                    <aside>
                        <ul class="widget links">
                            <li id="l1">L1</li>
                            <li id="l2" class="ext">L2</li> <!-- even -->
                            <li id="l3">L3</li>
                            <li id="l4" class="ext">L4</li> <!-- even -->
                        </ul>
                    </aside>
                    <footer id="ft">
                        <nav>
                            <a href="/h" class="nl">Home</a> <!-- odd, not special -->
                            <a href="/a" class="nl sp">About</a> <!-- even, special -->
                        </nav>
                    </footer>
                </test-root>
                """);
        assert xml.find("section#main article.post.featured > h1#title1 + p.intro:contains(Introduction)").size() == 1;
        // Corrected: :nth-child(even) applies to <li>s. l2 is 2nd, l4 is 4th. Both are .ext. Both
        // are not #link100. So 2.
        assert xml.find("aside ul.links li.ext:nth-child(even):not(#link100)").size() == 2;
        // nav a.nl:nth-of-type(odd) -> Home
        // :not(.sp) -> Home (as Home is not .sp) -> 1 match
        assert xml.find("footer nav a.nl:nth-of-type(odd):not(.sp)").size() == 1;
    }
}