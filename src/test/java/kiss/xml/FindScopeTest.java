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

public class FindScopeTest {

    @Test
    public void root() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
                """);

        assert xml.find(":root").name().equals("root");
        assert xml.find("a").find(":root").name().equals("root");

        assert select(xml, 1, ":root b");
        assert select(xml, 2, ":root , b");
        assert select(xml, 2, "b , :root");
    }

    @Test
    public void scope() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
                """);

        assert xml.find(":scope").name().equals("root");
        assert xml.find("a").find(":scope").name().equals("a");

        assert select(xml, 1, ":scope b");
        assert select(xml, 2, ":scope , b");
        assert select(xml, 2, "b , :scope");
    }

    @Test
    public void scopeWithCombinator() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
                """);

        assert xml.find("a").find(":scope ~ *, :scope").size() == 3;
        assert xml.find("a").find("~ *, :scope").size() == 3;
        assert xml.find("a").find(":scope, :scope ~ *").size() == 3;
        assert xml.find("a").find(":scope,  ~ *").size() == 3;
    }
}
