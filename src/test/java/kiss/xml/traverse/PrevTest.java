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

public class PrevTest {

    /**
     * @see XML#prev()
     */
    @Test
    public void prev() {
        XML root = I.xml("""
                <root>
                    <first/>
                    text is ignored
                    <center/>
                    <last/>
                </root>
                """);

        XML prev1 = root.find("last").prev();
        assert prev1.name().equals("center");

        XML prev2 = root.find("center").prev();
        assert prev2.name().equals("first");

        XML prev3 = root.find("first").prev();
        assert prev3.size() == 0;
    }

    @Test
    public void prevFromSingleElement() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    <target/>
                    <sibling2/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevIgnoringIntermediateTextNodes() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    text node
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevIgnoringIntermediateCommentNodes() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    <!-- comment node -->
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevIgnoringMultipleIntermediateNonElementNodes() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    text1
                    <!-- comment1 -->
                    <?pi instruction?>
                    text2
                    <!-- comment2 -->
                    <![CDATA[cdata]]>
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevWhenImmediatePrevIsElement() {
        XML xml = I.xml("""
                <root>
                    <sibling1/>
                    <target/>
                    text node
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevWhenNoPrevSiblingElementOnlyNonElement() {
        XML xml = I.xml("""
                <root>
                    text node
                    <!-- comment node -->
                    <?pi instruction?>
                    <![CDATA[cdata]]>
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevWhenNoPrevSiblingAtAllAsFirstChild() {
        XML xml = I.xml("""
                <root>
                    <target/>
                    <sibling2/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevWhenOnlyChild() {
        XML xml = I.xml("""
                <root>
                    <target/>
                </root>
                """);
        XML target = xml.find("target");

        XML prev = target.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevFromMultipleElementsEachHavingPrev() {
        XML xml = I.xml("""
                <doc>
                    <section>
                        <s1a class='prev-marker'/>
                        <t1 class='target'/>
                        <s1b/>
                        <t1_ignored/>
                    </section>
                    <section>
                        <s2a class='prev-marker'/>
                        <t2 class='target'/>
                        <s2b/>
                        <t2_ignored/>
                    </section>
                </doc>
                """);
        XML targets = xml.find(".target");
        assert targets.size() == 2;

        XML prevs = targets.prev();
        assert prevs.size() == 2;

        List<XML> prevList = I.signal(prevs).toList();
        List<String> prevNames = prevList.stream().map(XML::name).collect(Collectors.toList());
        assert prevNames.contains("s1a");
        assert prevNames.contains("s2a");
        assert prevList.stream().allMatch(x -> x.hasClass("prev-marker"));
    }

    @Test
    public void prevFromMultipleElementsSomeWithNoPrev() {
        XML xml = I.xml("""
                <doc>
                    <section>
                        <t1 class='target'/>
                    </section>
                    <section>
                        <s2a class='prev-marker'/>
                        <t2 class='target'/>
                    </section>
                    <section>
                        text node
                        <s3a class='prev-marker'/>
                        <!-- comment -->
                        <t3 class='target'/>
                    </section>
                    <section>
                        <!-- only comment -->
                        <t4 class='target'/>
                    </section>
                    <section>
                        <s5a class='prev-marker-not-direct'/>
                        <another/>
                        <t5 class='target'/>
                    </section>
                </doc>
                """);
        XML targets = xml.find(".target");
        assert targets.size() == 5;

        XML prevs = targets.prev();
        assert prevs.size() == 3;
    }

    @Test
    public void prevAtEmptySet() {
        XML xml = I.xml("<root/>");
        XML emptySet = xml.find(".nonexistent");
        assert emptySet.size() == 0;

        XML prev = emptySet.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevWithinDifferentParentsNotInterfering() {
        XML xml = I.xml("""
                <doc>
                    <outer_sibling class='prev-marker-outer'/>
                    <parent1>
                        <s1a class='prev-marker-inner'/>
                        text
                        <t1 class='target'/>
                    </parent1>
                    <parent2>
                        <!-- comment -->
                        <t2 class='target'/>
                        <s2_after/>
                    </parent2>
                </doc>
                """);
        XML targets = xml.find(".target");
        assert targets.size() == 2;

        XML prevs = targets.prev();

        assert prevs.size() == 1;
        assert prevs.hasClass("prev-marker-inner");
        assert prevs.name().equals("s1a");
    }
}