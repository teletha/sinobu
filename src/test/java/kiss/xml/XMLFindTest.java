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
    public void parent() {
        String text = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(text).find("Q:parent").size() == 1;
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