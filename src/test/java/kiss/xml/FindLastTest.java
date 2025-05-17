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

import static kiss.xml.FindAssetion.*;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class FindLastTest {
    @Test
    public void lastChild() {
        XML xml = I.xml("""
                <m>
                  <Q/>
                  <Q/>
                  <R/>
                </m>
                """);

        assert select(xml, 0, "Q:last-child");
        assert select(xml, 1, "R:last-child");
    }

    @Test
    public void lastChild_singleElement() {
        XML xml = I.xml("""
                <m>
                  <A/>
                </m>
                """);
        assert select(xml, 1, "A:last-child");
    }

    @Test
    public void lastChild_multipleDistinctElements() {
        XML xml = I.xml("""
                <m>
                  <A/>
                  <B/>
                  <C/>
                </m>
                """);
        assert select(xml, 0, "A:last-child");
        assert select(xml, 0, "B:last-child");
        assert select(xml, 1, "C:last-child");
    }

    @Test
    public void lastChild_mixedElements() {
        // Last child is the second B
        XML xml = I.xml("""
                <m>
                  <A/>
                  <B/>
                  <A/>
                  <B/>
                </m>
                """);
        assert select(xml, 0, "A:last-child");
        assert select(xml, 1, "B:last-child");
    }

    @Test
    public void lastChild_emptyParent() {
        XML xml = I.xml("<m/>");
        assert select(xml, 0, "A:last-child");
    }

    @Test
    public void lastChild_nestedStructure() {
        XML xml = I.xml("""
                <m>
                  <P>
                    <Q/>
                    <R/>
                  </P>
                  <S>
                    <Q/>
                    <T/>
                  </S>
                </m>
                """);
        // Global search for elements that are last children
        assert select(xml, 0, "Q:last-child"); // No Q is a last child of its parent
        assert select(xml, 1, "R:last-child"); // R in P is a last child
        assert select(xml, 1, "T:last-child"); // T in S is a last child

        // More specific paths
        assert select(xml, 1, "P > R:last-child");
        assert select(xml, 0, "P > Q:last-child");
        assert select(xml, 1, "S > T:last-child");
        assert select(xml, 0, "S > Q:last-child");
    }

    @Test
    public void lastChild_noMatch() {
        XML xml = I.xml("""
                <m>
                  <A/>
                  <B/>
                </m>
                """);
        assert select(xml, 0, "C:last-child");
    }

    @Test
    public void lastOfType() {
        XML xml = I.xml("""
                <m>
                  <Q/>
                  <Q/>
                  <R/>
                </m>
                """);

        assert select(xml, 1, "Q:last-of-type");
        assert select(xml, 1, "R:last-of-type");
    }

    @Test
    public void lastOfType_singleElement() {
        XML xml = I.xml("""
                <m>
                  <A/>
                </m>
                """);
        assert select(xml, 1, "A:last-of-type");
    }

    @Test
    public void lastOfType_multipleDistinctElements() {
        XML xml = I.xml("""
                <m>
                  <A/>
                  <B/>
                  <C/>
                </m>
                """);
        assert select(xml, 1, "A:last-of-type");
        assert select(xml, 1, "B:last-of-type");
        assert select(xml, 1, "C:last-of-type");
    }

    @Test
    public void lastOfType_mixedElements() {
        XML xml = I.xml("""
                <m>
                  <A/>
                  <B/>
                  <A/>
                  <C/>
                  <A/>
                  <B/>
                </m>
                """);
        assert select(xml, 1, "A:last-of-type"); // The third A
        assert select(xml, 1, "B:last-of-type"); // The second B
        assert select(xml, 1, "C:last-of-type"); // The only C
    }

    @Test
    public void lastOfType_emptyParent() {
        XML xml = I.xml("<m/>");
        assert select(xml, 0, "A:last-of-type");
    }

    @Test
    public void lastOfType_nestedStructure() {
        XML xml = I.xml("""
                <m>
                  <P>
                    <Q/>
                    <R/>
                    <Q/>
                  </P>
                  <S>
                    <Q/>
                    <T/>
                  </S>
                  <Q/>
                </m>
                """);
        // Global search
        assert select(xml, 3, "Q:last-of-type"); // Last Q in P, Q in S, and direct Q child of m
        assert select(xml, 1, "R:last-of-type"); // R in P
        assert select(xml, 1, "T:last-of-type"); // T in S

        // Path-specific
        assert select(xml, 1, "P > Q:last-of-type"); // Last Q in P
        assert select(xml, 1, "P > R:last-of-type"); // R in P (is last R of type in P)
        assert select(xml, 1, "S > Q:last-of-type"); // Q in S (is last Q of type in S)
        assert select(xml, 1, "S > T:last-of-type"); // T in S (is last T of type in S)
    }

    @Test
    public void lastOfType_noMatch() {
        XML xml = I.xml("""
                <m>
                  <A/>
                  <B/>
                </m>
                """);
        assert select(xml, 0, "C:last-of-type");
    }
}