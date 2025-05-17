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

public class NotTest {
    @Test
    public void notElement() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <T/>
                    <S/>
                </m>
                """);

        assert select(xml, 2, "*:not(Q)");
        assert select(xml, 2, "*:not(T)");
        assert select(xml, 2, "*:not(S)");
        assert select(xml, 3, "*:not(R)");
    }

    @Test
    public void notAttribute() {
        XML xml = I.xml("""
                <m>
                    <Q class='A'/>
                    <Q class='B'/>
                    <Q class='A B'/>
                </m>
                """);

        assert select(xml, 1, "Q:not(.A)");
    }

    @Test
    public void notMultipleAttributes() {
        XML xml = I.xml("""
                <m>
                    <Q class='A B'/>
                    <Q class='B C'/>
                    <Q class='C D'/>
                </m>
                """);

        // Select Q elements not having class A or class D
        assert select(xml, 1, "Q:not(.A):not(.D)");
    }

    @Test
    public void notNestedElement() {
        XML xml = I.xml("""
                <m>
                    <Q><S><T/></S></Q>
                    <Q><S/></Q>
                    <Q><U/></Q>
                </m>
                """);

        // Select Q elements where S does not have T child
        assert select(xml, 1, "Q:not(:has(S))");
    }

    @Test
    public void notWithPseudoClass() {
        XML xml = I.xml("""
                <m>
                    <Q><S/></Q>
                    <Q><S/></Q>
                    <Q><T/></Q>
                </m>
                """);

        // Select Q elements that are not the first-child
        assert select(xml, 2, "Q:not(:first-child)");
    }

    @Test
    public void notEmptyElements() {
        XML xml = I.xml("""
                <m>
                    <Q></Q>
                    <Q>text</Q>
                    <Q> </Q>
                </m>
                """);

        // Select Q elements that are not empty (contain any text)
        assert select(xml, 2, "Q:not(:empty)");
    }

    @Test
    public void notWithClassAndPseudo() {
        XML xml = I.xml("""
                <m>
                    <Q class="A"/>
                    <Q class="B"/>
                    <Q class="C"/>
                    <Q class="A"/>
                </m>
                """);

        // Select Q elements not having class "A" and not first-child
        assert select(xml, 2, "Q:not(.A):not(:first-child)");
    }

    @Test
    public void notWithComplexSelector() {
        XML xml = I.xml("""
                <m>
                    <Q><S class="x"/></Q>
                    <Q><S class="y"/></Q>
                    <Q><T/></Q>
                </m>
                """);

        // Select Q elements that do NOT have S with class x
        assert select(xml, 2, "Q:not(:has(S.x))");
    }

    @Test
    public void notWithMultipleChildren() {
        XML xml = I.xml("""
                <m>
                    <Q><A/><B/></Q>
                    <Q><A/></Q>
                    <Q><B/></Q>
                </m>
                """);

        assert select(xml, 0, "Q:not(:has(A)):not(:has(B))");
    }

    @Test
    public void notWithTextContent() {
        XML xml = I.xml("""
                <m>
                    <Q>text</Q>
                    <Q> </Q>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:empty");
        assert select(xml, 2, "Q:not(:empty)");
    }

    @Test
    public void notWithAdjacentSiblings() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <S/>
                    <Q/>
                    <T/>
                    <Q/>
                </m>
                """);

        assert select(xml, 2, "Q:not(:has(+ S))");
    }

    @Test
    public void notWithAttributeValueContains() {
        XML xml = I.xml("""
                <m>
                    <Q class="A B"/>
                    <Q class="B"/>
                    <Q class="C"/>
                </m>
                """);

        assert select(xml, 2, "Q:not([class~='A'])");
    }

    @Test
    public void notCombinedWithNthChild() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 2, "Q:not(:nth-child(2))");
    }

    @Test
    public void notDeepNestedChild() {
        XML xml = I.xml("""
                <m>
                    <Q><A><B><C/></B></A></Q>
                    <Q><A><B/></A></Q>
                    <Q/>
                </m>
                """);

        assert select(xml, 2, "Q:not(:has(C))");
    }
}
