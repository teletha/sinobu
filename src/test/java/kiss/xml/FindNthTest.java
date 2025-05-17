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

public class FindNthTest {

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

        assert xml.find("Q:nth-child(1)").size() == 1;
        assert xml.find("Q:nth-child(2)").size() == 1;
        assert xml.find("Q:nth-child(5)").size() == 0;
        assert xml.find("Q:nth-child(100)").size() == 0;
        assert xml.find("Q:nth-child(2n)").size() == 3;
        assert xml.find("Q:nth-child(3n)").size() == 2;
        assert xml.find("Q:nth-child(4n)").size() == 1;
        assert xml.find("Q:nth-child(2n+1)").size() == 3;
        assert xml.find("Q:nth-child(odd)").size() == 3;
        assert xml.find("Q:nth-child(even)").size() == 3;
    }

    @Test
    public void nthChildSpacedArg() {
        XML xml = I.xml("""
                <root>
                    <item>1</item>
                    <item>2</item>
                    <item>3</item>
                    <item>4</item>
                </root>
                """);

        assert xml.find("item:nth-child( 1 )").size() == 1;
        assert xml.find("item:nth-child( 2n )").size() == 2;
        assert xml.find("item:nth-child( -n + 2 )").size() == 2;
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

        assert xml.find("item:nth-child(-1)").size() == 0;
        assert xml.find("item:nth-child(0)").size() == 0;
        assert xml.find("item:nth-child(0n)").size() == 0;
        assert xml.find("item:nth-child(-n)").size() == 0;
        assert xml.find("item:nth-child(-0n)").size() == 0;
        assert xml.find("item:nth-child(-2n-0)").size() == 0;
        assert xml.find("item:nth-child(n+1000)").size() == 0;
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

        assert xml.find("Q:nth-of-type(1)").size() == 1;
        assert xml.find("Q:nth-of-type(2)").size() == 1;
        assert xml.find("Q:nth-of-type(5)").size() == 0;
        assert xml.find("Q:nth-of-type(n)").size() == 4;
        assert xml.find("Q:nth-of-type(2n)").size() == 2;
        assert xml.find("Q:nth-of-type(3n)").size() == 1;
        assert xml.find("Q:nth-of-type(2n+1)").size() == 2;
        assert xml.find("Q:nth-of-type(odd)").size() == 2;
        assert xml.find("Q:nth-of-type(even)").size() == 2;
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

        assert xml.find("a:nth-of-type(-1)").size() == 0;
        assert xml.find("a:nth-of-type(0)").size() == 0;
        assert xml.find("a:nth-of-type(-n)").size() == 0;
        assert xml.find("a:nth-of-type(0n)").size() == 0;
        assert xml.find("a:nth-of-type(-2n)").size() == 0;
        assert xml.find("a:nth-of-type(n+100)").size() == 0;
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

        assert xml.find("x:nth-of-type( 1 )").size() == 1;
        assert xml.find("x:nth-of-type( 2n )").size() == 2;
        assert xml.find("x:nth-of-type( -n + 2 )").size() == 2;
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

        assert xml.find("x:nth-child(1)").size() == 1; // x at index 1
        assert xml.find("x:nth-child(3)").size() == 1; // x at index 3
        assert xml.find("x:nth-child(5)").size() == 1; // x at index 5

        assert xml.find("x:nth-of-type(1)").size() == 1; // first x (index 1)
        assert xml.find("x:nth-of-type(2)").size() == 1; // second x (index 3)
        assert xml.find("x:nth-of-type(3)").size() == 1; // third x (index 5)
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

        assert xml.find("a:nth-of-type(1)").size() == 1;
        assert xml.find("a:nth-of-type(2)").size() == 1;
        assert xml.find("a:nth-of-type(3)").size() == 1;
        assert xml.find("a:nth-of-type(4)").size() == 1;

        assert xml.find("a:nth-of-type(2n)").size() == 2;
        assert xml.find("a:nth-of-type(odd)").size() == 2;
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

        assert xml.find("x:nth-of-type(5n)").size() == 1; // index 5 only
        assert xml.find("x:nth-of-type(6n)").size() == 0;
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

        assert xml.find("e:nth-child(2n+2)").size() == 3;
        assert xml.find("e:nth-child(3n+1)").size() == 2;
        assert xml.find("e:nth-child(4n-2)").size() == 2;
        assert xml.find("e:nth-child(n-3)").size() == 3;
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

        assert xml.find("x:nth-child(+2n+1)").size() == 4; // 1,3,5,7
        assert xml.find("x:nth-child(+3n-1)").size() == 2; // 2,5
        assert xml.find("x:nth-child(-1n+4)").size() == 4; // 1〜4
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

        assert xml.find("x:nth-child(1)").size() == 1;
        assert xml.find("x:nth-child(3)").size() == 1;
        assert xml.find("x:nth-child(5)").size() == 1;

        assert xml.find("x:nth-of-type(1)").size() == 1;
        assert xml.find("x:nth-of-type(2)").size() == 1;
        assert xml.find("x:nth-of-type(3)").size() == 1;
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

        assert xml.find("n:nth-child(\n\t 2n + 1 )").size() == 2; // 1,3
        assert xml.find("n:nth-child( odd )").size() == 2;
        assert xml.find("n:nth-child( even )").size() == 2;
    }

    @Test
    public void nthChildInLargeDocument() {
        StringBuilder xmlContent = new StringBuilder("<root>");
        for (int i = 1; i <= 100; i++) {
            xmlContent.append("<e/>");
        }
        xmlContent.append("</root>");

        XML xml = I.xml(xmlContent.toString());

        assert xml.find("e:nth-child(10)").size() == 1;
        assert xml.find("e:nth-child(10n)").size() == 10;
        assert xml.find("e:nth-child(33n+1)").size() == 4; // 1,34,67,100
        assert xml.find("e:nth-child(101)").size() == 0;
    }
}
