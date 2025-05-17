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

public class FirstChildTypeTest {

    @Test
    public void firstChildBasic() {
        XML xml = I.xml("<m><Q id='q1'/><Q id='q2'/><R id='r1'/></m>");

        assert select(xml, 1, "Q#q1:first-child");
        assert select(xml, 0, "Q#q2:first-child");
        assert select(xml, 0, "R#r1:first-child");
        assert select(xml, 1, "*#q1:first-child"); // ID selector before pseudo-class
    }

    @Test
    public void firstChildComplex() {
        XML xml = I.xml("""
                <root>
                    text1
                    <childA id='ca1'>
                        <grandchildA1 id='gca1'/>
                        textInA
                        <grandchildA2 id='gca2'/>
                    </childA>
                    <!-- comment -->
                    <childB id='cb1'>
                        textInB
                        <!-- commentInB -->
                        <grandchildB1 id='gcb1'/>
                    </childB>
                    <childA id='ca2'/>
                </root>
                """);

        assert select(xml, 1, "childA#ca1:first-child");
        assert select(xml, 0, "childB#cb1:first-child");
        assert select(xml, 0, "childA#ca2:first-child");
        assert select(xml, 1, "*[id='ca1']:first-child"); // Attribute selector before
                                                          // pseudo-class

        XML childA_ca1 = xml.find("#ca1");
        assert select(childA_ca1, 1, "grandchildA1#gca1:first-child");
        assert select(childA_ca1, 0, "grandchildA2#gca2:first-child");
        assert select(childA_ca1, 1, "*[id='gca1']:first-child");

        XML childB_cb1 = xml.find("#cb1");
        assert select(childB_cb1, 1, "grandchildB1#gcb1:first-child");
        assert select(childB_cb1, 1, "*[id='gcb1']:first-child");
    }

    @Test
    public void firstChildWithOnlyOneChild() {
        XML xml = I.xml("<m><Q id='q1'/></m>");
        assert select(xml, 1, "Q#q1:first-child");
    }

    @Test
    public void firstChildNoElementChildren() {
        XML xml = I.xml("<m>text <!-- comment --></m>");
        assert select(xml, 0, "*:first-child"); // Pseudo-class is already at the end
    }

    @Test
    public void firstChildCombinedWithOtherSelectors() {
        XML xml = I.xml("""
                <m>
                    <div class='item' id='d1'/>
                    <span class='item' id='s1'/>
                    <div class='item first' id='d2'/>
                </m>
                """);
        assert select(xml, 1, "div.item#d1:first-child");
        assert select(xml, 0, "span.item:first-child"); // Type, class, then pseudo-class
        assert select(xml, 0, "div.first:first-child"); // Type, class, then pseudo-class
    }

    @Test
    public void firstOfTypeBasic() {
        XML xml = I.xml("<m><Q id='q1'/><Q id='q2'/><R id='r1'/><P id='p1'/><Q id='q3'/><R id='r2'/></m>");

        assert select(xml, 1, "Q#q1:first-of-type");
        assert select(xml, 0, "Q#q2:first-of-type");
        assert select(xml, 0, "Q#q3:first-of-type");

        assert select(xml, 1, "R#r1:first-of-type");
        assert select(xml, 0, "R#r2:first-of-type");

        assert select(xml, 1, "P#p1:first-of-type");
    }

    @Test
    public void firstOfTypeComplex() {
        XML xml = I.xml("""
                <root>
                    <typeA id='a1'/>
                    text
                    <typeB id='b1'/>
                    <typeA id='a2'>
                        <typeC id='c1'/>
                        <typeA id='a3'/>
                        <typeC id='c2'/>
                        <typeB id='b3'/>
                    </typeA>
                    <!-- comment -->
                    <typeB id='b2'/>
                    <typeC id='c3'/>
                </root>
                """);

        assert select(xml, 1, "typeA#a1:first-of-type");
        assert select(xml, 0, "typeA#a2:first-of-type");
        assert select(xml, 1, "typeB#b1:first-of-type");
        assert select(xml, 0, "typeB#b2:first-of-type");
        assert select(xml, 1, "typeC#c3:first-of-type");

        XML typeA_a2 = xml.find("#a2");
        assert select(typeA_a2, 1, "typeC#c1:first-of-type");
        assert select(typeA_a2, 0, "typeC#c2:first-of-type");
        assert select(typeA_a2, 1, "typeA#a3:first-of-type");
        assert select(typeA_a2, 1, "typeB#b3:first-of-type");
    }

    @Test
    public void firstOfTypeWithUniversalSelector() {
        XML xml = I.xml("""
                <root>
                    <elemP id='p1'/>
                    <elemQ id='q1'/>
                    <elemP id='p2'/>
                    <elemR id='r1'/>
                    <elemQ id='q2'/>
                </root>
                """);
        // assert validateSelector(xml, 3, "*:first-of-type");
        assert select(xml, 1, "*#p1:first-of-type");
        assert select(xml, 1, "*#q1:first-of-type");
        assert select(xml, 1, "*#r1:first-of-type");
        assert select(xml, 0, "*#p2:first-of-type");
        assert select(xml, 0, "*#q2:first-of-type");
    }

    @Test
    public void firstOfTypeCombinedWithOtherSelectors() {
        XML xml = I.xml("""
                <m>
                    <div class='item' id='d1'/>
                    <span class='item first-span' id='s1'/>
                    <div class='item another' id='d2'/>
                    <span class='item' id='s2'/>
                    <div class='other' id='d3'/>
                </m>
                """);
        assert select(xml, 1, "div.item#d1:first-of-type");
        assert select(xml, 0, "div.item#d2:first-of-type");

        assert select(xml, 1, "span.item#s1:first-of-type");
        assert select(xml, 0, "span.item#s2:first-of-type");

        assert select(xml, 0, "div.other#d3:first-of-type");
    }

    @Test
    public void firstChildWithNoMatchingType() {
        XML xml = I.xml("<m><P/><Q id='q1'/></m>");
        assert select(xml, 0, "Q:first-child");
    }

    @Test
    public void firstOfTypeWithNoMatchingType() {
        XML xml = I.xml("<m><P/><R/></m>");
        assert select(xml, 0, "Q:first-of-type");
    }

    @Test
    public void firstChildIgnoresTextNodesAndComments() {
        XML xml = I.xml("<m>text<!--comment--><Q id='q1'/><R/></m>");
        assert select(xml, 1, "Q#q1:first-child");
        assert select(xml, 0, "R:first-child");
    }

    @Test
    public void firstOfTypeIgnoresTextNodesAndComments() {
        XML xml = I.xml("<m><P id='p1'/>text<!--comment--><Q id='q1'/><P id='p2'/><R/></m>");
        assert select(xml, 1, "Q#q1:first-of-type");
        assert select(xml, 1, "P#p1:first-child");
        assert select(xml, 1, "P#p1:first-of-type");
    }
}