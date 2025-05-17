/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.traverse;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class NextTest {

    /**
     * @see XML#next()
     */
    @Test
    public void next() {
        XML root = I.xml("""
                <root>
                    <first/>
                    <center/>
                    text is ignored
                    <last/>
                </root>
                """);

        XML next1 = root.find("first").next();
        assert next1.name().equals("center");

        XML next2 = root.find("center").next();
        assert next2.name().equals("last");

        XML next3 = root.find("last").next();
        assert next3.size() == 0;
    }

    @Test
    public void nextFromSingleElement() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    <target/>
                    <sibling2 class="next-marker"/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling2");
    }

    @Test
    public void nextIgnoringIntermediateTextNodes() {
        XML xml = I.xml("""
                <root>
                    <target/>
                    text node
                    <sibling1 class="next-marker"/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextIgnoringIntermediateCommentNodes() {
        XML xml = I.xml("""
                <root>
                    <target/>
                    <!-- comment node -->
                    <sibling1 class="next-marker"/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextIgnoringMultipleIntermediateNonElementNodes() {
        XML xml = I.xml("""
                <root>
                    <target/>
                    text1
                    <!-- comment1 -->
                    <?pi instruction?>
                    text2
                    <!-- comment2 -->
                    <![CDATA[cdata]]>
                    <sibling1 class="next-marker"/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextWhenImmediateNextIsElement() {
        XML xml = I.xml("""
                <root>
                    text node
                    <target/>
                    <sibling1 class="next-marker"/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextWhenNoNextSiblingElementOnlyNonElement() {
        XML xml = I.xml("""
                <root>
                    <target/>
                    text node
                    <!-- comment node -->
                    <?pi instruction?>
                    <![CDATA[cdata]]>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 0;
    }

    @Test
    public void nextWhenNoNextSiblingAtAllAsLastChild() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 0;
    }

    @Test
    public void nextWhenOnlyChild() {
        XML xml = I.xml("""
                <root>
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML next = target.next();
        assert next.size() == 0;
    }

    @Test
    public void nextFromMultipleElementsEachHavingNext() {
        XML xml = I.xml("""
                <doc>
                    <section>
                        <t1 class='target'/>
                        <s1a class='next-marker'/>
                        <t1_ignored/>
                    </section>
                    <section>
                        <t2 class='target'/>
                        text
                        <s2a class='next-marker'/>
                    </section>
                </doc>
                """);
        XML targets = xml.find(".target");
        assert targets.size() == 2;

        XML nexts = targets.next();
        assert nexts.size() == 2;

        List<XML> nextList = I.signal(nexts).toList();
        List<String> nextNames = nextList.stream().map(XML::name).collect(Collectors.toList());
        assert nextNames.contains("s1a");
        assert nextNames.contains("s2a");
        assert nextList.stream().allMatch(x -> x.hasClass("next-marker"));
    }

    @Test
    public void nextFromMultipleElementsSomeWithNoNext() {
        XML xml = I.xml("""
                <doc>
                    <section>
                        <t1 class='target'/>
                        <s1a class='next-marker'/>
                    </section>
                    <section>
                        <t2 class='target'/>
                    </section>
                    <section>
                        <t3 class='target'/>
                        <!-- comment -->
                        text node
                        <s3a class='next-marker'/>
                    </section>
                    <section>
                        <t4 class='target'/>
                        <!-- only comment -->
                    </section>
                    <section>
                        <t5 class='target'/>
                        <another/>
                        <s5a class='next-marker-not-direct'/>
                    </section>
                </doc>
                """);
        XML targets = xml.find(".target");
        assert targets.size() == 5;

        XML nexts = targets.next();
        assert nexts.size() == 3; // s1a, s3a, and 'another' for t5

        List<String> nextNames = I.signal(nexts).map(XML::name).toList();
        assert nextNames.contains("s1a");
        assert nextNames.contains("s3a");
        assert nextNames.contains("another");
    }

    @Test
    public void nextAtEmptySet() {
        XML xml = I.xml("<root/>");
        XML emptySet = xml.find(".nonexistent");
        assert emptySet.size() == 0;

        XML next = emptySet.next();
        assert next.size() == 0;
    }

    @Test
    public void nextWithinDifferentParentsNotInterfering() {
        XML xml = I.xml("""
                <doc>
                    <parent1>
                        <t1 class='target'/>
                        <!-- comment -->
                        <s1a class='next-marker-inner'/>
                    </parent1>
                    <parent2>
                        <s2_before/>
                        <t2 class='target'/>
                    </parent2>
                    <outer_sibling class='next-marker-outer'/>
                </doc>
                """);
        XML targets = xml.find(".target");
        assert targets.size() == 2;

        XML nexts = targets.next();
        assert nexts.size() == 1;
        assert nexts.hasClass("next-marker-inner");
        assert nexts.name().equals("s1a");
    }
}