/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class XMLTraversingTest {

    /**
     * @see XML#first()
     */
    @Test
    public void first() {
        String text = """
                <root>
                    <child1 class='a'/>
                    <child2 class='a'/>
                    <child3 class='a'/>
                </root>
                """;
        XML found = I.xml(text).find(".a");
        assert found.size() == 3;

        // traverse to first matched element
        XML first = found.first();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstWhenAlreadySingle() {
        String text = """
                <root>
                    <child1 class='a'/>
                </root>
                """;
        XML found = I.xml(text).find(".a");
        assert found.size() == 1;

        XML first = found.first();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstAtEmpty() {
        XML root = I.xml("<root/>");

        assert root.find("notFound").size() == 0;
        assert root.find("notFound").first().size() == 0;
    }

    // Content to be added to XMLTraversingTest.java

    /**
     * Test {@link XML#first()} on an XML set obtained from {@link XML#children()}.
     * Ensures that the first child element is correctly selected from a list of children.
     */
    @Test
    public void firstOnChildrenResult() {
        String text = """
                <root>
                    <child1 id='c1'/>
                    <child2 id='c2'/>
                    <child3 id='c3'/>
                </root>
                """;
        // Get children of <root>
        XML children = I.xml(text).children();
        assert children.size() == 3;
        // Sanity check: ensure the children are what we expect
        List<String> ids = I.signal(children).map(c -> c.attr("id")).toList();
        assert ids.get(0).equals("c1");
        assert ids.get(1).equals("c2");
        assert ids.get(2).equals("c3");

        XML firstChild = children.first();
        assert firstChild.size() == 1;
        assert firstChild.name().equals("child1");
        assert firstChild.attr("id").equals("c1");
    }

    /**
     * Test {@link XML#first()} on an empty XML set obtained from {@link XML#children()}
     * when the parent element has no element children.
     */
    @Test
    public void firstOnEmptyChildrenResult() {
        String text = "<root></root>"; // The <root> element has no child elements
        XML children = I.xml(text).children();
        assert children.size() == 0;

        XML firstChild = children.first();
        assert firstChild.size() == 0; // Expect an empty XML set
    }

    /**
     * @see XML#last()
     */
    @Test
    public void last() {
        String text = """
                <root>
                    <child1 class='a'/>
                    <child2 class='a'/>
                    <child3 class='a'/>
                </root>
                """;
        XML found = I.xml(text).find(".a");
        assert found.size() == 3;

        // traverse to last matched element
        XML last = found.last();
        assert last.size() == 1;
        assert last.name().equals("child3");
    }

    @Test
    public void lastWhenAlreadySingle() {
        String text = """
                <root>
                    <child1 class='a'/>
                </root>
                """;
        XML found = I.xml(text).find(".a");
        assert found.size() == 1;

        XML last = found.last();
        assert last.size() == 1;
        assert last.name().equals("child1");
    }

    @Test
    public void lastAtEmpty() {
        XML root = I.xml("<root/>");

        assert root.find("notFound").size() == 0;
        assert root.find("notFound").last().size() == 0;
    }

    // Content to be added to XMLTraversingTest.java

    /**
     * Test {@link XML#last()} on an XML set obtained from {@link XML#children()}.
     * Ensures that the last child element is correctly selected from a list of children.
     */
    @Test
    public void lastOnChildrenResult() {
        String text = """
                <root>
                    <child1 id='c1'/>
                    <child2 id='c2'/>
                    <child3 id='c3'/>
                </root>
                """;
        // Get children of <root>
        XML children = I.xml(text).children();
        assert children.size() == 3;
        // Sanity check: ensure the children are what we expect
        List<String> ids = I.signal(children).map(c -> c.attr("id")).toList();
        assert ids.get(0).equals("c1");
        assert ids.get(1).equals("c2");
        assert ids.get(2).equals("c3");

        XML lastChild = children.last();
        assert lastChild.size() == 1;
        assert lastChild.name().equals("child3");
        assert lastChild.attr("id").equals("c3");
    }

    /**
     * Test {@link XML#last()} on an empty XML set obtained from {@link XML#children()}
     * when the parent element has no element children (e.g., only text nodes or comments).
     */
    @Test
    public void lastOnEmptyChildrenResult() {
        String text = "<root>  <!-- This is a comment -->  Some text  </root>"; // No element
                                                                                // children
        XML children = I.xml(text).children();
        assert children.size() == 0;

        XML lastChild = children.last();
        assert lastChild.size() == 0; // Expect an empty XML set
    }

    /**
     * @see XML#parent()
     */
    @Test
    public void parent() {
        // traverse to parent element
        XML root = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root.find("first").parent().name() == "root";
        assert root.find("center").parent().name() == "root";
        assert root.find("last").parent().name() == "root";

        // traverse to parent element from nested element
        root = I.xml("<root><child><grand/></child></root>");
        assert root.find("grand").parent().name() == "child";
    }

    /**
     * @see XML#parent()
     */
    @Test
    public void parentFromMultipleChildrenWithSameParent() {
        String text = """
                <root>
                    <parent1>
                        <child1 class='target'/>
                        <child2 class='target'/>
                    </parent1>
                </root>
                """;
        XML children = I.xml(text).find(".target");
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
        String text = """
                <root>
                    <parent1>
                        <child1 class='target'/>
                    </parent1>
                    <parent2>
                        <child2 class='target'/>
                    </parent2>
                </root>
                """;
        XML children = I.xml(text).find(".target");
        assert children.size() == 2;

        XML parents = children.parent();
        assert parents.size() == 2;
        assert parents.first().name() == "parent1";
        assert parents.last().name() == "parent2";
    }

    @Test
    public void parentOfRootElement() {
        XML root = I.xml("<root/>");
        assert root.size() == 1;

        XML parent = root.parent();
        assert parent.size() == 1;
    }

    @Test
    public void parentAtEmptySet() {
        String text = "<root/>";
        XML emptySet = I.xml(text).find(".nonexistent");
        assert emptySet.size() == 0;

        XML parent = emptySet.parent();
        assert parent.size() == 0;
    }

    @Test
    public void parentMixedWithNonElementNodes() {
        String text = """
                <root>
                    <!-- comment -->
                    <child1>
                        text
                        <grandchild class='target'/>
                    </child1>
                </root>
                """;
        XML grandchild = I.xml(text).find(".target");
        assert grandchild.size() == 1;

        XML parent = grandchild.parent();
        assert parent.size() == 1;
        assert parent.name().equals("child1");
    }

    /**
     * @see XML#children()
     */
    @Test
    public void children() {
        // traverse to child elements
        XML root = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root.children().size() == 3;

        // skip text node
        root = I.xml("""
                <root>
                    text<first/>is
                    <child>
                        <center/>
                    </child>
                    ignored<last/>!!
                </root>
                """);
        assert root.children().size() == 3;

        // can't traverse
        root = I.xml("<root/>");
        assert root.children().size() == 0;
    }

    @Test
    public void childrenFromSingleParent() {
        String text = """
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """;
        XML root = I.xml(text);

        XML children = root.children();
        assert children.size() == 3;

        List<XML> childList = I.signal(children).toList();
        assert childList.get(0).name().equals("child1");
        assert childList.get(1).name().equals("child2");
        assert childList.get(2).name().equals("child3");
    }

    @Test
    public void childrenIgnoringTextAndCommentNodes() {
        String text = """
                <root>
                    text node
                    <child1/>
                    <!-- comment node -->
                    <child2/>
                    more text
                    <child3/>
                </root>
                """;
        XML root = I.xml(text);

        XML children = root.children();
        assert children.size() == 3;

        List<XML> childList = I.signal(children).toList();
        assert childList.get(0).name().equals("child1");
        assert childList.get(1).name().equals("child2");
        assert childList.get(2).name().equals("child3");
    }

    @Test
    public void childrenIgnoringGrandchildren() {
        String text = """
                <root>
                    <child1>
                        <grandchild1/>
                    </child1>
                    <child2/>
                </root>
                """;
        XML root = I.xml(text);

        XML children = root.children();
        assert children.size() == 2;

        List<XML> childList = I.signal(children).toList();
        assert childList.get(0).name().equals("child1");
        assert childList.get(1).name().equals("child2");
    }

    @Test
    public void childrenFromParentWithNoElementChildren() {
        String text = "<root>only text <!-- and comment --></root>";
        XML root = I.xml(text);

        XML children = root.children();
        assert children.size() == 0;
    }

    @Test
    public void childrenFromParentWithNoChildrenAtAll() {
        String text = "<root/>";
        XML root = I.xml(text);

        XML children = root.children();
        assert children.size() == 0;
    }

    @Test
    public void childrenFromMultipleParents() {
        String text = """
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
                """;
        XML parents = I.xml(text).find("parent1, parent2");
        assert parents.size() == 2;

        XML children = parents.children();
        assert children.size() == 4;

        List<String> names = I.signal(children).map(XML::name).toList();
        assert names.contains("c1a");
        assert names.contains("c1b");
        assert names.contains("c2a");
        assert names.contains("c2b");
    }

    @Test
    public void childrenFromMultipleParentsSomeWithNoChildren() {
        String text = """
                <doc>
                    <parent1>
                        <c1a/>
                    </parent1>
                    <parent2/>
                    <parent3>
                        <c3a/>
                    </parent3>
                </doc>
                """;
        XML parents = I.xml(text).find("parent1, parent2, parent3");
        assert parents.size() == 3;

        XML children = parents.children();
        assert children.size() == 2;

        List<String> names = I.signal(children).map(XML::name).toList();
        assert names.contains("c1a");
        assert names.contains("c3a");
        assert names.size() == 2;
    }

    @Test
    public void childrenAtEmptySet() {
        String text = "<root/>";
        XML emptySet = I.xml(text).find(".nonexistent");
        assert emptySet.size() == 0;

        XML children = emptySet.children();
        assert children.size() == 0;
    }

    /**
     * @see XML#element(String)
     */
    @Test
    public void elementFromSingleParent() {
        String text = """
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """;
        XML root = I.xml(text);

        XML c2 = root.element("child2");
        assert c2.size() == 1;
        assert c2.name().equals("child2");
    }

    @Test
    public void elementFromMultipleParents() {
        String text = """
                <doc>
                    <parent1>
                        <item/>
                        <shared/>
                    </parent1>
                    <parent2>
                        <item/>
                        <shared/>
                    </parent2>
                </doc>
                """;
        XML parents = I.xml(text).find("parent1, parent2");
        assert parents.size() == 2;

        XML items = parents.element("item");
        assert items.size() == 2;

        List<String> names = I.signal(items).map(XML::name).toList();
        assert names.equals(List.of("item", "item"));
    }

    @Test
    public void elementWithNoMatchingTags() {
        XML root = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                </root>
                """);

        XML result = root.element("nonexistent");
        assert result.size() == 0;
    }

    @Test
    public void elementWithNestedDescendants() {
        XML root = I.xml("""
                <root>
                    <a>
                        <b>
                            <target/>
                        </b>
                    </a>
                </root>
                """);

        XML targets = root.element("target");
        assert targets.size() == 1;
        assert targets.name().equals("target");
    }

    @Test
    public void elementAvoidsDuplicates() {
        XML root = I.xml("""
                <root>
                    <group>
                        <item id="1"/>
                    </group>
                    <group>
                        <item id="1"/>
                    </group>
                </root>
                """);

        XML items = root.element("item");
        assert items.size() == 2;
    }

    @Test
    public void elementWithWildcard() {
        XML root = I.xml("""
                <root>
                    <a/><b/><c/>
                </root>
                """);

        XML all = root.element("*");
        assert all.size() == 3;

        List<String> names = I.signal(all).map(XML::name).toList();
        assert names.equals(List.of("a", "b", "c"));
    }

    @Test
    public void elementFromEmptySet() {
        XML empty = I.xml("<root/>").find("nonexistent");
        assert empty.size() == 0;

        XML result = empty.element("child");
        assert result.size() == 0;
    }

    @Test
    public void elementFromParentWithOnlyText() {
        XML root = I.xml("<root>some text</root>");
        XML result = root.element("any");
        assert result.size() == 0;
    }

    /**
     * @see XML#firstChild()
     */
    @Test
    public void firstChild() {
        // traverse to first child element
        XML root = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root.firstChild().name().equals("first");

        // skip text node
        root = I.xml("""
                <root>
                    text is ignored
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root.firstChild().name().equals("first");

        // can't traverse
        root = I.xml("<root/>");
        assert root.firstChild().size() == 0;
    }

    @Test
    public void firstChildFromSingleParent() {
        String text = """
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """;
        XML root = I.xml(text);

        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildIgnoringLeadingTextAndCommentNodes() {
        String text = """
                <root>
                    text node
                    <!-- comment node -->
                    <child1/>
                    <child2/>
                </root>
                """;
        XML root = I.xml(text);

        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildWhenFirstIsElement() {
        String text = """
                <root>
                    <child1/>
                    text node
                    <child2/>
                </root>
                """;
        XML root = I.xml(text);

        XML first = root.firstChild();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstChildFromParentWithNoElementChildren() {
        String text = """
                <root>
                    only text
                    <!-- and comment -->
                </root>
                """;
        XML root = I.xml(text);

        XML first = root.firstChild();
        assert first.size() == 0;
    }

    @Test
    public void firstChildFromParentWithNoChildrenAtAll() {
        String text = "<root/>";
        XML root = I.xml(text);

        XML first = root.firstChild();
        assert first.size() == 0;
    }

    @Test
    public void firstChildFromMultipleParents() {
        String text = """
                <doc>
                    <parent1>text<c1a/><c1b/></parent1>
                    <parent2><!-- comment --><c2a/><c2b/></parent2>
                </doc>
                """;
        XML parents = I.xml(text).find("parent1, parent2");
        assert parents.size() == 2;

        XML firstChildren = parents.firstChild();
        assert firstChildren.size() == 2;

        List<String> firstChildNames = I.signal(firstChildren).map(XML::name).toList();
        assert firstChildNames.contains("c1a");
        assert firstChildNames.contains("c2a");
    }

    @Test
    public void firstChildFromMultipleParentsSomeWithNoElementChildren() {
        String text = """
                <doc>
                    <parent1><c1a/></parent1>
                    <parent2>text only</parent2>
                    <parent3><!-- comment --><c3a/></parent3>
                    <parent4/>
                </doc>
                """;
        XML parents = I.xml(text).find("parent1, parent2, parent3, parent4");
        assert parents.size() == 4;

        XML firstChildren = parents.firstChild();
        assert firstChildren.size() == 2;

        List<String> firstChildNames = I.signal(firstChildren).map(XML::name).toList();
        assert firstChildNames.contains("c1a");
        assert firstChildNames.contains("c3a");
        assert firstChildNames.size() == 2;
    }

    @Test
    public void firstChildAtEmptySet() {
        String text = "<root/>";
        XML emptySet = I.xml(text).find(".nonexistent");
        assert emptySet.size() == 0;

        XML first = emptySet.firstChild();
        assert first.size() == 0;
    }

    /**
     * @see XML#lastChild()
     */
    @Test
    public void lastChild() {
        // traverse to last child element
        XML root = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                </root>
                """);
        assert root.lastChild().name().equals("last");

        // skip text node
        root = I.xml("""
                <root>
                    <first/>
                    <center/>
                    <last/>
                    text is ignored
                </root>
                """);
        assert root.lastChild().name().equals("last");

        // can't traverse
        root = I.xml("<root/>");
        assert root.lastChild().size() == 0;
    }

    @Test
    public void lastChildFromSingleParent() {
        String text = """
                <root>
                    <child1/>
                    <child2/>
                    <child3/>
                </root>
                """;
        XML root = I.xml(text);

        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child3");
    }

    @Test
    public void lastChildIgnoringTrailingTextAndCommentNodes() {
        String text = """
                <root>
                    <child1/>
                    <child2/>
                    text node
                    <!-- comment node -->
                </root>
                """;
        XML root = I.xml(text);

        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    public void lastChildWhenLastIsElement() {
        String text = """
                <root>
                    <child1/>
                    text node
                    <child2/>
                </root>
                """;
        XML root = I.xml(text);

        XML last = root.lastChild();
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    public void lastChildFromParentWithNoElementChildren() {
        String text = """
                <root>
                    only text
                    <!-- and comment -->
                </root>
                """;
        XML root = I.xml(text);

        XML last = root.lastChild();
        assert last.size() == 0;
    }

    @Test
    public void lastChildFromParentWithNoChildrenAtAll() {
        String text = "<root/>";
        XML root = I.xml(text);

        XML last = root.lastChild();
        assert last.size() == 0;
    }

    @Test
    public void lastChildFromMultipleParents() {
        String text = """
                <doc>
                    <parent1>
                        <c1a/>
                        <c1b/>
                        text
                    </parent1>
                    <parent2>
                        <c2a/>
                        <c2b/>
                        <!-- comment -->
                    </parent2>
                </doc>
                """;
        XML parents = I.xml(text).find("parent1, parent2");
        assert parents.size() == 2;

        XML lastChildren = parents.lastChild();
        assert lastChildren.size() == 2;

        List<String> lastChildNames = I.signal(lastChildren).map(XML::name).toList();
        assert lastChildNames.contains("c1b");
        assert lastChildNames.contains("c2b");
    }

    @Test
    public void lastChildFromMultipleParentsSomeWithNoElementChildren() {
        String text = """
                <doc>
                    <parent1><c1a/></parent1>
                    <parent2>text only</parent2>
                    <parent3>
                        <c3a/>
                        <!-- comment -->
                    </parent3>
                    <parent4/>
                </doc>
                """;
        XML parents = I.xml(text).find("parent1, parent2, parent3, parent4");
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
        String text = "<root/>";
        XML emptySet = I.xml(text).find(".nonexistent");
        assert emptySet.size() == 0;

        XML last = emptySet.lastChild();
        assert last.size() == 0;
    }

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

        // traverse to previous element
        XML next = root.find("last").prev();
        assert next.name() == "center";

        // skip previous text node
        next = root.find("center").prev();
        assert next.name() == "first";

        // can't traverse
        next = root.find("first").prev();
        assert next.size() == 0;
    }

    @Test
    public void prevFromSingleElement() {
        String text = """
                <root>
                    <sibling1/>
                    <target/>
                    <sibling2/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevIgnoringIntermediateTextNodes() {
        String text = """
                <root>
                    <sibling1/>
                    text node
                    <target/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevIgnoringIntermediateCommentNodes() {
        String text = """
                <root>
                    <sibling1/>
                    <!-- comment node -->
                    <target/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevIgnoringMultipleIntermediateNonElementNodes() {
        String text = """
                <root>
                    <sibling1/>
                    text1
                    <!-- comment1 -->
                    text2
                    <!-- comment2 -->
                    <target/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevWhenImmediatePrevIsElement() {
        String text = """
                <root>
                    <sibling1/>
                    <target/>
                    text node
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 1;
        assert prev.name().equals("sibling1");
    }

    @Test
    public void prevWhenNoPrevSiblingElementOnlyNonElement() {
        String text = """
                <root>
                    text node
                    <!-- comment node -->
                    <target/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevWhenNoPrevSiblingAtAllAsFirstChild() {
        String text = """
                <root>
                    <target/>
                    <sibling2/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevWhenOnlyChild() {
        String text = """
                <root>
                    <target/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML prev = target.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevFromMultipleElementsEachHavingPrev() {
        String text = """
                <doc>
                    <section>
                        <s1a class='prev-marker'/>
                        <t1 class='target'/>
                    </section>
                    <section>
                        <s2a class='prev-marker'/>
                        <t2 class='target'/>
                    </section>
                </doc>
                """;
        XML targets = I.xml(text).find(".target");
        assert targets.size() == 2;

        XML prevs = targets.prev();
        assert prevs.size() == 2;

        List<XML> prevList = I.signal(prevs).toList();
        assert prevList.get(0).hasClass("prev-marker");
        assert prevList.get(1).hasClass("prev-marker");
        assert (prevList.get(0).name().equals("s1a") && prevList.get(1).name().equals("s2a")) || (prevList.get(0)
                .name()
                .equals("s2a") && prevList.get(1).name().equals("s1a"));

    }

    @Test
    public void prevFromMultipleElementsSomeWithNoPrev() {
        String text = """
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
                </doc>
                """;
        XML targets = I.xml(text).find(".target");
        assert targets.size() == 4;

        XML prevs = targets.prev();
        assert prevs.size() == 2;

        List<XML> prevList = I.signal(prevs).toList();
        assert prevList.stream().allMatch(xml -> xml.hasClass("prev-marker"));
        assert prevList.size() == 2;

        List<String> prevNames = prevList.stream().map(XML::name).collect(Collectors.toList());
        assert prevNames.contains("s2a");
        assert prevNames.contains("s3a");
    }

    @Test
    public void prevAtEmptySet() {
        String text = "<root/>";
        XML emptySet = I.xml(text).find(".nonexistent");
        assert emptySet.size() == 0;

        XML prev = emptySet.prev();
        assert prev.size() == 0;
    }

    @Test
    public void prevWithinDifferentParentsNotInterfering() {
        String text = """
                <doc>
                    <outer_sibling class='prev-marker-outer'/>
                    <parent1>
                        <s1a class='prev-marker-inner'/>
                        <t1 class='target'/>
                    </parent1>
                    <parent2>
                        <t2 class='target'/>
                    </parent2>
                </doc>
                """;
        XML targets = I.xml(text).find(".target");
        XML prevs = targets.prev();

        assert prevs.size() == 1;
        assert prevs.hasClass("prev-marker-inner");
        assert prevs.name().equals("s1a");
    }

    @Test
    public void prevUntilBasic() {
        XML root = I.xml("""
                <root>
                    <n1 class='stop'/><n2/><n3/><n4/>
                </root>
                """);

        XML result = root.find("n4").prevUntil(".stop");
        assert result.size() == 2;
        assert result.first().name().equals("n3");
        assert result.last().name().equals("n2");

        root = I.xml("""
                <root>
                    <n1 class='stop'/><n2/><n3/>
                </root>
                """);

        result = root.find("n2").prevUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void prevUntilStopperIsTag() {
        XML root = I.xml("""
                <root>
                    <other/>
                    <stopper/>
                    <item2/>
                    <item1/>
                    <start/>
                </root>
                """);

        XML result = root.find("start").prevUntil("stopper");
        assert result.size() == 2;
        assert result.first().name().equals("item1");
        assert result.last().name().equals("item2");
    }

    @Test
    public void prevUntilWithTextNodes() {
        XML root = I.xml("""
                <root>
                    <n1 class='stop'/>text<m1/>more text<m2/>final text<n3/>
                </root>
                """);

        XML result = root.find("n3").prevUntil(".stop");
        assert result.size() == 2;
        assert result.first().name().equals("m2");
        assert result.last().name().equals("m1");
    }

    @Test
    public void prevUntilStopperIsId() {
        XML root = I.xml("""
                <root>
                    <item3/>
                    <item2 id='stopHere'/>
                    <item1/>
                    <start/>
                </root>
                """);

        XML result = root.find("start").prevUntil("#stopHere");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void prevUntilStopperIsAttribute() {
        XML root = I.xml("""
                <root>
                    <item3/>
                    <item2 data-stop='yes'/>
                    <item1/>
                    <start/>
                </root>
                """);

        XML result = root.find("start").prevUntil("[data-stop=yes]");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void prevUntilStopperIsCombinedSelector() {
        XML root = I.xml("""
                <root>
                    <item/>
                    <stopper class='end' id='final' data-marker='stop'/>
                    <item class='end'/>
                    <item id='final'/>
                    <item data-marker='stop'/>
                    <start/>
                </root>
                """);

        XML xml = root.find("start");
        assert xml.prevUntil(".end[data-marker=stop]").size() == 3;
        assert xml.prevUntil("[data-marker=stop].end").size() == 3;
    }

    @Test
    public void prevUntilNoStopper() {
        XML root = I.xml("""
                <root>
                    <m1/><m2/><n3/>
                </root>
                """);

        XML result = root.find("n3").prevUntil(".nonexistent");
        assert result.size() == 2;
        assert result.first().name().equals("m2");
        assert result.last().name().equals("m1");
    }

    @Test
    public void prevUntilOnEmptySet() {
        XML root = I.xml("<root/>");
        XML result = root.find("nonexistent").prevUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void prevUntilStopperIsOneOfMultipleCssSelectors() {
        XML root = I.xml("""
                <root>
                    <n3 class='stop'/>
                    <n2/>
                    <n1/>
                </root>
                """);

        XML result = root.find("n1").prevUntil(".end, .stop, #finish"); //
        assert result.size() == 1;
        assert result.first().name().equals("n2");

        root = I.xml("""
                <root>
                    <item3 class='stop'/>
                    <item2 id='finish'/>
                    <item1/>
                    <start/>
                </root>
                """);
        result = root.find("start").prevUntil(".stop, #finish");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void prevUntilMultipleStartNodes() {
        XML root = I.xml("""
                <root>
                    <div><s3 class='stop'/><s2/><s1 class='start'/></div>
                    <div><s4 class='stop'/><s3/><s2/><s1 class='start'/></div>
                </root>
                """);

        XML result = root.find(".start").prevUntil(".stop");
        assert result.size() == 3;

        List<String> names = I.signal(result).map(XML::name).toList();
        long s2Count = names.stream().filter(name -> name.equals("s2")).count();
        long s3Count = names.stream().filter(name -> name.equals("s3")).count();

        assert names.contains("s2");
        assert names.contains("s3");
        assert s2Count == 2;
        assert s3Count == 1;
    }

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

        // traverse to next element
        XML next = root.find("first").next();
        assert next.name() == "center";

        // skip next text node
        next = root.find("center").next();
        assert next.name() == "last";

        // can't traverse
        next = root.find("last").next();
        assert next.size() == 0;
    }

    @Test
    public void nextFromSingleElement() {
        String text = """
                <root>
                    <sibling1/>
                    <target/>
                    <sibling2 class="next-marker"/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling2");
    }

    @Test
    public void nextIgnoringIntermediateTextNodes() {
        String text = """
                <root>
                    <target/>
                    text node
                    <sibling1 class="next-marker"/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextIgnoringIntermediateCommentNodes() {
        String text = """
                <root>
                    <target/>
                    <!-- comment node -->
                    <sibling1 class="next-marker"/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextIgnoringMultipleIntermediateNonElementNodes() {
        String text = """
                <root>
                    <target/>
                    text1
                    <!-- comment1 -->
                    text2
                    <!-- comment2 -->
                    <sibling1 class="next-marker"/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextWhenImmediateNextIsElement() {
        String text = """
                <root>
                    text node
                    <target/>
                    <sibling1 class="next-marker"/>
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 1;
        assert next.hasClass("next-marker");
        assert next.name().equals("sibling1");
    }

    @Test
    public void nextWhenNoNextSiblingElementOnlyNonElement() {
        String text = """
                <root>
                    <target/>
                    text node
                    <!-- comment node -->
                </root>
                """;
        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 0;
    }

    @Test
    public void nextWhenNoNextSiblingAtAllAsLastChild() {
        String text = """
                <root>
                    <sibling1/>
                    <target/>
                </root>
                """;

        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 0;
    }

    @Test
    public void nextWhenOnlyChild() {
        String text = """
                <root>
                    <target/>
                </root>
                """;

        XML target = I.xml(text).find("target");

        XML next = target.next();
        assert next.size() == 0;
    }

    @Test
    public void nextFromMultipleElementsEachHavingNext() {
        String text = """
                <doc>
                    <section>
                        <t1 class='target'/>
                        <s1a class='next-marker'/>
                    </section>
                    <section>
                        <t2 class='target'/>
                        <s2a class='next-marker'/>
                    </section>
                </doc>
                """;

        XML targets = I.xml(text).find(".target");
        assert targets.size() == 2;

        XML nexts = targets.next();
        assert nexts.size() == 2;

        List<XML> nextList = I.signal(nexts).toList();
        assert nextList.stream().allMatch(xml -> xml.hasClass("next-marker"));
        assert (nextList.get(0).name().equals("s1a") && nextList.get(1).name().equals("s2a")) || (nextList.get(0)
                .name()
                .equals("s2a") && nextList.get(1).name().equals("s1a"));
    }

    @Test
    public void nextFromMultipleElementsSomeWithNoNext() {
        String text = """
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
                </doc>
                """;

        XML targets = I.xml(text).find(".target");
        assert targets.size() == 4;

        XML nexts = targets.next();
        assert nexts.size() == 2;

        List<XML> nextList = I.signal(nexts).toList();
        assert nextList.stream().allMatch(xml -> xml.hasClass("next-marker"));
        assert nextList.size() == 2;

        List<String> nextNames = nextList.stream().map(XML::name).collect(Collectors.toList());
        assert nextNames.contains("s1a");
        assert nextNames.contains("s3a");
    }

    @Test
    public void nextAtEmptySet() {
        String text = "<root/>";
        XML emptySet = I.xml(text).find(".nonexistent");
        assert emptySet.size() == 0;

        XML next = emptySet.next();
        assert next.size() == 0;
    }

    @Test
    public void nextWithinDifferentParentsNotInterfering() {
        String text = """
                <doc>
                    <parent1>
                        <t1 class='target'/>
                        <s1a class='next-marker-inner'/>
                    </parent1>
                    <parent2>
                        <t2 class='target'/>
                    </parent2>
                    <outer_sibling class='next-marker-outer'/>
                </doc>
                """;

        XML targets = I.xml(text).find(".target");
        XML nexts = targets.next();

        assert nexts.size() == 1;
        assert nexts.hasClass("next-marker-inner");
        assert nexts.name().equals("s1a");
    }

    @Test
    public void nextUntilBasic() {
        XML root = I.xml("""
                <root>
                    <n1/>
                    <n2 class='stop'/>
                    <n3/>
                    <n4 class='stop'/>
                </root>
                """);

        XML result = root.find("n1").nextUntil(".stop");
        assert result.size() == 0;

        root = I.xml("""
                <root>
                    <n1/>
                    <m1/>
                    <m2/>
                    <n2 class='stop'/>
                    <n3/>
                    <n4 class='stop'/>
                </root>
                """);

        result = root.find("n1").nextUntil(".stop");
        assert result.size() == 2;
        assert result.first().name().equals("m1");
        assert result.last().name().equals("m2");
    }

    @Test
    public void nextUntilStopperIsTag() {
        XML root = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2/>
                    <stopper/>
                    <other/>
                </root>
                """);

        XML result = root.find("start").nextUntil("stopper");
        assert result.size() == 2;
        assert result.first().name().equals("item1");
        assert result.last().name().equals("item2");
    }

    @Test
    public void nextUntilStopperIsId() {
        XML root = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 id='stopHere'/>
                    <item3/>
                </root>
                """);
        XML result = root.find("start").nextUntil("#stopHere");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void nextUntilStopperIsAttribute() {
        XML root = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 data-stop='yes'/>
                    <item3/>
                </root>
                """);

        XML result = root.find("start").nextUntil("[data-stop=\"yes\"]");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void nextUntilStopperIsCombinedSelector() {
        XML root = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 class='end'/>
                    <stopper class='end' id='final' data-marker='stop'/>
                    <item3/>
                </root>
                """);
        XML result = root.find("start").nextUntil(".end[data-marker=stop]");
        assert result.size() == 2;
        assert result.first().name().equals("item1");
        assert result.last().name().equals("item2");
    }

    @Test
    public void nextUntilWithTextNodes() {
        XML root = I.xml("""
                <root>
                    <n1/>text<m1/>more text<m2/>final text<n2 class='stop'/>
                </root>
                """);

        XML result = root.find("n1").nextUntil(".stop");
        assert result.size() == 2;
        assert result.first().name().equals("m1");
        assert result.last().name().equals("m2");
    }

    @Test
    public void nextUntilNoStopper() {
        XML root = I.xml("""
                <root>
                    <n1/>
                    <m1/>
                    <m2/>
                </root>
                """);

        XML result = root.find("n1").nextUntil(".nonexistent");
        assert result.size() == 2;
        assert result.first().name().equals("m1");
        assert result.last().name().equals("m2");
    }

    @Test
    public void nextUntilStopperIsOneOfMultipleCssSelectors() {
        XML root = I.xml("""
                <root>
                    <n1/>
                    <n2/>
                    <n3 class='stop'/>
                </root>
                """);

        XML result = root.find("n1").nextUntil(".end, .stop, #finish");
        assert result.size() == 1;
        assert result.first().name().equals("n2");

        root = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 id='finish'/>
                    <item3 class='stop'/>
                </root>
                """);

        result = root.find("start").nextUntil(".stop, #finish");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void nextUntilWithMultiSelector() {
        XML root = I.xml("""
                <root>
                    <n1/>
                    <n2/>
                    <n3 class='stop'/>
                </root>
                """);

        XML result = root.find("n1").nextUntil(".end, .stop");
        assert result.size() == 1;
        assert result.first().name().equals("n2");
    }

    @Test
    public void nextUntilOnEmptySet() {
        XML root = I.xml("<root/>");
        XML result = root.find("nonexistent").nextUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void nextUntilMultipleStartNodes() {
        XML root = I.xml("""
                <root>
                    <div><s1/><s2/><s3 class='stop'/></div>
                    <div><s1/><s2/><s3/><s4 class='stop'/></div>
                </root>
                """);

        XML result = root.find("s1").nextUntil(".stop");
        assert result.size() == 3;

        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("s2");
        assert list.get(1).name().equals("s2");
        assert list.get(2).name().equals("s3");
    }

    @Test
    public void parentUntilBasic() {
        XML root = I.xml("""
                <div class='ancestor'>
                    <p><span id='start'>text</span></p>
                </div>
                """);

        XML result = root.find("#start").parentUntil(".ancestor");
        assert result.size() == 1;
        assert result.first().name().equals("p");

        root = I.xml("""
                <div class='ancestor'>
                    <p id='start'><span>text</span></p>
                </div>
                """);

        result = root.find("#start").parentUntil(".ancestor");
        assert result.size() == 0;
    }

    @Test
    public void parentUntilStopperIsTag() {
        XML root = I.xml("""
                <stopperTag>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </stopperTag>
                """);
        XML result = root.find("startNode").parentUntil("stopperTag");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilStopperIsId() {
        XML root = I.xml("""
                <div id='stopHere'>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </div>
                """);
        XML result = root.find("startNode").parentUntil("#stopHere");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilStopperIsAttribute() {
        XML root = I.xml("""
                <div data-stop='true'>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </div>
                """);
        XML result = root.find("startNode").parentUntil("[data-stop=true]");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilStopperIsCombinedSelector() {
        XML root = I.xml("""
                <div class='ancestor final' id='top' data-role='stopper'>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </div>
                """);
        XML result = root.find("startNode").parentUntil("div.ancestor#top[data-role=stopper]");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilThroughMultipleLevels() {
        XML root = I.xml("""
                <div class='grandparent'>
                    <div class='parent'>
                        <p class='child'><span id='start'/></p>
                    </div>
                </div>
                """);

        XML result = root.find("#start").parentUntil(".grandparent");
        assert result.size() == 2;

        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("p");
        assert list.get(1).name().equals("div");
        assert list.get(1).attr("class").equals("parent");
    }

    @Test
    public void parentUntilNoStopper() {
        XML root = I.xml("""
                <div>
                    <p>
                        <span>
                            <b id='start'/>
                        </span>
                    </p>
                </div>
                """);

        XML result = root.find("#start").parentUntil(".nonexistent");
        assert result.size() == 4;

        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("span");
        assert list.get(1).name().equals("p");
        assert list.get(2).name().equals("div");
    }

    @Test
    public void parentUntilStopperIsOneOfMultipleCssSelectors() {
        XML root = I.xml("""
                <div id='anc1'>
                  <section class='anc2'>
                    <article data-anc='3'>
                      <p>
                        <span id='start'/>
                      </p>
                    </article>
                  </section>
                </div>
                """);

        // Stop at article[data-anc='3']
        XML result1 = root.find("#start").parentUntil("article[data-anc='3'], .anc2, #anc1");
        assert result1.size() == 1;
        assert result1.first().name().equals("p");

        // Stop at section.anc2
        XML result2 = root.find("#start").parentUntil(".anc2, #anc1");
        assert result2.size() == 2;
        List<XML> list2 = I.signal(result2).toList();
        assert list2.get(0).name().equals("p");
        assert list2.get(1).name().equals("article");

        // Stop at div#anc1
        XML result3 = root.find("#start").parentUntil("#anc1");
        assert result3.size() == 3;
        List<XML> list3 = I.signal(result3).toList();
        assert list3.get(0).name().equals("p");
        assert list3.get(1).name().equals("article");
        assert list3.get(2).name().equals("section");
    }

    @Test
    public void parentUntilMultipleStartNodes() {
        XML root = I.xml("""
                <div class="stopper">
                    <section>
                        <p><span id="s1" class="start"/></p>
                    </section>
                    <section>
                        <figure><b id="s2" class="start"/> </figure>
                    </section>
                </div>
                """);
        XML result = root.find(".start").parentUntil(".stopper");
        assert result.size() == 4;

        List<String> names = I.signal(result).map(XML::name).toList();
        assert names.contains("p");
        assert names.contains("section");
        assert names.contains("figure");

        assert names.stream().filter(name -> name.equals("p")).count() == 1;
        assert names.stream().filter(name -> name.equals("section")).count() == 2;
        assert names.stream().filter(name -> name.equals("figure")).count() == 1;
    }

    @Test
    public void parentUntilOnEmptySet() {
        XML root = I.xml("<root/>");
        XML result = root.find("nonexistent").parentUntil(".stop");
        assert result.size() == 0;
    }
}