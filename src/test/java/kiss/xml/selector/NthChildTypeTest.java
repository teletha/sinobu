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

public class NthChildTypeTest {

    @Test
    public void nthChild() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                    <P/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:nth-child(1)");
        assert select(xml, 1, "Q:nth-child(2)");
        assert select(xml, 0, "Q:nth-child(5)");
        assert select(xml, 0, "Q:nth-child(100)");
        assert select(xml, 3, "Q:nth-child(2n)");
        assert select(xml, 2, "Q:nth-child(3n)");
        assert select(xml, 1, "Q:nth-child(4n)");
        assert select(xml, 3, "Q:nth-child(2n+1)");
        assert select(xml, 3, "Q:nth-child(odd)");
        assert select(xml, 3, "Q:nth-child(even)");
    }

    @Test
    public void nthChildNegativeRemainder() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 5, "Q:nth-child(n-1)");
        assert select(xml, 5, "Q:nth-child(n-2)");
        assert select(xml, 5, "Q:nth-child(n-3)");
        assert select(xml, 5, "Q:nth-child(n-4)");
        assert select(xml, 5, "Q:nth-child(n-5)");
        assert select(xml, 5, "Q:nth-child(n-6)");
        assert select(xml, 5, "Q:nth-child(n-7)");
    }

    @Test
    public void nthChildNegativeCoeficient() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:nth-child(-n+1)");
        assert select(xml, 2, "Q:nth-child(-n+2)");
        assert select(xml, 3, "Q:nth-child(-n+3)");
        assert select(xml, 4, "Q:nth-child(-n+4)");
        assert select(xml, 5, "Q:nth-child(-n+5)");
        assert select(xml, 5, "Q:nth-child(-n+6)");
        assert select(xml, 5, "Q:nth-child(-n+7)");
    }

    @Test
    public void nthChildInvalidArg() {
        XML xml = I.xml("""
                <root>
                    <item>1</item>
                    <item>2</item>
                    <item>3</item>
                    <item>4</item>
                </root>
                """);

        assert select(xml, 0, "item:nth-child(-1)");
        assert select(xml, 0, "item:nth-child(0)");
        assert select(xml, 0, "item:nth-child(0n)");
        assert select(xml, 0, "item:nth-child(-n)");
        assert select(xml, 0, "item:nth-child(-0n)");
        assert select(xml, 0, "item:nth-child(-2n-0)");
        assert select(xml, 0, "item:nth-child(n+1000)");
    }

    @Test
    public void nthOfType() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <P/>
                    <Q/>
                    <P/>
                    <P/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:nth-of-type(1)");
        assert select(xml, 1, "Q:nth-of-type(2)");
        assert select(xml, 0, "Q:nth-of-type(5)");
        assert select(xml, 4, "Q:nth-of-type(n)");
        assert select(xml, 2, "Q:nth-of-type(2n)");
        assert select(xml, 1, "Q:nth-of-type(3n)");
        assert select(xml, 2, "Q:nth-of-type(2n+1)");
        assert select(xml, 2, "Q:nth-of-type(odd)");
        assert select(xml, 2, "Q:nth-of-type(even)");
    }

    @Test
    public void nthOfTypeInvalidArg() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <a/>
                    <a/>
                    <a/>
                </root>
                """);

        assert select(xml, 0, "a:nth-of-type(-1)");
        assert select(xml, 0, "a:nth-of-type(0)");
        assert select(xml, 0, "a:nth-of-type(-n)");
        assert select(xml, 0, "a:nth-of-type(0n)");
        assert select(xml, 0, "a:nth-of-type(-2n)");
        assert select(xml, 0, "a:nth-of-type(n+100)");
    }

    @Test
    public void nthOfTypeSpacedArg() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                </root>
                """);

        assert select(xml, 1, "x:nth-of-type( 1 )");
        assert select(xml, 2, "x:nth-of-type( 2n )");
        assert select(xml, 2, "x:nth-of-type( -n + 2 )");
    }

    @Test
    public void nthChildVsNthOfTypeDifference() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <y/>
                    <x/>
                    <y/>
                    <x/>
                </root>
                """);

        assert select(xml, 1, "x:nth-child(1)");
        assert select(xml, 1, "x:nth-child(3)");
        assert select(xml, 1, "x:nth-child(5)");

        assert select(xml, 1, "x:nth-of-type(1)");
        assert select(xml, 1, "x:nth-of-type(2)");
        assert select(xml, 1, "x:nth-of-type(3)");
    }

    @Test
    public void nthOfTypeWithInterleavedTags() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <a/>
                    <b/>
                    <c/>
                    <a/>
                    <c/>
                    <a/>
                </root>
                """);

        assert select(xml, 1, "a:nth-of-type(1)");
        assert select(xml, 1, "a:nth-of-type(2)");
        assert select(xml, 1, "a:nth-of-type(3)");
        assert select(xml, 1, "a:nth-of-type(4)");

        assert select(xml, 2, "a:nth-of-type(2n)");
        assert select(xml, 2, "a:nth-of-type(odd)");
    }

    @Test
    public void nthOfTypeLargeSkip() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                </root>
                """);

        assert select(xml, 1, "x:nth-of-type(5n)");
        assert select(xml, 0, "x:nth-of-type(6n)");
    }

    @Test
    public void nthChildComplexExpressions() {
        XML xml = I.xml("""
                <root>
                    <e/>
                    <e/>
                    <e/>
                    <e/>
                    <e/>
                    <e/>
                </root>
                """);

        assert select(xml, 3, "e:nth-child(2n+2)");
        assert select(xml, 2, "e:nth-child(3n+1)");
        assert select(xml, 2, "e:nth-child(4n-2)");
        assert select(xml, 6, "e:nth-child(n-3)");
    }

    @Test
    public void nthChildWithSignedArguments() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                </root>
                """);

        assert select(xml, 4, "x:nth-child(+2n+1)");
        assert select(xml, 2, "x:nth-child(+3n-1)");
        assert select(xml, 4, "x:nth-child(-1n+4)");
    }

    @Test
    public void nthOfTypeAmongMixedElements() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <y/>
                    <x/>
                    <z/>
                    <x/>
                    <y/>
                </root>
                """);

        assert select(xml, 1, "x:nth-child(1)");
        assert select(xml, 1, "x:nth-child(3)");
        assert select(xml, 1, "x:nth-child(5)");

        assert select(xml, 1, "x:nth-of-type(1)");
        assert select(xml, 1, "x:nth-of-type(2)");
        assert select(xml, 1, "x:nth-of-type(3)");
    }

    @Test
    public void nthChildWithWhitespaceAndNoise() {
        XML xml = I.xml("""
                <root>
                    <n/>
                    <n/>
                    <n/>
                    <n/>
                </root>
                """);

        assert select(xml, 2, "n:nth-child(\n\t 2n + 1 )");
        assert select(xml, 2, "n:nth-child( odd )");
        assert select(xml, 2, "n:nth-child( even )");
    }

    @Test
    public void nthChildInLargeDocument() {
        StringBuilder xmlContent = new StringBuilder("<root>");
        for (int i = 1; i <= 100; i++) {
            xmlContent.append("<e/>");
        }
        xmlContent.append("</root>");

        XML xml = I.xml(xmlContent.toString());

        assert select(xml, 1, "e:nth-child(10)");
        assert select(xml, 10, "e:nth-child(10n)");
        assert select(xml, 4, "e:nth-child(33n+1)");
        assert select(xml, 0, "e:nth-child(101)");
    }

    @Test
    public void nthChildWithUniversalSelector() {
        XML xml = I.xml("""
                <m>
                    <Q/> <!-- 1st child -->
                    <P/> <!-- 2nd child -->
                    <Q/> <!-- 3rd child -->
                    <S/> <!-- 4th child -->
                    <Q/> <!-- 5th child -->
                </m>
                """);

        assert select(xml, 1, "*:nth-child(1)"); // Q
        assert select(xml, 1, "*:nth-child(2)"); // P
        assert select(xml, 1, "*:nth-child(3)"); // Q
        assert select(xml, 1, "*:nth-child(4)"); // S
        assert select(xml, 1, "*:nth-child(5)"); // Q
        assert select(xml, 0, "*:nth-child(6)");

        assert select(xml, 2, "*:nth-child(2n)"); // P (2nd), S (4th)
        assert select(xml, 3, "*:nth-child(odd)"); // Q (1st), Q (3rd), Q (5th)
        assert select(xml, 1, "*:nth-child(-n+1)"); // Q (1st)
    }

    @Test
    public void nthOfTypeWithUniversalSelector() {
        XML xml = I.xml("""
                <m>
                    <Q id="q1"/>
                    <P id="p1"/>
                    <Q id="q2"/>
                    <P id="p2"/>
                    <Q id="q3"/>
                    <S id="s1"/>
                </m>
                """);

        assert select(xml, 3, "*:nth-of-type(1)"); // q1, p1, s1
        assert select(xml, 2, "*:nth-of-type(2)"); // q2, p2
        assert select(xml, 1, "*:nth-of-type(3)"); // q3
        assert select(xml, 0, "*:nth-of-type(4)");

        assert select(xml, 4, "*:nth-of-type(odd)"); // q1,p1,s1 (1st of their type), q3 (3rd Q)
        assert select(xml, 2, "*:nth-of-type(even)"); // q2,p2 (2nd of their type)
    }

    @Test
    public void nthChildWithUniversalSelectorAndNoMatches() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                </m>
                """);
        assert select(xml, 0, "*:nth-child(2)");
        assert select(xml, 0, "*:nth-child(2n)");
    }

    @Test
    public void nthOfTypeWithUniversalSelectorAndNoMatches() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <P/>
                </m>
                """);
        // Every element is the 1st of its type.
        assert select(xml, 0, "*:nth-of-type(2)");
        assert select(xml, 0, "*:nth-of-type(even)");
    }

    @Test
    public void nthChildWithUniversalSelectorAndTextNodes() {
        XML xml = I.xml("""
                <m>
                    TextBefore
                    <Q/> <!-- 1st element child -->
                    TextBetween
                    <P/> <!-- 2nd element child -->
                    TextAfter
                </m>
                """);
        assert select(xml, 1, "*:nth-child(1)"); // Q
        assert select(xml, 1, "*:nth-child(2)"); // P
        assert select(xml, 1, "*:nth-child(odd)"); // Q
    }

    @Test
    public void nthLastChildWithUniversalSelector() {
        XML xml = I.xml("""
                <m>
                    <Q/> <!-- 5th from last -->
                    <P/> <!-- 4th from last -->
                    <Q/> <!-- 3rd from last -->
                    <S/> <!-- 2nd from last -->
                    <Q/> <!-- 1st from last -->
                </m>
                """);

        assert select(xml, 1, "*:nth-last-child(1)"); // Last Q
        assert select(xml, 1, "*:nth-last-child(2)"); // S
        assert select(xml, 1, "*:nth-last-child(3)"); // Middle Q
        assert select(xml, 1, "*:nth-last-child(4)"); // P
        assert select(xml, 1, "*:nth-last-child(5)"); // First Q
        assert select(xml, 0, "*:nth-last-child(6)");

        assert select(xml, 2, "*:nth-last-child(2n)"); // S (2nd), P (4th)
        assert select(xml, 3, "*:nth-last-child(odd)"); // Last Q (1st), Middle Q (3rd), First Q
                                                        // (5th)
        assert select(xml, 1, "*:nth-last-child(-n+1)"); // Last Q (1st)
    }

    @Test
    public void nthLastOfTypeWithUniversalSelector() {
        XML xml = I.xml("""
                <m>
                    <Q id="q1"/> <!-- 3rd Q from last -->
                    <P id="p1"/> <!-- 2nd P from last -->
                    <Q id="q2"/> <!-- 2nd Q from last -->
                    <P id="p2"/> <!-- 1st P from last -->
                    <Q id="q3"/> <!-- 1st Q from last -->
                    <S id="s1"/> <!-- 1st S from last -->
                </m>
                """);

        assert select(xml, 3, "*:nth-last-of-type(1)"); // q3, p2, s1
        assert select(xml, 2, "*:nth-last-of-type(2)"); // q2, p1
        assert select(xml, 1, "*:nth-last-of-type(3)"); // q1
        assert select(xml, 0, "*:nth-last-of-type(4)");

        assert select(xml, 4, "*:nth-last-of-type(odd)"); // q3,p2,s1 (1st of type), q1 (3rd Q)
        assert select(xml, 2, "*:nth-last-of-type(even)"); // q2,p1 (2nd of type)
    }

    @Test
    public void nthLastChildWithUniversalSelectorAndNoMatches() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                </m>
                """);
        assert select(xml, 0, "*:nth-last-child(2)");
        assert select(xml, 0, "*:nth-last-child(2n)"); // Only 1 child, so no 2nd, 4th etc.
    }

    @Test
    public void nthLastOfTypeWithUniversalSelectorAndNoMatches() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <P/>
                </m>
                """);
        // Every element is the 1st (and only) of its type from the last.
        assert select(xml, 0, "*:nth-last-of-type(2)");
        assert select(xml, 0, "*:nth-last-of-type(even)");
    }

    @Test
    public void nthLastChildWithUniversalSelectorAndTextNodes() {
        XML xml = I.xml("""
                <m>
                    TextBefore
                    <Q/> <!-- 2nd element child from last -->
                    TextBetween
                    <P/> <!-- 1st element child from last -->
                    TextAfter
                </m>
                """);
        assert select(xml, 1, "*:nth-last-child(1)"); // P
        assert select(xml, 1, "*:nth-last-child(2)"); // Q
        assert select(xml, 1, "*:nth-last-child(odd)"); // P (1st)
        assert select(xml, 1, "*:nth-last-child(even)"); // Q (2nd)
    }
}
