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

public class ParentTest {

    /**
     * @see XML#parent()
     */
    @Test
    public void parent() {
        XML xml1 = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert xml1.find("first").parent().name().equals("root");
        assert xml1.find("center").parent().name().equals("root");
        assert xml1.find("last").parent().name().equals("root");

        XML xml2 = I.xml("<root><child><grand/></child></root>");
        assert xml2.find("grand").parent().name().equals("child");
    }

    /**
     * @see XML#parent()
     */
    @Test
    public void parentFromMultipleChildrenWithSameParent() {
        XML xml = I.xml("""
                <root>
                    <parent1>
                        <child1 class='target'/>
                        <child2 class='target'/>
                    </parent1>
                </root>
                """);
        XML children = xml.find(".target");
        assert children.size() == 2;

        XML parent = children.parent();
        assert parent.size() == 1;
        assert parent.name().equals("parent1");
    }

    /**
     * @see XML#parent()
     */
    @Test
    public void parentFromMultipleChildrenWithDifferentParents() {
        XML xml = I.xml("""
                <root>
                    <parent1>
                        <child1 class='target'/>
                    </parent1>
                    <parent2>
                        <child2 class='target'/>
                    </parent2>
                </root>
                """);
        XML children = xml.find(".target");
        assert children.size() == 2;

        XML parents = children.parent();
        assert parents.size() == 2;
        assert parents.first().name().equals("parent1");
        assert parents.last().name().equals("parent2");
    }

    @Test
    public void parentOfRootElement() {
        XML root = I.xml("<root/>");
        assert root.size() == 1;
        assert root.name().equals("root");

        XML parent = root.parent();
        assert parent.size() == 1;
    }

    @Test
    public void parentAtEmptySet() {
        XML xml = I.xml("<root/>");
        XML emptySet = xml.find(".nonexistent");
        assert emptySet.size() == 0;

        XML parent = emptySet.parent();
        assert parent.size() == 0;
    }

    @Test
    public void parentMixedWithNonElementNodes() {
        XML xml = I.xml("""
                <root>
                    <!-- comment -->
                    <child1>
                        text
                        <grandchild class='target'/>
                    </child1>
                </root>
                """);
        XML grandchild = xml.find(".target");
        assert grandchild.size() == 1;

        XML parent = grandchild.parent();
        assert parent.size() == 1;
        assert parent.name().equals("child1");
    }

    @Test
    public void parentFromDeeplyNestedStructure() {
        XML xml = I.xml("""
                <a>
                    <b>
                        <c>
                            <d>
                                <e class='target'/>
                            </d>
                        </c>
                    </b>
                </a>
                """);
        XML target = xml.find(".target");
        assert target.size() == 1;

        XML parentD = target.parent();
        assert parentD.size() == 1;
        assert parentD.name().equals("d");

        XML parentC = parentD.parent();
        assert parentC.size() == 1;
        assert parentC.name().equals("c");

        XML parentB = parentC.parent();
        assert parentB.size() == 1;
        assert parentB.name().equals("b");

        XML parentA = parentB.parent();
        assert parentA.size() == 1;
        assert parentA.name().equals("a");
    }

    @Test
    public void parentDoesNotAffectOriginal() {
        XML xml = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                </root>
                """);
        XML children = xml.find("*");
        assert children.size() == 2;

        XML parent = children.parent();

        assert children.size() == 2;
        assert parent.size() == 1;
        assert parent.name().equals("root");
    }

    @Test
    public void parentOfTextNodeSelectionIsNoOpDueToNoSelection() {
        XML xml = I.xml("<root>text<child/></root>");
        XML textNodes = xml.find("text()");
        assert textNodes.size() == 0;

        XML parent = textNodes.parent();
        assert parent.size() == 0;
    }

    @Test
    public void parentOfChildrenOfRoot() {
        XML xml = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                </root>
                """);
        XML children = xml.children();
        assert children.size() == 2;

        XML parents = children.parent();
        assert parents.size() == 1;
        assert parents.name().equals("root");
    }

    @Test
    public void parentOfGrandChildrenFromDifferentBranches() {
        XML xml = I.xml("""
                <root>
                    <childA>
                        <grandchild1 class="gc"/>
                        <grandchild2 class="gc"/>
                    </childA>
                    <childB>
                        <grandchild3 class="gc"/>
                    </childB>
                </root>
                """);
        XML grandChildren = xml.find(".gc");
        assert grandChildren.size() == 3;

        XML parents = grandChildren.parent();
        assert parents.size() == 2;
        assert parents.first().name().equals("childA");
        assert parents.last().name().equals("childB");
    }
}