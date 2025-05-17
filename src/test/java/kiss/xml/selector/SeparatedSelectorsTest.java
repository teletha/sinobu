/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.selector;

import static kiss.xml.selector.FindAssetion.*;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class SeparatedSelectorsTest {

    @Test
    public void multiple() {
        XML xml = I.xml("""
                <root>
                    <a id="a1"/>
                    <b id="b1"/>
                    <c id="c1"/>
                    <d id="d1" class='value'/>
                    <e id="e1" class='value'/>
                </root>
                """);

        assert selectWithPermutation(xml, 2, "a", "b");
        assert selectWithPermutation(xml, 3, "a", ".value");
        assert selectWithPermutation(xml, 3, "a:first-child", ".value");
        assert selectWithPermutation(xml, 1, "b:first-child", ".value:last-child");
    }

    @Test
    public void tagAndClass() {
        XML xml = I.xml("""
                <root>
                    <x class='foo' id="x1"/>
                    <y class='bar' id="y1"/>
                    <z class='foo bar' id="z1"/>
                </root>
                """);

        assert selectWithPermutation(xml, 3, ".foo", ".bar");
        assert selectWithPermutation(xml, 2, "x.foo", "y.bar");
        assert selectWithPermutation(xml, 1, "z.foo.bar");
    }

    @Test
    public void pseudoAndAttribute() {
        XML xml = I.xml("""
                <root>
                    <item class='target' id="i1"/>
                    <item class='target' id="i2"/>
                    <item id="i3"/>
                </root>
                """);

        assert selectWithPermutation(xml, 2, ".target", "item:nth-child(1)");
        assert selectWithPermutation(xml, 2, ".target", "item:nth-child(2)");
        assert selectWithPermutation(xml, 3, ".target", "item:last-child");
    }

    @Test
    public void tagAndAttribute() {
        XML xml = I.xml("""
                <root>
                    <entry type='x' id="e1"/>
                    <entry type='y' id="e2"/>
                    <entry type='z' id="e3"/>
                </root>
                """);

        assert selectWithPermutation(xml, 2, "entry[type='x']", "entry[type='y']");
        assert selectWithPermutation(xml, 3, "entry", "entry[type='z']");
    }

    @Test
    public void multipleCombinators() {
        XML xml = I.xml("""
                <root>
                    <group id="g1">
                        <child id="c1"/>
                        <child class='value' id="c2"/>
                    </group>
                    <group id="g2">
                        <child id="c3"/>
                    </group>
                </root>
                """);
        assert selectWithPermutation(xml, 3, "group child", ".value");
        assert selectWithPermutation(xml, 2, "group:first-child child", ".value");
    }

    @Test
    public void siblingSelectors() {
        XML xml = I.xml("""
                <root>
                    <a id="a1"/>
                    <b id="b1"/>
                    <c id="c1"/>
                    <d id="d1"/>
                </root>
                """);

        assert selectWithPermutation(xml, 4, "a", "b", "c", "d"); // a1, b1, c1, d1
        assert selectWithPermutation(xml, 2, "a", "d"); // a1, d1
        assert selectWithPermutation(xml, 1, "c"); // c1
    }

    // === 追加のテストケース ===

    @Test
    public void noMatches() {
        XML xml = I.xml("<root><a/></root>");
        assert selectWithPermutation(xml, 0, "b", "c");
    }

    @Test
    public void overlappingSelectors() {
        XML xml = I.xml("""
                <root>
                    <div class="item featured" id="d1"/>
                    <span class="item" id="s1"/>
                    <div class="item" id="d2"/>
                </root>
                """);
        assert selectWithPermutation(xml, 3, ".item", ".featured");
        assert selectWithPermutation(xml, 2, "div.item", ".featured");
        assert selectWithPermutation(xml, 3, ".item", "div");
    }

    @Test
    public void idAndOtherSelectors() {
        XML xml = I.xml("""
                <root>
                    <div id="uniqueName" class="box">Content</div>
                    <span class="box">Other</span>
                    <div id="anotherId">Another</div>
                </root>
                """);
        assert selectWithPermutation(xml, 2, "#uniqueName", ".box");
        assert selectWithPermutation(xml, 3, "#uniqueName", ".box", "#anotherId");
    }

    @Test
    public void attributeExistenceAndValueSelectors() {
        XML xml = I.xml("""
                <list>
                    <item data-id="1" lang="en" id="i1"/>
                    <item lang="fr" id="i2"/>
                    <item data-id="2" id="i3"/>
                    <item id="i4"/>
                </list>
                """);
        assert selectWithPermutation(xml, 2, "[data-id]", "[lang='en']");
        assert selectWithPermutation(xml, 4, "[data-id]", "[lang]", "item:last-child");
    }

    @Test
    public void pseudoClassNthChildAndOthers() {
        XML xml = I.xml("""
                <ul>
                    <li class="odd" id="li1">1</li>
                    <li class="even" id="li2">2</li>
                    <li class="odd" id="li3">3</li>
                    <li class="even" id="li4">4</li>
                </ul>
                """);
        assert selectWithPermutation(xml, 4, "li:nth-child(even)", ".odd");
    }

    @Test
    public void universalSelectorInMultiple() {
        XML xml = I.xml("""
                <root>
                    <section>
                        <h1 id="h1_s"/>
                        <p id="p1_s"/>
                    </section>
                    <div id="d1_r">
                        <h1 id="h1_d"/>
                        <span/>
                    </div>
                </root>
                """);

        assert selectWithPermutation(xml, 3, "section > *:first-child", "div > *:first-child", "p");
        assert selectWithPermutation(xml, 4, "*:first-child", "p");
    }

    @Test
    public void emptyAndNonEmptySelectors() {
        XML xml = I.xml("""
                <container>
                    <empty1/>
                    <full1><child/></full1>
                    <empty2 class="foo"/>
                </container>
                """);
        assert selectWithPermutation(xml, 3, ":empty", ".foo");
    }

    @Test
    public void redundantSelectors() {
        XML xml = I.xml("""
                <root>
                    <a id="a1"/>
                    <b id="b1"/>
                </root>
                """);
        assert selectWithPermutation(xml, 1, "a", "a:first-child");
        assert selectWithPermutation(xml, 2, "a", "b", "a");
    }

    @Test
    public void selectorsWithSpacesAndCommas() {
        XML xml = I.xml("""
                <root>
                    <div class="c1 c2" id="d1">
                        <span class="c3" id="s1"></span>
                    </div>
                    <p class="c1" id="p1"></p>
                </root>
                """);
        assert selectWithPermutation(xml, 3, "div .c3", ".c1");
        assert selectWithPermutation(xml, 2, "div.c1", "span.c3");
    }
}