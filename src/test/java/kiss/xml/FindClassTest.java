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

public class FindClassTest extends FindTestBase {

    @Test
    public void singleClass() {
        XML xml = I.xml("""
                <root>
                    <div class="post featured"></div>
                    <article class="post"></article>
                    <span class="post icon"></span>
                    <a class="featured"></a>
                    <p class="external"></p>
                    <p class="external large"></p>
                    <p class=""></p> <!-- Empty class attribute -->
                    <p no-class-attr="true"></p> <!-- No class attribute -->
                </root>
                """);

        validateSelector(xml, 3, ".post");
        validateSelector(xml, 2, ".featured");
        validateSelector(xml, 2, ".external");
        validateSelector(xml, 1, ".icon");
        validateSelector(xml, 1, ".large");
        validateSelector(xml, 0, ".nonexistent");
    }

    @Test
    public void classWithHyphen() {
        XML xml = I.xml("""
                <m>
                    <e class='a-b'/>
                    <e class='a--b'/>
                    <e class='--c'/>
                    <e class='-d'/>
                    <e class='none'/>
                </m>
                """);

        validateSelector(xml, 1, ".a-b");
        validateSelector(xml, 1, ".a--b");
        validateSelector(xml, 1, ".--c");
        validateSelector(xml, 1, ".-d");
    }

    @Test
    public void classWithEscapedHyphenInSelector() {
        XML xml = I.xml("""
                <m>
                    <e class='a-b'/>
                    <e class='a--b'/>
                    <e class='none'/>
                </m>
                """);

        validateSelector(xml, 1, ".a\\-b");
        validateSelector(xml, 1, ".a\\-\\-b");
    }

    @Test
    public void classWithMultipleValuesSpaceSeparated() {
        XML xml = I.xml("""
                <m>
                    <e class='A B C'/>
                    <e class='AA B CC'/>
                    <e class=' B '/> <!-- Leading/trailing spaces in attr value -->
                </m>
                """);

        validateSelector(xml, 1, ".A");
        validateSelector(xml, 3, ".B");
        validateSelector(xml, 1, ".C");
        validateSelector(xml, 1, ".AA");
        validateSelector(xml, 1, ".CC");
    }

    @Test
    public void multipleClassesChained() {
        XML xml = I.xml("""
                <root>
                    <div class="a b c"></div>
                    <div class="a b"></div>
                    <div class="a c"></div>
                    <div class="a"></div>
                    <div class="b c"></div>
                    <div class="c b a"></div> <!-- Different order -->
                </root>
                """);

        validateSelector(xml, 2, ".a.b.c");
        validateSelector(xml, 3, ".a.b");
        validateSelector(xml, 3, ".b.a");
        validateSelector(xml, 3, ".a.c");
        validateSelector(xml, 0, ".a.d");
        validateSelector(xml, 3, ".b.c");
        validateSelector(xml, 3, ".c.b");
    }

    @Test
    public void classCombinedWithTag() {
        XML xml = I.xml("""
                <root>
                    <div class="message error">Error</div>
                    <span class="message warning">Warning</span>
                    <div class="message success">Success</div>
                    <p class="error">Paragraph error</p>
                </root>
                """);

        validateSelector(xml, 2, "div.message");
        validateSelector(xml, 1, "span.message");
        validateSelector(xml, 1, "span.warning");
        validateSelector(xml, 0, "p.message");
        validateSelector(xml, 2, "*.error");
        validateSelector(xml, 1, "div.error");
        validateSelector(xml, 1, "p.error");
    }

    @Test
    public void classCaseSensitivity() {
        XML xml = I.xml("""
                <root>
                    <div class="myClass"></div>
                    <div class="myclass"></div>
                    <div class="MYCLASS"></div>
                </root>
                """);

        validateSelector(xml, 1, ".myClass");
        validateSelector(xml, 1, ".myclass");
        validateSelector(xml, 1, ".MYCLASS");
        validateSelector(xml, 0, ".MyClass"); // No match
    }

    @Test
    public void classWithNumbers() {
        XML xml = I.xml("""
                <root>
                    <div class="item1"></div>
                    <div class="item-2"></div>
                    <div class="c4s"></div>
                </root>
                """);

        validateSelector(xml, 1, ".item1");
        validateSelector(xml, 1, ".item-2");
        validateSelector(xml, 1, ".c4s");
    }

    @Test
    public void classStartingWithDigitOrHyphenRequiresEscapeInCSS2ButNotNecessarilyHere() {
        XML xml = I.xml("""
                <m>
                    <e class='1digit-start'/> <!-- Potentially problematic class name for CSS2 -->
                    <e class='-hyphen-start'/>
                    <e class='--double-hyphen-start'/>
                </m>
                """);

        validateSelector(xml, 1, ".1digit-start");
        validateSelector(xml, 1, ".-hyphen-start");
        validateSelector(xml, 1, ".--double-hyphen-start");
    }

    @Test
    public void classWithSpecialCharactersRequiringEscaping() {
        // For class names like "a.b", "a:b", "a[b]"
        // XML attribute: class="a.b" -> Selector: ".a\\.b"
        // XML attribute: class="a:b" -> Selector: ".a\\:b"
        // XML attribute: class="a[b]" -> Selector: ".a\\\\[b\\]" (more complex)
        XML xml = I.xml("<m><e class='dot.char'/><e class='colon:char'/></m>");

        validateSelector(xml, 1, ".dot\\.char");
        validateSelector(xml, 1, ".colon\\:char");
    }
}