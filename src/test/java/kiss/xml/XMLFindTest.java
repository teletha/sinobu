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

public class XMLFindTest {

    @Test
    public void multiple() {
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
        assert xml.find(" a , b ").size() == 2;
        assert xml.find("a, .value").size() == 3;
        assert xml.find("a:first-child, .value").size() == 3;
        assert xml.find("b:first-child, .value:last-child").size() == 1;
    }

    @Test
    public void combineIdAndClassAnd() {
        XML xml = I.xml("""
                <root>
                    <article id="art1" class="post featured"></article>
                    <section id="main" class="content"></section>
                    <h1 id="art1"></h1>
                    <div class="post"></div>
                </root>
                """);
        assert xml.find("article#art1").size() == 1;
        assert xml.find("section#main").size() == 1;
        assert xml.find(".post#art1").size() == 1;
        assert xml.find(" .post#art1 ").size() == 1;
        assert xml.find("#art1.post").size() == 1; // ID on article.post
        assert xml.find("article.featured").size() == 1;
        assert xml.find(".content#main").size() == 1;

        assert xml.find("article#main").size() == 0; // ID main is on section
        assert xml.find(".post#title1").size() == 0; // ID title1 is on h1
    }

    @Test
    public void combineIdAndClassAndAttribute() {
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
    public void combinedStressTest() {
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
        assert xml.find("aside ul.links li.ext:nth-child(even):not(#link100)").size() == 2;
        assert xml.find("footer nav a.nl:nth-of-type(odd):not(.sp)").size() == 1;
    }

    @Test
    public void child1() {
        String text = xml("""
                <m>
                    <P>
                        <Q/>
                        <z>
                            <Q/>
                        </z>
                        <Q/>
                    </P>
                    <Q/>
                </m>
                """);

        assert I.xml(text).find("P>Q").size() == 2;
        assert I.xml(text).find("P > Q").size() == 2;
        assert I.xml(text).find("P   >    Q").size() == 2;
    }

    @Test
    public void child2() {
        XML xml = I.xml("""
                <root>
                    <section>
                        <p>P1</p>
                        <div><p>P2_grandchild</p></div>
                        <p>P3</p>
                    </section>
                </root>
                """);
        assert xml.find("section > p").size() == 2;
        assert xml.find("section > div").size() == 1;
        assert xml.find("div > p").size() == 1;
        assert xml.find("section > span").size() == 0;
    }

    @Test
    public void descendant() {
        XML xml = I.xml("""
                <root>
                    <section>
                        <p>S1P1</p>
                        <div>
                            <p>S1D1P1</p>
                        </div>
                        <article>
                            <p>S1A1P1</p>
                            <p>S1A1P2</p>
                        </article>
                    </section>
                    <p>P_out</p>
                </root>
                """);
        assert xml.find("section p").size() == 4;
        assert xml.find("div p").size() == 1;
        assert xml.find("article p").size() == 2;
        assert xml.find("section div p").size() == 1;
    }

    @Test
    public void sibling() {
        String text = xml("""
                <m>
                    <P>
                        <Q/>
                        <P/>
                        <Q/>
                        <Q/>
                    </P>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert I.xml(text).find("P+Q").size() == 2;
        assert I.xml(text).find("P + Q").size() == 2;
        assert I.xml(text).find("P   +   Q").size() == 2;
        assert I.xml(text).find(" P   +   Q ").size() == 2;
    }

    @Test
    public void siblings() {
        String text = xml("""
                <m>
                    <P>
                        <Q/>
                        <P/>
                        <Q/>
                    </P>
                    <Q/>
                    <Q/>
                    <nonstop/>
                    <Q/>
                </m>
                """);

        assert I.xml(text).find("P~Q").size() == 4;
        assert I.xml(text).find("P ~ Q").size() == 4;
        assert I.xml(text).find("P   ~   Q").size() == 4;
        assert I.xml(text).find("  P   ~   Q  ").size() == 4;
    }

    @Test
    public void generalSiblingWithClass() {
        XML xml = I.xml("""
                <m>
                    <P class="a"/>
                    <Q class="x"/>
                    <Q class="y"/>
                    <Q class="z"/>
                    <Q/>
                </m>
                """);
        assert xml.find("P.a ~ Q").size() == 4;
        assert xml.find("P.a ~ Q.y").size() == 1;
        assert xml.find("P.a ~ Q:not([class])").size() == 1;
    }

    @Test
    public void previous() {
        String text = xml("""
                <m>
                    <P>
                        <Q id='a'/>
                        <P/>
                        <Q id='b'/>
                    </P>
                    <Q id='c'/>
                    <Q id='d'/>
                    <P/>
                </m>
                """);

        assert I.xml(text).find("P<Q").size() == 2;
        assert I.xml(text).find("P < Q").size() == 2;
        assert I.xml(text).find("P   <   Q").size() == 2;
    }

    @Test
    public void lastChild() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:last-child").size() == 0;
        assert I.xml(text).find("R:last-child").size() == 1;
    }

    @Test
    public void lastOfType() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:last-of-type").size() == 1;
        assert I.xml(text).find("R:last-of-type").size() == 1;
    }

    @Test
    public void nthChild() {
        String text = xml("<m><Q/><Q/><Q/><Q/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-child(1)").size() == 1;
        assert I.xml(text).find("Q:nth-child(2)").size() == 1;
        assert I.xml(text).find("Q:nth-child(5)").size() == 0;
        assert I.xml(text).find("Q:nth-child(100)").size() == 0;
        assert I.xml(text).find("Q:nth-child(2n)").size() == 3;
        assert I.xml(text).find("Q:nth-child(3n)").size() == 2;
        assert I.xml(text).find("Q:nth-child(4n)").size() == 1;
        assert I.xml(text).find("Q:nth-child(2n+1)").size() == 3;
        assert I.xml(text).find("Q:nth-child(odd)").size() == 3;
        assert I.xml(text).find("Q:nth-child(even)").size() == 3;
    }

    @Test
    public void nthChildSpacedArg() {
        XML xml = I.xml("""
                <root>
                    <item>1</item>
                    <item>2</item>
                    <item>3</item>
                    <item>4</item>
                </root>
                """);

        assert xml.find("item:nth-child( 1 )").size() == 1;
        assert xml.find("item:nth-child( 2n )").size() == 2;
        assert xml.find("item:nth-child( -n + 2 )").size() == 2;
    }

    @Test
    public void nthChildInvalidArg() {
        XML xml = I.xml("""
                <root>
                    <item>1</item>
                    <item>2</item>
                    <item>3</item>
                    <item>4</item>
                </root>
                """);

        assert xml.find("item:nth-child(-1)").size() == 0;
        assert xml.find("item:nth-child(0)").size() == 0;
        assert xml.find("item:nth-child(0n)").size() == 0;
        assert xml.find("item:nth-child(-n)").size() == 0;
        assert xml.find("item:nth-child(-0n)").size() == 0;
        assert xml.find("item:nth-child(-2n-0)").size() == 0;
        assert xml.find("item:nth-child(n+1000)").size() == 0;
    }

    @Test
    public void nthOfType() {
        String text = xml("<m><Q/><P/><Q/><P/><P/><Q/><Q/></m>");

        // assert I.xml(text).find("Q:nth-of-type(1)").size() == 1;
        // assert I.xml(text).find("Q:nth-of-type(2)").size() == 1;
        // assert I.xml(text).find("Q:nth-of-type(5)").size() == 0;
        assert I.xml(text).find("Q:nth-of-type(n)").size() == 4;
        assert I.xml(text).find("Q:nth-of-type(2n)").size() == 2;
        assert I.xml(text).find("Q:nth-of-type(3n)").size() == 1;
        assert I.xml(text).find("Q:nth-of-type(2n+1)").size() == 2;
        assert I.xml(text).find("Q:nth-of-type(odd)").size() == 2;
        assert I.xml(text).find("Q:nth-of-type(even)").size() == 2;
    }

    @Test
    public void nthLastChild() {
        String text = xml("<m><Q/><Q/><Q/><Q/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-last-child(1)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(2)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(3)").size() == 0;
        assert I.xml(text).find("Q:nth-last-child(100)").size() == 0;
        assert I.xml(text).find("Q:nth-last-child(2n)").size() == 3;
        assert I.xml(text).find("Q:nth-last-child(3n)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(4n)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(2n+1)").size() == 3;
        assert I.xml(text).find("Q:nth-last-child(odd)").size() == 3;
        assert I.xml(text).find("Q:nth-last-child(even)").size() == 3;
    }

    @Test
    public void nthLastOfType() {
        String text = xml("<m><Q/><P/><Q/><P/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-last-of-type(1)").size() == 1;
        assert I.xml(text).find("Q:nth-last-of-type(2)").size() == 1;
        assert I.xml(text).find("Q:nth-last-of-type(5)").size() == 0;
        assert I.xml(text).find("Q:nth-last-of-type(n)").size() == 4;
        assert I.xml(text).find("Q:nth-last-of-type(2n)").size() == 2;
        assert I.xml(text).find("Q:nth-last-of-type(3n)").size() == 1;
        assert I.xml(text).find("Q:nth-last-of-type(2n+1)").size() == 2;
        assert I.xml(text).find("Q:nth-last-of-type(odd)").size() == 2;
        assert I.xml(text).find("Q:nth-last-of-type(even)").size() == 2;
    }

    @Test
    public void onlyChild() {
        String text = xml("<m><Q/><r><Q/></r><r><Q/></r></m>");

        assert I.xml(text).find("Q:only-child").size() == 2;
    }

    @Test
    public void onlyOfType() {
        String text = xml("<m><Q/><r><Q/><P/></r><r><Q/></r><Q/></m>");

        assert I.xml(text).find("Q:only-of-type").size() == 2;
    }

    @Test
    public void empty() {
        String text = xml("<m><Q/><Q>text</Q><Q><r/></Q></m>");

        assert I.xml(text).find("Q:empty").size() == 1;
    }

    @Test
    public void notElement() {
        String text = xml("<m><Q><S/></Q><Q><t/></Q></m>");

        assert I.xml(text).find("Q:not(S)").size() == 1;
    }

    @Test
    public void notAttribute() {
        String text = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(text).find("Q:not(.A)").size() == 1;
    }

    @Test
    public void hasElement() {
        String text = xml("<m><Q><S/></Q><Q><S/><T/></Q><Q><T/></Q></m>");

        assert I.xml(text).find("Q:has(S)").size() == 2;
        assert I.xml(text).find("Q:has(T)").size() == 2;
        assert I.xml(text).find("Q:has(T:first-child)").size() == 1;
        assert I.xml(text).find("Q:has(S + T)").size() == 1;
    }

    @Test
    public void hasElementNest() {
        String text = xml("""
                <m>
                    <Q>
                        <S/>
                    </Q>
                    <Q>
                        <S>
                            <T/>
                        </S>
                    </Q>
                </m>
                """);

        assert I.xml(text).find("Q:has(S:has(T))").size() == 1;
    }

    @Test
    public void hasAttribute() {
        String text = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(text).find("Q:has(.A)").size() == 2;
        assert I.xml(text).find("Q:has(.B)").size() == 2;
        assert I.xml(text).find("Q:has(.A.B)").size() == 1;
    }

    @Test
    public void parent() {
        String text = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(text).find("Q:parent").size() == 1;
    }

    @Test
    public void parent2() {
        String text = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(text).find("Q").parent().size() == 1;
    }

    @Test
    public void root() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
                """);

        assert xml.find(":root").name().equals("root");
        assert xml.find("a").find(":root").name().equals("root");
        assert xml.find(":root b").size() == 1;
        assert xml.find(":root , b").size() == 2;
        assert xml.find("b , :root").size() == 2;
    }

    @Test
    public void scope() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
                """);

        assert xml.find(":scope").name().equals("root");
        assert xml.find("a").find(":scope").name().equals("a");
        assert xml.find(":scope b").size() == 1;
        assert xml.find(":scope , b").size() == 2;
        assert xml.find("b , :scope").size() == 2;
    }

    @Test
    public void scopeWithCombinator() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
                """);

