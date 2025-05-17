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

public class FindSiblingsTest {

    @Test
    public void sibling() {
        XML xml = I.xml("""
                <m>
                    <P>
                        <Q/>
                        <P/>
                        <Q/>
                        <Q/>
                    </P>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 2, "P + Q");
    }

    @Test
    public void siblings() {
        XML xml = I.xml("""
                <m>
                    <P>
                        <Q/>
                        <P/>
                        <Q/>
                    </P>
                    <Q/>
                    <Q/>
                    <nonstop/>
                    <Q/>
                </m>
                """);

        assert select(xml, 4, "P ~ Q");
    }

    @Test
    public void siblingWithClass() {
        XML xml = I.xml("""
                <m>
                    <P class="a"/>
                    <Q class="x"/>
                    <Q class="y"/>
                    <Q class="z"/>
                    <Q/>
                </m>
                """);
        assert select(xml, 4, "P.a ~ Q");
        assert select(xml, 1, "P.a ~ Q.y");
        assert select(xml, 3, "P.a ~ Q[class]");
        assert select(xml, 1, "P.a ~ Q:not([class])");
    }

    @Test
    public void previous() {
        XML xml = I.xml("""
                <m>
                    <P>
                        <Q id='a'/>
                        <P/>
                        <Q id='b'/>
                    </P>
                    <Q id='c'/>
                    <Q id='d'/>
                    <P/>
                </m>
                """);

        assert select(xml, 2, "P < Q");
    }

    @Test
    public void none() {
        XML xml = I.xml("""
                <m>
                    <P/>
                </m>
                """);

        assert select(xml, 0, "P + none");
        assert select(xml, 0, "P ~ none");
        assert select(xml, 0, "P < none");
    }

}
