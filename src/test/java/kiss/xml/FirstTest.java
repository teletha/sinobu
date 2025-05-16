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

public class FirstTest {

    /**
     * @see XML#first()
     */
    @Test
    public void first() {
        XML xml = I.xml("""
                <root>
                    <child1 class='a'/>
                    <child2 class='a'/>
                    <child3 class='a'/>
                </root>
                """);
        XML found = xml.find(".a");
        assert found.size() == 3;

        XML first = found.first();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstWhenAlreadySingle() {
        XML xml = I.xml("""
                <root>
                    <child1 class='a'/>
                </root>
                """);
        XML found = xml.find(".a");
        assert found.size() == 1;

        XML first = found.first();
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstAtEmpty() {
        XML xml = I.xml("<root/>");
        XML empty = xml.find("notFound");
        assert empty.size() == 0;

        XML firstFromEmpty = empty.first();
        assert firstFromEmpty.size() == 0;
    }

    @Test
    public void firstOnChildrenResult() {
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

        XML firstChild = children.first();
        assert firstChild.size() == 1;
        assert firstChild.name().equals("child1");
        assert firstChild.attr("id").equals("c1");
    }

    @Test
    public void firstOnEmptyChildrenResult() {
        XML xml = I.xml("<root></root>");
        XML children = xml.children();
        assert children.size() == 0;

        XML firstChild = children.first();
        assert firstChild.size() == 0;
    }

    @Test
    public void firstAfterAttributeFilter() {
        XML xml = I.xml("""
                <root>
                    <child class='a'/>
                    <child class='b'/>
                    <child class='c'/>
                </root>
                """);
        XML result = xml.find("[class='b']");
        assert result.size() == 1;

        XML first = result.first();
        assert first.name().equals("child");
        assert first.attr("class").equals("b");
    }

    @Test
    public void firstOnChildrenWithMixedContent() {
        XML xml = I.xml("""
                <root>
                    Text node
                    <child1/>
                    <child2/>
                </root>
                """);
        XML children = xml.children();
        assert children.size() == 2;

        XML first = children.first();
        assert first.name().equals("child1");
    }

    @Test
    public void firstDoesNotMutateOriginal() {
        XML xml = I.xml("""
                <root>
                    <child1/>
                    <child2/>
                </root>
                """);
        XML found = xml.find("*");
        XML first = found.first();

        assert found.size() == 2;
        assert first.size() == 1;
        assert first.name().equals("child1");
    }

    @Test
    public void firstAfterDeepFind() {
        XML xml = I.xml("""
                <root>
                    <group>
                        <item id='1'/>
                        <item id='2'/>
                    </group>
                    <group>
                        <item id='3'/>
                    </group>
                </root>
                """);
        XML found = xml.find("item");
        assert found.size() == 3;

        XML first = found.first();
        assert first.attr("id").equals("1");
    }

    @Test
    public void firstOnMultipleRoots() {
        // I.xml() might not directly support multiple roots,
        // but a find operation could result in a set of top-level-like elements.
        XML xml = I.xml("""
                <container>
                    <item id='A'/>
                    <item id='B'/>
                    <item id='C'/>
                </container>
                """);
        XML items = xml.find("item");
        assert items.size() == 3;

        XML firstItem = items.first();
        assert firstItem.size() == 1;
        assert firstItem.attr("id").equals("A");
    }
}