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

public class EmptyTest {

    @Test
    public void empty() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q>text</Q>
                    <Q><r/></Q>
                </m>
                """);

        assert select(xml, 1, "Q:empty");
    }

    @Test
    public void whitespace() {
        XML xml = I.xml("""
                <m>
                    <Q> </Q>
                    <Q>\r</Q>
                    <Q>\n</Q>
                    <Q>\t</Q>
                </m>
                """);

        assert select(xml, 0, "Q:empty");
    }

    @Test
    public void close() {
        XML xml = I.xml("""
                <m>
                    <Q></Q>
                </m>
                """);

        assert select(xml, 1, "Q:empty");
    }

    @Test
    public void attribute() {
        XML xml = I.xml("""
                <m>
                    <Q id='test'/>
                </m>
                """);

        assert select(xml, 1, "Q:empty");
    }

    @Test
    public void comment() {
        XML xml = I.xml("""
                <m>
                    <Q><!-- comment --></Q>
                    <Q><!-- comment --> <!-- and whitespace --></Q>
                </m>
                """);

        assert select(xml, 1, "Q:empty");
    }

    @Test
    public void pi() {
        XML xml = I.xml("""
                <m>
                    <Q><? processing-instruction ?></Q>
                </m>
                """);

        assert select(xml, 1, "Q:empty");
    }
}