        assert xml.find("a").find(":scope ~ *, :scope").size() == 3;
        assert xml.find("a").find("~ *, :scope").size() == 3;
        assert xml.find("a").find(":scope, :scope ~ *").size() == 3;
        assert xml.find("a").find(":scope,  ~ *").size() == 3;
    }

    @Test
    public void contains() {
        String text = xml("<m><Q>a</Q><Q>b</Q><Q>aa</Q></m>");

        assert I.xml(text).find("Q:contains(a)").size() == 2;
        assert I.xml(text).find("Q:contains(b)").size() == 1;
        assert I.xml(text).find("Q:contains(aa)").size() == 1;
    }

    @Test
    public void namespacedElementIsSelectedByLocalName() {
        XML xml = I.xml("""
                <root xmlns:myns="http://example.com/ns" xmlns:other="http://other.com/ns">
                    <myns:elem id="ns1">Namespace Test 1</myns:elem>
                    <other:elem id="ns2">Namespace Test 2</other:elem>
                    <elem id="no-ns">No Namespace</elem>
                    <myns:widget id="widget1"/>
                </root>
                """);

        assert xml.find("elem").size() == 3;
    }

    @Test
    public void contextual() {
        XML root = I.xml("""
                <Q>
                    <Q/>
                    <Q/>
                </Q>
                """);

        assert root.find("> Q").size() == 2;
        assert root.find(">Q").find("+Q").size() == 1;
        assert root.find("> Q").find("~Q").size() == 1;
    }

    @Test
    public void checkCacheKeyWithDifferentAxis() {
        XML root = I.xml("""
                <root>
                    <h>heading1</h>
                    <p>para1-1</p>
                    <h>heading2</h>
                    <p>para2-1</p>
                    <h>heading3</h>
                    <p>para3-1</p>
                </root>
                """);

        // the compiled XPATH cache key must take into account the axis as well as the selector.
        XML xml = root.find("h");
        assert xml.size() == 3;
        for (XML h : xml) {
            assert h.nextUntil("h").size() == 1;
        }
    }

    /**
     * <p>
     * Format to human-redable text for display when assertion is fail..
     * </p>
     */
    private static final String xml(String text) {
        return text;
    }
}