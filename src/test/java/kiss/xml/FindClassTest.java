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

public class FindClassTest extends FindAssetion {

    @Test
    public void clazz() {
        XML xml = I.xml("""
                <root>
                    <p class="on"/>
                    <p class="on large"/>
                    <p class=""/>
                    <p no-class-attr="on"/>
                </root>
                """);

        assert xml.find(".on").size() == 2;
        assert xml.find(".large").size() == 1;
        assert xml.find(".on.large").size() == 1;
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

        assert select(xml, 1, ".a-b");
        assert select(xml, 1, ".a--b");
        assert select(xml, 1, ".--c");
        assert select(xml, 1, ".-d");
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

        assert select(xml, 1, ".a\\-b");
        assert select(xml, 1, ".a\\-\\-b");
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

        assert select(xml, 1, ".A");
        assert select(xml, 3, ".B");
        assert select(xml, 1, ".C");
        assert select(xml, 1, ".AA");
        assert select(xml, 1, ".CC");
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

        assert select(xml, 2, ".a.b.c");
        assert select(xml, 3, ".a.b");
        assert select(xml, 3, ".b.a");
        assert select(xml, 3, ".a.c");
        assert select(xml, 0, ".a.d");
        assert select(xml, 3, ".b.c");
        assert select(xml, 3, ".c.b");
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

        assert select(xml, 2, "div.message");
        assert select(xml, 1, "span.message");
        assert select(xml, 1, "span.warning");
        assert select(xml, 0, "p.message");
        assert select(xml, 2, "*.error");
        assert select(xml, 1, "div.error");
        assert select(xml, 1, "p.error");
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

        assert select(xml, 1, ".myClass");
        assert select(xml, 1, ".myclass");
        assert select(xml, 1, ".MYCLASS");
        assert select(xml, 0, ".MyClass"); // No match
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

        assert select(xml, 1, ".item1");
        assert select(xml, 1, ".item-2");
        assert select(xml, 1, ".c4s");
    }

    @Test
    public void classStartingWithDigitOrHyphenRequiresEscapeInCSS2ButNotNecessarilyHere() {
        XML xml = I.xml("""
                <m>
                    <e class='1digit-start'/> <!-- Potentially problematic class name for CSS2 -->
                    <e class='-hype-start'/>
                    <e class='--double-hype-start'/>
                </m>
                """);

        assert select(xml, 1, ".1digit-start");
        assert select(xml, 1, ".-hype-start");
        assert select(xml, 1, ".--double-hype-start");
    }

    @Test
    public void classWithSpecialCharactersRequiringEscaping() {
        // For class names like "a.b", "a:b", "a[b]"
        // XML attribute: class="a.b" -> Selector: ".a\\.b"
        // XML attribute: class="a:b" -> Selector: ".a\\:b"
        // XML attribute: class="a[b]" -> Selector: ".a\\\\[b\\]" (more complex)
        XML xml = I.xml("<m><e class='dot.char'/><e class='colon:char'/></m>");

        assert select(xml, 1, ".dot\\.char");
        assert select(xml, 1, ".colon\\:char");
    }
}