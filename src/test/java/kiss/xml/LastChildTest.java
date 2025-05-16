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

import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class LastChildTest {

    /**
     * @see XML#lastChild()
     */
    @Test
    public void lastChild() {
        XML root1 = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root1.lastChild().name().equals("last");

        XML root2 = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                    text is ignored
                </root>
                """);
        assert root2.lastChild().name().equals("last");

        XML root3 = I.xml("<root/>");
        assert root3.lastChild().size() == 0;
    }

    @Test
    public void lastChildFromSingleParent() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """);

        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child3");
    }

    @Test
    public void lastChildIgnoringTrailingTextAndCommentNodes() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                    text node
                    <!-- comment node -->
                </root>
                """);

        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    public void lastChildWhenLastIsElement() {
        XML root = I.xml("""
                <root>
                    text node
                    <child1/>
                    <child2/>
                </root>
                """);

        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    public void lastChildFromParentWithNoElementChildren() {
        XML root = I.xml("""
                <root>
                    only text
                    <!-- and comment -->
                </root>
                """);

        XML last = root.lastChild();
        assert last.size() == 0;
    }

    @Test
    public void lastChildFromParentWithNoChildrenAtAll() {
        XML root = I.xml("<root/>");

        XML last = root.lastChild();
        assert last.size() == 0;
    }

    @Test
    public void lastChildFromMultipleParents() {
        XML doc = I.xml("""
                <doc>
                    <parent1>
                        <c1a/>
                        <c1b/>
                        text
                    </parent1>
                    <parent2>
                        <!-- comment -->
                        <c2a/>
                        <c2b/>
                    </parent2>
                </doc>
                """);
        XML parents = doc.find("parent1, parent2");
        assert parents.size() == 2;

        XML lastChildren = parents.lastChild();
        assert lastChildren.size() == 2;

        List<String> lastChildNames = I.signal(lastChildren).map(XML::name).toList();
        assert lastChildNames.contains("c1b");
        assert lastChildNames.contains("c2b");
    }

    @Test
    public void lastChildFromMultipleParentsSomeWithNoElementChildren() {
        XML doc = I.xml("""
                <doc>
                    <parent1><c1a/></parent1>
                    <parent2>text only</parent2>
                    <parent3>
                        <!-- comment -->
                        <c3a/>
                    </parent3>
                    <parent4/>
                </doc>
                """);
        XML parents = doc.find("parent1, parent2, parent3, parent4");
        assert parents.size() == 4;

        XML lastChildren = parents.lastChild();
        assert lastChildren.size() == 2;

        List<String> lastChildNames = I.signal(lastChildren).map(XML::name).toList();
        assert lastChildNames.contains("c1a");
        assert lastChildNames.contains("c3a");
        assert lastChildNames.size() == 2;
    }

    @Test
    public void lastChildAtEmptySet() {
        XML xml = I.xml("<root/>");
        XML emptySet = xml.find(".nonexistent");
        assert emptySet.size() == 0;

        XML last = emptySet.lastChild();
        assert last.size() == 0;
    }

    @Test
    public void lastChildWithOnlyWhitespaceAfterElement() {
        XML root = I.xml("""
                <root>
                    <child/>
                    \s\s
                    \t
                </root>
                """);
        assert root.lastChild().name().equals("child");
    }

    @Test
    public void lastChildOnElementWithAttributesOnly() {
        XML root = I.xml("<root attr='value'/>");
        assert root.lastChild().size() == 0;
    }

    @Test
    public void lastChildIgnoringProcessingInstructionAfter() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                    <?processing instruction?>
                </root>
                """);
        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    public void lastChildIgnoringCDataSectionAfter() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                    <![CDATA[some cdata content]]>
                </root>
                """);
        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    public void lastChildSkipsMixedContentAfterIncludingPIAndCData() {
        XML root = I.xml("""
                <root>
                    <anotherChild/>
                    <actualLastChild/>
                    Text After
                    <!-- Comment After -->
                    <?pi after?>
                    <![CDATA[CDATA after]]>
                </root>
                """);
        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("actualLastChild");
    }
}