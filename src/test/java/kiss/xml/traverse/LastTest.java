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

class LastTest {

    /**
     * @see XML#last()
     */
    @Test
    void last() {
        XML xml = I.xml("""
                <root>
                    <child1 class='a'/>
                    <child2 class='a'/>
                    <child3 class='a'/>
                </root>
                """);
        XML found = xml.find(".a");
        assert found.size() == 3;

        XML last = found.last();
        assert last.size() == 1;
        assert last.name().equals("child3");
    }

    @Test
    void lastWhenAlreadySingle() {
        XML xml = I.xml("""
                <root>
                    <child1 class='a'/>
                </root>
                """);
        XML found = xml.find(".a");
        assert found.size() == 1;

        XML last = found.last();
        assert last.size() == 1;
        assert last.name().equals("child1");
    }

    @Test
    void lastAtEmpty() {
        XML xml = I.xml("<root/>");
        XML empty = xml.find("notFound");
        assert empty.size() == 0;

        XML lastFromEmpty = empty.last();
        assert lastFromEmpty.size() == 0;
    }

    @Test
    void lastOnChildrenResult() {
        XML xml = I.xml("""
                <root>
                    <child1 id='c1'/>
                    <child2 id='c2'/>
                    <child3 id='c3'/>
                </root>
                """);
        XML children = xml.children();
        assert children.size() == 3;
        List<String> ids = I.signal(children).map(c -> c.attr("id")).toList();
        assert ids.get(0).equals("c1");
        assert ids.get(1).equals("c2");
        assert ids.get(2).equals("c3");

        XML lastChild = children.last();
        assert lastChild.size() == 1;
        assert lastChild.name().equals("child3");
        assert lastChild.attr("id").equals("c3");
    }

    @Test
    void lastOnEmptyChildrenResult() {
        XML xml = I.xml("<root>  <!-- This is a comment -->  Some text  </root>");
        XML children = xml.children();
        assert children.size() == 0;

        XML lastChild = children.last();
        assert lastChild.size() == 0;
    }

    @Test
    void lastAfterAttributeFilter() {
        XML xml = I.xml("""
                <root>
                    <child class='a'/>
                    <child class='b'/>
                    <child class='c'/>
                </root>
                """);
        XML result = xml.find("[class='b']");
        assert result.size() == 1;

        XML last = result.last();
        assert last.name().equals("child");
        assert last.attr("class").equals("b");
    }

    @Test
    void lastOnChildrenWithMixedContent() {
        XML xml = I.xml("""
                <root>
                    Text node
                    <child1/>
                    <child2/>
                </root>
                """);
        XML children = xml.children();
        assert children.size() == 2;

        XML last = children.last();
        assert last.name().equals("child2");
    }

    @Test
    void lastDoesNotMutateOriginal() {
        XML xml = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                </root>
                """);
        XML found = xml.find("*");
        XML last = found.last();

        assert found.size() == 2;
        assert last.size() == 1;
        assert last.name().equals("child2");
    }

    @Test
    void lastAfterDeepFind() {
        XML xml = I.xml("""
                <root>
                    <group>
                        <item id='1'/>
                        <item id='2'/>
                    </group>
                    <group>
                        <item id='3'/>
                        <item id='4'/>
                    </group>
                </root>
                """);
        XML found = xml.find("item");
        assert found.size() == 4;

        XML last = found.last();
        assert last.attr("id").equals("4");
    }

    @Test
    void lastOnMultipleRoots() {
        XML xml = I.xml("""
                <container>
                    <item id='X'/>
                    <item id='Y'/>
                    <item id='Z'/>
                </container>
                """);
        XML items = xml.find("item");
        assert items.size() == 3;

        XML lastItem = items.last();
        assert lastItem.size() == 1;
        assert lastItem.attr("id").equals("Z");
    }
}