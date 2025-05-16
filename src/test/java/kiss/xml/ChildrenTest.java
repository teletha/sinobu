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

public class ChildrenTest {

    /**
     * @see XML#children()
     */
    @Test
    public void children() {
        XML root1 = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root1.children().size() == 3;

        XML root2 = I.xml("""
                <root>
                    text<first/>is
                    <child>
                        <center/>
                    </child>
                    ignored<last/>!!
                </root>
                """);
        assert root2.children().size() == 3;

        XML root3 = I.xml("<root/>");
        assert root3.children().size() == 0;
    }

    @Test
    public void childrenFromSingleParent() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """);

        XML children = root.children();
        assert children.size() == 3;

        List<XML> childList = I.signal(children).toList();
        assert childList.get(0).name().equals("child1");
        assert childList.get(1).name().equals("child2");
        assert childList.get(2).name().equals("child3");
    }

    @Test
    public void childrenIgnoringTextAndCommentNodes() {
        XML root = I.xml("""
                <root>
                    text node
                    <child1/>
                    <!-- comment node -->
                    <child2/>
                    more text
                    <?processing instruction?>
                    <child3/>
                    <![CDATA[cdata section]]>
                </root>
                """);

        XML children = root.children();
        assert children.size() == 3;

        List<XML> childList = I.signal(children).toList();
        assert childList.get(0).name().equals("child1");
        assert childList.get(1).name().equals("child2");
        assert childList.get(2).name().equals("child3");
    }

    @Test
    public void childrenIgnoringGrandchildren() {
        XML root = I.xml("""
                <root>
                    <child1>
                        <grandchild1/>
                    </child1>
                    <child2/>
                </root>
                """);

        XML children = root.children();
        assert children.size() == 2;

        List<XML> childList = I.signal(children).toList();
        assert childList.get(0).name().equals("child1");
        assert childList.get(1).name().equals("child2");
    }

    @Test
    public void childrenFromParentWithNoElementChildren() {
        XML root = I.xml("<root>only text <!-- and comment --></root>");

        XML children = root.children();
        assert children.size() == 0;
    }

    @Test
    public void childrenFromParentWithNoChildrenAtAll() {
        XML root = I.xml("<root/>");

        XML children = root.children();
        assert children.size() == 0;
    }

    @Test
    public void childrenFromMultipleParents() {
        XML doc = I.xml("""
                <doc>
                    <parent1>
                        <c1a/>
                        <c1b/>
                    </parent1>
                    <parent2>
                        <c2a/>
                        <c2b/>
                    </parent2>
                </doc>
                """);
        XML parents = doc.find("parent1, parent2");
        assert parents.size() == 2;

        XML children = parents.children();
        assert children.size() == 4;

        List<String> names = I.signal(children).map(XML::name).toList();
        assert names.contains("c1a");
        assert names.contains("c1b");
        assert names.contains("c2a");
        assert names.contains("c2b");
        assert names.indexOf("c1a") < names.indexOf("c1b");
        assert names.indexOf("c2a") < names.indexOf("c2b");
        assert names.indexOf("c1b") < names.indexOf("c2a") || names.indexOf("c1b") < names.indexOf("c2b"); // Order
                                                                                                           // between
                                                                                                           // parent
                                                                                                           // blocks
                                                                                                           // matters
                                                                                                           // for
                                                                                                           // test
                                                                                                           // stability
    }

    @Test
    public void childrenFromMultipleParentsSomeWithNoChildren() {
        XML doc = I.xml("""
                <doc>
                    <parent1>
                        <c1a/>
                    </parent1>
                    <parent2/>
                    <parent3>
                        <c3a/>
                        text
                    </parent3>
                    <parent4>
                        <!-- comment -->
                    </parent4>
                </doc>
                """);
        XML parents = doc.find("parent1, parent2, parent3, parent4");
        assert parents.size() == 4;

        XML children = parents.children();
        assert children.size() == 2;

        List<String> names = I.signal(children).map(XML::name).toList();
        assert names.contains("c1a");
        assert names.contains("c3a");
        assert names.size() == 2;
    }

    @Test
    public void childrenAtEmptySet() {
        XML xml = I.xml("<root/>");
        XML emptySet = xml.find(".nonexistent");
        assert emptySet.size() == 0;

        XML children = emptySet.children();
        assert children.size() == 0;
    }

    @Test
    public void childrenFromSelfClosingElementAreEmpty() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2 attr="val"/>
                    <child3></child3>
                </root>
                """);
        XML childrenOfRoot = root.children();
        for (XML child : childrenOfRoot) {
            assert child.children().size() == 0;
        }
    }

    @Test
    public void childrenOfElementsContainingOnlyTextOrCDataAreEmpty() {
        XML root = I.xml("""
                <root>
                    <child1><![CDATA[some <xml> content]]></child1>
                    <child2>plain text</child2>
                </root>
                """);
        XML childrenOfRoot = root.children();
        assert childrenOfRoot.size() == 2;

        for (XML child : childrenOfRoot) {
            assert child.children().size() == 0;
        }
    }

    @Test
    public void childrenWithWhitespaceOnlyTextNodesBetweenThem() {
        XML root = I.xml("""
                <root>
                    <childA/>
                    \s\s
                    \t
                    <childB/>

                </root>
                """);
        XML children = root.children();
        assert children.size() == 2;
        assert children.first().name().equals("childA");
        assert children.last().name().equals("childB");
    }

    @Test
    public void childrenWithAttributes() {
        XML root = I.xml("""
                <root>
                    <child name="a"/>
                    <child name="b"/>
                    <child name="c"/>
                </root>
                """);
        XML children = root.children();
        assert children.size() == 3;

        List<String> names = I.signal(children).map(x -> x.attr("name")).toList();
        assert names.contains("a");
        assert names.contains("b");
        assert names.contains("c");
        assert names.get(0).equals("a");
        assert names.get(1).equals("b");
        assert names.get(2).equals("c");
    }
}