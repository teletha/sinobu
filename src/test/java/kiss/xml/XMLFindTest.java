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
        assert xml.find("article").size() == 2;
        assert xml.find("  article  ").size() == 2;
        assert xml.find("h1").size() == 2;
        assert xml.find("lonely-child").size() == 1;
        assert xml.find("nonexistent").size() == 0;
    }

    @Test
    public void universalSelector() {
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
        assert xml.find("*").size() == 5;
        assert xml.find(" * ").size() == 5;
        assert xml.find("main > *").size() == 3;
        assert xml.find("main *").size() == 3;
    }

    @Test
    public void types() {
        String text = "<m><E/><F/><e><G/></e></m>";

        assert I.xml(text).find("E,F").size() == 2;
        assert I.xml(text).find("E, F").size() == 2;
        assert I.xml(text).find(" E , F ").size() == 2;
    }

    @Test
    public void typeWithDot() {
        String text = "<m><E.E.E/></m>";

        assert I.xml(text).find("E\\.E\\.E").size() == 1;
    }

    @Test
    public void typeWithHyphen() {
        String text = "<m><E-E/><E--E/></m>";

        assert I.xml(text).find("E-E").size() == 1;
        assert I.xml(text).find("E--E").size() == 1;
    }

    @Test
    public void typeWithEscapedHyphen() {
        String text = "<m><E-E/><E--E/></m>";

        assert I.xml(text).find("E\\-E").size() == 1;
        assert I.xml(text).find("E\\-\\-E").size() == 1;
    }

    @Test
    public void clazz() {
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

    @Test
    public void clazzWithHyphen() {
        String text = "<m><e class='a-b'/><e class='a--b'/><e class='none'/></m>";

        assert I.xml(text).find(".a-b").size() == 1;
        assert I.xml(text).find(".a--b").size() == 1;
    }

    @Test
    public void clazzWithEscapedHyphen() {
        String text = "<m><e class='a-b'/><e class='a--b'/><e class='none'/></m>";

        assert I.xml(text).find(".a\\-b").size() == 1;
        assert I.xml(text).find(".a\\-\\-b").size() == 1;
    }

    @Test
    public void clazzWithEscapedDollar() {
        String text = "<m><e class='a\\b'/><e class='none'/></m>";

        assert I.xml(text).find(".a\\\\b").size() == 1;
    }

    @Test
    public void clazzWithMultipleValue() {
        String text = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(text).find(".A").size() == 1;
        assert I.xml(text).find(".B").size() == 1;
        assert I.xml(text).find(".C").size() == 1;
    }

    @Test
    public void multipleClasses() {
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
    public void id() {
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
    public void idWithHyphen() {
        String text = "<m><e id='A-A'/><e id='A--A'/></m>";

        assert I.xml(text).find("#A-A").size() == 1;
        assert I.xml(text).find("#A--A").size() == 1;
    }

    @Test
    public void idWithEscapedHyphen() {
        String text = "<m><e id='A-A'/><e id='A--A'/></m>";

        assert I.xml(text).find("#A\\-A").size() == 1;
        assert I.xml(text).find("#A\\-\\-A").size() == 1;
    }

    @Test
    public void attribute() {
        String text = "<m><e A='A' B='B'/><e A='B' B='A'/></m>";

        assert I.xml(text).find("[A]").size() == 2;
        assert I.xml(text).find("[B]").size() == 2;
    }

    @Test
    public void attributeNS() {
        String text = "<m xmlns:p='p'><e p:A='A' B='B'/><e A='B' p:B='A'/></m>";

        assert I.xml(text).find("[A]").size() == 2;
        assert I.xml(text).find("[B]").size() == 2;
    }

    @Test
    public void attributeValue() {
        String text = "<m><e A='a'/><e A='B'/></m>";

        // variants for white space
        assert I.xml(text).find("[A='a']").size() == 1;
        assert I.xml(text).find("[A = 'a']").size() == 1;
        assert I.xml(text).find("[A       =    'a']").size() == 1;
        assert I.xml(text).find("[ A = 'a' ]").size() == 1;
    }

    @Test
    public void attributeSingleQuote() {
        String text = "<m><e A='A'/><e A='B'/></m>";

        // variants for white space
        assert I.xml(text).find("[A='A']").size() == 1;
        assert I.xml(text).find("[A = 'A']").size() == 1;
        assert I.xml(text).find("[A       =    'A']").size() == 1;
        assert I.xml(text).find("[ A = 'A' ]").size() == 1;
    }

    @Test
    public void attributeWithoutQuote() {
        String text = "<m><e A='A'/><e A='B'/></m>";

        assert I.xml(text).find("[A=A]").size() == 1;
    }

    @Test
    public void attributeExistence() {
        String text = """
                <root>
                    <item name='one'/>
                    <item id='two'/>
                    <item/>
                    <item name='four' class='test'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[name]").size() == 2;
        assert xml.find("[id]").size() == 1;
        assert xml.find("[class]").size() == 1;
        assert xml.find("[unknown]").size() == 0;
    }

    @Test
    public void attributeEquals() {
        String text = """
                <root>
                    <item name='one'/>
                    <item name='two'/>
                    <item name='one two'/>
                    <item name='ONE'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[name='one']").size() == 1;
        assert xml.find("[name='two']").size() == 1;
        assert xml.find("[name='one two']").size() == 1;
        assert xml.find("[name='ONE']").size() == 1; // CSS attribute selectors are case-sensitive
                                                     // by default for values unless specified
        assert xml.find("[name='three']").size() == 0;
    }

    @Test
    public void attributeContainsWord() { // [attr~=value]
        String text = """
                <root>
                    <item class='foo bar baz'/>
                    <item class='foo'/>
                    <item class='bar foo'/>
                    <item class='foobar'/>
                    <item class='other foo-bar'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[class~='foo']").size() == 3;
        assert xml.find("[class~='bar']").size() == 2;
        assert xml.find("[class~='baz']").size() == 1;
        assert xml.find("[class~='foobar']").size() == 1; // This should match if foobar is a whole
                                                          // word
        assert xml.find("[class~='foo-bar']").size() == 1;
        assert xml.find("[class~='other']").size() == 1;
        assert xml.find("[class~='b']").size() == 0; // "b" is not a whole word
    }

    @Test
    public void attributeStartsWithPrefixOrIsExactly() {
        String text = """
                <root>
                    <item lang='en'/>
                    <item lang='en-US'/>
                    <item lang='en-GB'/>
                    <item lang='fr-CA'/>
                    <item lang='english'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[lang|='en']").size() == 3;
        assert xml.find("[lang|='fr']").size() == 1;
        assert xml.find("[lang|='english']").size() == 1;
        assert xml.find("[lang|='e']").size() == 0;
    }

    @Test
    public void attributeStartsWith() { // [attr^=value]
        String text = """
                <root>
                    <item href='http://example.com'/>
                    <item href='https://example.org'/>
                    <item href='http://another.com'/>
                    <item href='test/http://example.com'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[href^='http://']").size() == 2;
        assert xml.find("[href^='https://']").size() == 1;
        assert xml.find("[href^='example']").size() == 0;
    }

    @Test
    public void attributeEndsWith() { // [attr$=value]
        String text = """
                <root>
                    <item src='image.png'/>
                    <item src='script.js'/>
                    <item src='archive.png.zip'/>
                    <item src='photo.jpeg'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[src$='.png']").size() == 1;
        assert xml.find("[src$='.js']").size() == 1;
        assert xml.find("[src$='.zip']").size() == 1;
        assert xml.find("[src$='jpeg']").size() == 1;
        assert xml.find("[src$='photo']").size() == 0;
    }

    @Test
    public void attributeContains() { // [attr*=value]
        String text = """
                <root>
                    <item title='Chapter 1: Introduction'/>
                    <item title='Chapter 2: Details'/>
                    <item title='Summary of Chapters'/>
                    <item title='chapter three'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[title*='Chapter']").size() == 3; // Case sensitive match
        assert xml.find("[title*='pter 2']").size() == 1;
        assert xml.find("[title*='summary']").size() == 0; // Case sensitive
        assert xml.find("[title*='chapter']").size() == 1; // Case sensitive match
        assert xml.find("[title*='xyz']").size() == 0;
    }

    @Test
    public void attributeNameWithHyphen() {
        String text = "<m><e data-test='value'/><invalid/></m>";
        assert I.xml(text).find("[data-test='value']").size() == 1;
        assert I.xml(text).find("[data-test]").size() == 1;
    }

    @Test
    public void attributeNameWithUnderscore() {
        String text = "<m><e my_attr='value'/><invalid/></m>";
        assert I.xml(text).find("[my_attr='value']").size() == 1;
        assert I.xml(text).find("[my_attr]").size() == 1;
    }

    @Test
    public void attributeExactValue() {
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
    public void attributeWhitespaceSeparatedList() {
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
    public void attributeHyphenSeparatedList() {
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
    public void attributeSubstringContains() {
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
    public void multipleAttributes() {
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

    @Test
    public void multipleAttributeSelectors() {
        String text = """
                <root>
                    <item name='one' type='a'/>
                    <item name='one' type='b'/>
                    <item name='two' type='a'/>
                    <item name='one'/>
                </root>
                """;

        XML xml = I.xml(text);
        assert xml.find("[name='one'][type='a']").size() == 1;
        assert xml.find("[name='one'][type='b']").size() == 1;
        assert xml.find("[name='one'][type='c']").size() == 0;
        assert xml.find("[name][type]").size() == 3;
    }

    @Test
    public void tagAndAttributeSelector() {
        String text = """
                <root>
                    <item class='target'>Item 1</item>
                    <div class='target'>Div 1</div>
                    <item>Item 2</item>
                    <item class='other'>Item 3</item>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("item[class='target']").size() == 1;
        assert xml.find("div[class='target']").size() == 1;
        assert xml.find("item[class='other']").size() == 1;
        assert xml.find("span[class='target']").size() == 0;
    }

    @Test
    public void caseSensitivityOfAttributeNamesAndValues() {
        String text = """
                <root>
                    <item dataName='valueOne' data-value='ValueA'/>
                    <item dataname='valueTwo' data-value='valuea'/>
                </root>
                """;
        XML xml = I.xml(text);
        // Attribute names in XML are case-sensitive.
        // CSS attribute selectors are case-sensitive for names.
        assert xml.find("[dataName]").size() == 1;
        assert xml.find("[dataname]").size() == 1;
        assert xml.find("[DATANAME]").size() == 0;

        // Attribute values in CSS selectors are case-sensitive by default.
        assert xml.find("[data-value='ValueA']").size() == 1;
        assert xml.find("[data-value='valuea']").size() == 1;
        assert xml.find("[data-value='VALUEA']").size() == 0;

        // Test with different types of selectors
        assert xml.find("[dataName^='value']").size() == 1;
        assert xml.find("[dataName^='VALUE']").size() == 0;

        assert xml.find("[data-value*='lueA']").size() == 1;
        assert xml.find("[data-value*='luea']").size() == 1;
        assert xml.find("[data-value*='LUEA']").size() == 0;
    }

    @Test
    public void attributeValueWithSpaces() {
        String text = """
                <root>
                    <item title='hello world'/>
                    <item title='helloworld'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[title='hello world']").size() == 1;
        assert xml.find("[title*='hello w']").size() == 1;
        assert xml.find("[title~='hello']").size() == 1;
        assert xml.find("[title~='world']").size() == 1;
    }

    @Test
    public void attributeValueNS() {
        String text = """
                <m xmlns:p='p' xmlns:z='z'>
                    <e p:A='a'/>
                    <e p:A='b'/>
                    <e z:A='a'/>
                    <e A='b'/>
                </m>
                """;

        // variants for white space
        assert I.xml(text).find("[A='a']").size() == 2;
    }

    @Test
    public void attributeConatainValue() {
        String text = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert I.xml(text).find("[A ~= 'A']").size() == 1;
        assert I.xml(text).find("[A ~= 'B']").size() == 1;
        assert I.xml(text).find("[A ~= 'C']").size() == 1;
    }

    @Test
    public void attributeConatainText() {
        String text = "<m><e A='A B C'/><e A='AB'/></m>";

        assert I.xml(text).find("[A *= 'A']").size() == 2;
        assert I.xml(text).find("[A *= 'B']").size() == 2;
        assert I.xml(text).find("[A *= 'C']").size() == 1;
    }

    @Test
    public void attributeStartWith() {
        String text = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(text).find("[A ^= 'A']").size() == 2;
        assert I.xml(text).find("[A^= 'A']").size() == 2;
        assert I.xml(text).find("[A ^='A']").size() == 2;
        assert I.xml(text).find("[A^='A']").size() == 2;
        assert I.xml(text).find("[A ^= 'B']").size() == 0;
        assert I.xml(text).find("[A ^= 'C']").size() == 0;
    }

    @Test
    public void attributeEndWith() {
        String text = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(text).find("[A $= 'A']").size() == 0;
        assert I.xml(text).find("[A $= 'B']").size() == 0;
        assert I.xml(text).find("[A $= 'C']").size() == 2;
        assert I.xml(text).find("[A $='C']").size() == 2;
        assert I.xml(text).find("[A$= 'C']").size() == 2;
        assert I.xml(text).find("[A$='C']").size() == 2;
    }

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
    public void firstChild() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:first-child").size() == 1;
        assert I.xml(text).find("R:first-child").size() == 0;
    }

    @Test
    public void firstOfType() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:first-of-type").size() == 1;
        assert I.xml(text).find("R:first-of-type").size() == 1;
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

        assert I.xml(text).find("Q:nth-of-type(1)").size() == 1;
        assert I.xml(text).find("Q:nth-of-type(2)").size() == 1;
        assert I.xml(text).find("Q:nth-of-type(5)").size() == 0;
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