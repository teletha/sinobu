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

public class FindDescendantTest {

    @Test
    public void none() {
        XML xml = I.xml("<root/>");

        assert select(xml, 0, "none");
        assert select(xml, 0, "> none");
    }

    @Test
    public void mix() {
        XML xml = I.xml("""
                <m>
                    <ok/>
                    <ok>
                        <ok id="not"/>
                        <not>
                            <ok/>
                        </not>
                    </ok>
                </m>
                """);

        assert xml.find("ok").size() == 4;
        assert xml.find("not ok").size() == 1;
        assert xml.find("ok > ok").size() == 1;
    }

    @Test
    public void child() {
        XML xml = I.xml("""
                <m>
                    <ok/>
                    <ok>
                        <ok id="not"/>
                        <not>
                            <ok/>
                        </not>
                    </ok>
                </m>
                """);

        assert select(xml, 2, "> ok");
        assert select(xml, 1, "ok > ok");
        assert select(xml, 1, "not > ok");
    }

    @Test
    public void descendant() {
        XML xml = I.xml("""
                <m>
                    <ok/>
                    <ok>
                        <ok id="not"/>
                        <not>
                            <ok/>
                        </not>
                    </ok>
                </m>
                """);

        assert select(xml, 4, "ok");
        assert select(xml, 2, "ok ok");
        assert select(xml, 1, "not ok");
    }

    @Test
    public void combination_childThenDescendantFromGroup() {
        XML xml = I.xml("""
                <container>
                    <group1>
                        <item>I1</item>
                        <div>
                            <item>I2</item>
                        </div>
                    </group1>
                    <group2>
                         <item>I3</item>
                    </group2>
                    <item>I4</item>
                </container>
                """);
        assert select(xml, 2, "> group1 item");
        assert select(xml, 1, "> group2 item");
        assert select(xml, 0, "> div item");
        assert select(xml, 1, "div item");
    }

    @Test
    public void combination_descendantThenChildFromBC() {
        XML xml = I.xml("""
                <A>
                    <B>
                        <C>
                            <D/>
                        </C>
                    </B>
                    <B>
                        <C>
                            <D/>
                            <E/>
                        </C>
                        <F/>
                    </B>
                    <G>
                        <C>
                            <D/>
                        </C>
                    </G>
                </A>
                """);
        assert select(xml, 2, "B > C");
        assert select(xml, 3, "C > D");
        assert select(xml, 1, "B C > E");
    }

    @Test
    public void combination_nestedStructureUnderC() {
        XML xml = I.xml("""
                <A>
                    <B>
                        <C>
                            <D>
                                <E/>
                            </D>
                        </C>
                    </B>
                    <X>
                        <Y>
                            <C>
                                <D/>
                            </C>
                        </Y>
                    </X>
                </A>
                """);
        assert select(xml, 1, "C E");
        assert select(xml, 1, "Y > C > D");
    }
}
