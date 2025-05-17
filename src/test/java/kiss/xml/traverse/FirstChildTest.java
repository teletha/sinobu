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

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class FirstChildTest {

    /**
     * @see XML#firstChild()
     */
    @Test
    public void firstChild() {
        // traverse to first child element
        XML root1 = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root1.firstChild().name().equals("first");

        // skip text node
        XML root2 = I.xml("""
                <root>
                    text is ignored
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root2.firstChild().name().equals("first");

        // can't traverse
        XML root3 = I.xml("<root/>");
        assert root3.firstChild().size() == 0;
    }

    @Test
    public void firstChildFromSingleParent() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """);

        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildIgnoringLeadingTextAndCommentNodes() {
        XML root = I.xml("""
                <root>
                    text node
                    <!-- comment node -->
                    <child1/>
                    <child2/>
                </root>
                """);

        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildWhenFirstIsElement() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    text node
                    <child2/>
                </root>
                """);

        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildFromParentWithNoElementChildren() {
        XML root = I.xml("""
                <root>
                    only text
                    <!-- and comment -->
                </root>
                """);

        XML first = root.firstChild();
        assert first.size() == 0;
    }

    @Test
    public void firstChildFromParentWithNoChildrenAtAll() {
        XML root = I.xml("<root/>");

        XML first = root.firstChild();
        assert first.size() == 0;
    }

    @Test
    public void firstChildFromMultipleParents() {
        XML doc = I.xml("""
                <doc>
                    <parent1>text<c1a/><c1b/></parent1>
                    <parent2><!-- comment --><c2a/><c2b/></parent2>
                </doc>
                """);
        XML parents = doc.find("parent1, parent2");
        assert parents.size() == 2;

        XML firstChildren = parents.firstChild();
        assert firstChildren.size() == 2;

        List<String> firstChildNames = I.signal(firstChildren).map(XML::name).toList();
        assert firstChildNames.contains("c1a");
        assert firstChildNames.contains("c2a");
    }

    @Test
    public void firstChildFromMultipleParentsSomeWithNoElementChildren() {
        XML doc = I.xml("""
                <doc>
                    <parent1><c1a/></parent1>
                    <parent2>text only</parent2>
                    <parent3><!-- comment --><c3a/></parent3>
                    <parent4/>
                </doc>
                """);
        XML parents = doc.find("parent1, parent2, parent3, parent4");
        assert parents.size() == 4;

        XML firstChildren = parents.firstChild();
        assert firstChildren.size() == 2;

        List<String> firstChildNames = I.signal(firstChildren).map(XML::name).toList();
        assert firstChildNames.contains("c1a");
        assert firstChildNames.contains("c3a");
        assert firstChildNames.size() == 2; // Ensure only these two are found
    }

    @Test
    public void firstChildAtEmptySet() {
        XML xml = I.xml("<root/>");
        XML emptySet = xml.find(".nonexistent");
        assert emptySet.size() == 0;

        XML first = emptySet.firstChild();
        assert first.size() == 0;
    }

    @Test
    public void firstChildWithOnlyWhitespaceBeforeElement() {
        XML root = I.xml("""
                <root>
                    \s\s
                    \t
                    <child/>
                </root>
                """);
        assert root.firstChild().name().equals("child");
    }

    @Test
    public void firstChildOnElementWithAttributesOnly() {
        XML root = I.xml("<root attr='value'/>");
        assert root.firstChild().size() == 0;
    }

    @Test
    public void firstChildIgnoringProcessingInstruction() {
        XML root = I.xml("""
                <root>
                    <?processing instruction?>
                    <child1/>
                    <child2/>
                </root>
                """);
        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildIgnoringCDataSection() {
        XML root = I.xml("""
                <root>
                    <![CDATA[some cdata content]]>
                    <child1/>
                    <child2/>
                </root>
                """);
        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildSkipsMixedContentIncludingPIAndCData() {
        XML root = I.xml("""
                <root>
                    Text Before
                    <!-- Comment Before -->
                    <?pi before?>
                    <![CDATA[CDATA before]]>
                    <actualFirstChild/>
                    <anotherChild/>
                </root>
                """);
        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("actualFirstChild");
    }
}