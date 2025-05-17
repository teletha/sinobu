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

public class HasTest {

    @Test
    public void hasElement() {
        XML xml = I.xml("""
                <m>
                    <Q><S/></Q>
                    <Q><S/><T/></Q>
                    <Q><T/></Q>
                </m>
                """);

        assert select(xml, 2, "Q:has(S)");
        assert select(xml, 2, "Q:has(T)");
        assert select(xml, 1, "Q:has(T:first-child)");
        assert select(xml, 1, "Q:has(S + T)");
    }

    @Test
    public void hasElementNest() {
        XML xml = I.xml("""
                <m>
                    <Q>
                        <S/>
                    </Q>
                    <Q>
                        <S>
                            <T/>
                        </S>
                    </Q>
                </m>
                """);

        assert select(xml, 1, "Q:has(S:has(T))");
    }

    @Test
    public void hasAttribute() {
        XML xml = I.xml("""
                <m>
                    <Q class='A'/>
                    <Q class='B'/>
                    <Q class='A B'/>
                </m>
                """);

        assert select(xml, 2, "Q:has(.A)");
        assert select(xml, 2, "Q:has(.B)");
        assert select(xml, 1, "Q:has(.A.B)");
    }

    @Test
    public void hasMultipleChildren() {
        XML xml = I.xml("""
                <m>
                    <Q><A/><B/></Q>
                    <Q><A/></Q>
                    <Q><B/></Q>
                    <Q><C/></Q>
                </m>
                """);

        // Only the first Q has both A and B as children
        assert select(xml, 1, "Q:has(A):has(B)");

        // Three Qs have either A or B
        assert select(xml, 3, "Q:has(A), Q:has(B)");

        // No A element contains B
        assert select(xml, 0, "Q:has(A:has(B))");
    }

    @Test
    public void hasDirectChildOnly() {
        XML xml = I.xml("""
                <m>
                    <Q><A/></Q>
                    <Q><C/><A><B/></A></Q>
                    <Q><A/><B/></Q>
                </m>
                """);

        // All three Qs contain A as descendant
        assert select(xml, 3, "Q:has(A)");

        // First and third Qs have A as the first child
        assert select(xml, 2, "Q:has(A:first-child)");

        // Only second Q has A containing B
        assert select(xml, 1, "Q:has(A:has(B))");
    }

    @Test
    public void hasSiblingConditions() {
        XML xml = I.xml("""
                <m>
                    <Q><A/><B/></Q>
                    <Q><B/><A/></Q>
                    <Q><A/></Q>
                    <Q><B/></Q>
                </m>
                """);

        // Only first Q has A immediately followed by B
        assert select(xml, 1, "Q:has(A + B)");

        // Only second Q has B immediately followed by A
        assert select(xml, 1, "Q:has(B + A)");

        // No Q has A followed by C
        assert select(xml, 0, "Q:has(A ~ C)");
    }

    @Test
    public void hasDeepNestPath() {
        XML xml = I.xml("""
                <root>
                    <level1>
                        <level2>
                            <target/>
                        </level2>
                    </level1>
                    <level1>
                        <other/>
                    </level1>
                </root>
                """);

        // Only the first level1 contains level2 which contains target
        assert select(xml, 1, "level1:has(level2:has(target))");

        // No level2 has <other> as a child
        assert select(xml, 0, "level1:has(level2:has(other))");
    }

    @Test
    public void hasWithUniversalSelector() {
        XML xml = I.xml("""
                <m>
                    <Q><X/></Q>
                    <Q><Y/></Q>
                    <Q><X/><Y/></Q>
                </m>
                """);

        // All Q elements have at least one child
        assert select(xml, 3, "Q:has(*)");

        // Two Qs have X as child
        assert select(xml, 2, "Q:has(X)");

        // Two Qs have Y as child
        assert select(xml, 2, "Q:has(Y)");
    }

    @Test
    public void hasWithNested() {
        XML xml = I.xml("""
                <root>
                    <user>
                        <profile>
                            <status>ok</status>
                        </profile>
                    </user>
                    <user>
                        <profile>
                            <invalid>no</invalid>
                        </profile>
                    </user>
                </root>
                """);

        assert select(xml, 2, "user:has(profile)");
        assert select(xml, 1, "user:has(profile:has(status))");
    }

    @Test
    public void hasWithMultipleOrAndNot() {
        XML xml = I.xml("""
                <root>
                    <item><A/></item>
                    <item><B/></item>
                    <item><A/><B/></item>
                    <item><C/></item>
                </root>
                """);

        // All items that have A or B
        assert select(xml, 3, "item:has(A), item:has(B)");

        // Only items that have both A and B
        assert select(xml, 1, "item:has(A):has(B)");

        // Items that have A but not B
        assert select(xml, 1, "item:has(A):not(:has(B))");

        // Items that have anything except A
        assert select(xml, 2, "item:has(*):not(:has(A))");
    }

    @Test
    public void hasDeepAndUniversalSelector() {
        XML xml = I.xml("""
                <data>
                    <record>
                        <meta>
                            <info id="1"/>
                        </meta>
                    </record>
                    <record>
                        <meta>
                            <info/>
                        </meta>
                    </record>
                </data>
                """);

        // Only first record has info with id
        assert select(xml, 1, "record:has(meta:has(info[id]))");

        // All records that have any child inside meta
        assert select(xml, 2, "record:has(meta:has(*))");
    }

    @Test
    public void hasMultipleDescendantLevels() {
        XML xml = I.xml("""
                <library>
                    <shelf>
                        <book>
                            <title/>
                        </book>
                    </shelf>
                    <shelf>
                        <magazine/>
                    </shelf>
                </library>
                """);

        // Only first shelf has book with title
        assert select(xml, 1, "shelf:has(book:has(title))");

        // Only second shelf has magazine
        assert select(xml, 1, "shelf:has(magazine)");
    }

    @Test
    public void hasWithPositionSelectors() {
        XML xml = I.xml("""
                <container>
                    <block><a/><b/></block>
                    <block><b/><a/></block>
                    <block><a/></block>
                    <block><b/></block>
                </container>
                """);

        // Only first block has a as first child
        assert select(xml, 2, "block:has(a:first-child)");

        // Only second block has a not as first child
        assert select(xml, 1, "block:has(a):not(:has(a:first-child))");
    }

}
