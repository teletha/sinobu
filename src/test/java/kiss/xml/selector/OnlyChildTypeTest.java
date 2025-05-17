/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.selector;

import static kiss.xml.selector.FindAssetion.*;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class OnlyChildTypeTest {
    @Test
    public void onlyChild() {
        XML xml = I.xml("""
                <m>
                  <Q/>
                  <r><Q/></r>
                  <r><Q/></r>
                </m>
                """);

        assert select(xml, 2, "Q:only-child");
    }

    @Test
    public void onlyOfType() {
        XML xml = I.xml("""
                <m>
                  <Q/>
                  <r>
                    <Q/>
                    <P/>
                  </r>
                  <r><Q/></r>
                  <Q/>
                </m>
                """);

        assert select(xml, 2, "Q:only-of-type");
    }

    @Test
    public void notOnlyChild() {
        XML xml = I.xml("""
                <m>
                  <Q/>
                  <r><Q/><S/></r>
                  <r><Q/></r>
                </m>
                """);

        assert select(xml, 1, "Q:only-child");
    }

    @Test
    public void nestedOnlyChild() {
        XML xml = I.xml("""
                <root>
                  <parent>
                    <Q/>
                  </parent>
                  <parent>
                    <Q/>
                    <R/>
                  </parent>
                  <parent>
                    <wrapper><Q/></wrapper>
                  </parent>
                </root>
                """);

        assert select(xml, 2, "Q:only-child");
    }

    @Test
    public void emptyAndOnlyChild() {
        XML xml = I.xml("""
                <container>
                  <div></div>
                  <span><Q/></span>
                  <section><Q></Q></section>
                </container>
                """);

        assert select(xml, 2, "Q:only-child");
        assert select(xml, 2, "Q:empty");
    }

    @Test
    public void multipleTypesOnlyOfType() {
        XML xml = I.xml("""
                <collection>
                  <group>
                    <A/>
                    <B/>
                    <C/>
                  </group>
                  <group>
                    <A/>
                    <A/>
                    <B/>
                  </group>
                  <group>
                    <B/>
                    <C/>
                  </group>
                </collection>
                """);

        assert select(xml, 3, "B:only-of-type");
        assert select(xml, 2, "C:only-of-type");
        assert select(xml, 1, "A:only-of-type");
    }

    @Test
    public void onlyChildWithTextNodes() {
        XML xml = I.xml("""
                <box>
                  <item> TextBefore <Q/> TextAfter </item>
                  <item> <Q/> </item>
                  <item> <R/> <Q/> </item>
                </box>
                """);

        assert select(xml, 2, "Q:only-child");
    }

    @Test
    public void onlyOfTypeWithTextNodes() {
        XML xml = I.xml("""
                <box>
                  <item> TextNode <Q/> <P/> </item>
                  <item> <Q/> TextNode </item>
                  <item> <P/> <Q/> TextNode <P/> </item>
                  <item> <S/> <Q/> <S/> </item>
                </box>
                """);
        assert select(xml, 4, "Q:only-of-type");
    }

    @Test
    public void onlyChildWithCommentNodes() {
        XML xml = I.xml("""
                <box>
                  <item> <!-- comment --> <Q/> <!-- comment --> </item>
                  <item> <Q/> </item>
                  <item> <R/> <!-- comment --> <Q/> </item>
                </box>
                """);
        assert select(xml, 2, "Q:only-child");
    }

    @Test
    public void onlyOfTypeWithCommentNodes() {
        XML xml = I.xml("""
                <box>
                  <item> <!-- comment --> <Q/> <P/> </item>
                  <item> <Q/> <!-- comment --> </item>
                  <item> <P/> <Q/> <!-- comment --> <P/> </item>
                </box>
                """);
        assert select(xml, 3, "Q:only-of-type");
    }

    @Test
    public void onlyChildNoMatch() {
        XML xml = I.xml("""
                <root>
                  <parent><Q/><R/></parent>
                  <parent><S/></parent>
                </root>
                """);
        assert select(xml, 0, "Q:only-child");
    }

    @Test
    public void onlyOfTypeNoMatch() {
        XML xml = I.xml("""
                <root>
                  <parent><Q/><Q/></parent>
                  <parent><P/><Q/><Q/></parent>
                </root>
                """);
        assert select(xml, 0, "Q:only-of-type");
    }

    @Test
    public void onlyOfTypeWithAttributeSelector() {
        XML xml = I.xml("""
                <container>
                  <div data-type="a"><Q/></div>
                  <div data-type="a"><Q/><Q/></div>
                  <div data-type="b"><Q/></div>
                  <div data-type="a"><Q/><Q/></div>
                </container>
                """);
        assert select(xml, 1, "[data-type='a'] > Q:only-of-type");
    }

    @Test
    public void onlyChildWithUniversalSelector() {
        XML xml = I.xml("""
                <box>
                  <item><A/></item>
                  <item><B/><C/></item>
                  <item><D/></item>
                </box>
                """);
        assert select(xml, 2, "*:only-child"); // A, D
    }

    @Test
    public void onlyOfTypeWithUniversalSelector() {
        XML xml = I.xml("""
                <box>
                  <item><A/><B/></item>
                  <item><A/><A/></item>
                  <item><C/></item>
                </box>
                """);
        // A in first item, B in first item, C in third item
        assert select(xml, 3, "*:only-of-type");
    }

    @Test
    public void complexNestingAndTypesForOnlyChild() {
        XML xml = I.xml("""
                <level1>
                  <level2A>
                    <Q id="q1"/> <!-- match -->
                  </level2A>
                  <level2B>
                    <level3A>
                      <Q id="q2"/> <!-- match -->
                    </level3A>
                    <level3B>
                      <Q id="q3"/> <!-- no match -->
                      <R/>
                    </level3B>
                  </level2B>
                  <Q id="q4"/> <!-- no match, level2A and level2B are siblings of Q -->
                  <S/>
                </level1>
                """);
        assert select(xml, 2, "Q:only-child"); // q1, q2
    }

    @Test
    public void complexNestingAndTypesForOnlyOfType() {
        XML xml = I.xml("""
                <level1>
                  <level2A>
                    <Q id="q_2a_1"/> <P id="p_2a_1"/> <!-- Q, P match -->
                  </level2A>
                  <level2B>
                    <level3A>
                      <Q id="q_3a_1"/> <Q id="q_3a_2"/> <!-- no Q match -->
                    </level3A>
                    <level3B>
                      <Q id="q_3b_1"/> <R id="r_3b_1"/> <!-- Q, R match -->
                    </level3B>
                    <P id="p_2b_1"/> <S id="s_2b_1"/> <!-- P, S match -->
                  </level2B>
                  <R id="r_l1_1"/> <R id="r_l1_2"/> <!-- no R match -->
                  <S id="s_l1_1"/> <!-- S match -->
                  <Q id="q_l1_1"/> <!-- Q match -->
                </level1>
                """);
        assert select(xml, 3, "Q:only-of-type"); // q_2a_1, q_3b_1, q_l1_1
        assert select(xml, 2, "P:only-of-type"); // p_2a_1, p_2b_1
        assert select(xml, 1, "R:only-of-type"); // r_3b_1
        assert select(xml, 2, "S:only-of-type"); // s_2b_1, s_l1_1
    }
}