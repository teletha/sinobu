/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class XMLFindTest {

    @Test
    public void type() {
        String xml = "<m><E/><E/><e><E/></e></m>";

        assert I.xml(xml).find("E").size() == 3;
    }

    @Test
    public void types() {
        String xml = "<m><E/><F/><e><G/></e></m>";

        assert I.xml(xml).find("E,F").size() == 2;
        assert I.xml(xml).find("E, F").size() == 2;
        assert I.xml(xml).find(" E , F ").size() == 2;
    }

    @Test
    public void typeWithDot() {
        String xml = "<m><E.E.E/></m>";

        assert I.xml(xml).find("E\\.E\\.E").size() == 1;
    }

    @Test
    public void typeWithHyphen() {
        String xml = "<m><E-E/><E--E/></m>";

        assert I.xml(xml).find("E-E").size() == 1;
        assert I.xml(xml).find("E--E").size() == 1;
    }

    @Test
    public void typeWithEscapedHyphen() {
        String xml = "<m><E-E/><E--E/></m>";

        assert I.xml(xml).find("E\\-E").size() == 1;
        assert I.xml(xml).find("E\\-\\-E").size() == 1;
    }

    @Test
    public void clazz() {
        String xml = "<m><e class='C'/><e class='none'/></m>";

        assert I.xml(xml).find(".C").size() == 1;
    }

    @Test
    public void clazzWithHyphen() {
        String xml = "<m><e class='a-b'/><e class='a--b'/><e class='none'/></m>";

        assert I.xml(xml).find(".a-b").size() == 1;
        assert I.xml(xml).find(".a--b").size() == 1;
    }

    @Test
    public void clazzWithEscapedHyphen() {
        String xml = "<m><e class='a-b'/><e class='a--b'/><e class='none'/></m>";

        assert I.xml(xml).find(".a\\-b").size() == 1;
        assert I.xml(xml).find(".a\\-\\-b").size() == 1;
    }

    @Test
    public void clazzWithEscapedDollar() {
        String xml = "<m><e class='a\\b'/><e class='none'/></m>";

        assert I.xml(xml).find(".a\\\\b").size() == 1;
    }

    @Test
    public void clazzWithMultipleValue() {
        String xml = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(xml).find(".A").size() == 1;
        assert I.xml(xml).find(".B").size() == 1;
        assert I.xml(xml).find(".C").size() == 1;
    }

    @Test
    public void clazzMultiple() {
        String xml = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(xml).find(".A.B").size() == 1;
        assert I.xml(xml).find(".B.C").size() == 1;
        assert I.xml(xml).find(".C.A").size() == 1;
    }

    @Test
    public void id() {
        String xml = "<m><e id='A'/><e id='AA'/></m>";

        assert I.xml(xml).find("#A").size() == 1;
        assert I.xml(xml).find("#AA").size() == 1;
    }

    @Test
    public void idWithHyphen() {
        String xml = "<m><e id='A-A'/><e id='A--A'/></m>";

        assert I.xml(xml).find("#A-A").size() == 1;
        assert I.xml(xml).find("#A--A").size() == 1;
    }

    @Test
    public void idWithEscapedHyphen() {
        String xml = "<m><e id='A-A'/><e id='A--A'/></m>";

        assert I.xml(xml).find("#A\\-A").size() == 1;
        assert I.xml(xml).find("#A\\-\\-A").size() == 1;
    }

    @Test
    public void attribute() {
        String xml = "<m><e A='A' B='B'/><e A='B' B='A'/></m>";

        assert I.xml(xml).find("[A]").size() == 2;
        assert I.xml(xml).find("[B]").size() == 2;
    }

    @Test
    public void attributeNS() {
        String xml = "<m xmlns:p='p'><e p:A='A' B='B'/><e A='B' p:B='A'/></m>";

        assert I.xml(xml).find("[p:A]").size() == 1;
        assert I.xml(xml).find("[p:B]").size() == 1;
        assert I.xml(xml).find("[A]").size() == 1;
        assert I.xml(xml).find("[B]").size() == 1;
    }

    @Test
    public void attributeValue() {
        String xml = "<m><e A='A'/><e A='B'/></m>";

        // variants for white space
        assert I.xml(xml).find("[A=\"A\"]").size() == 1;
        assert I.xml(xml).find("[A = \"A\"]").size() == 1;
        assert I.xml(xml).find("[A       =    \"A\"]").size() == 1;
        assert I.xml(xml).find("[ A = \"A\" ]").size() == 1;
    }

    @Test
    public void attributeValueNS() {
        String xml = "<m xmlns:p='p'><p:e p:A='A'/><e A='A'/><e p:A='B'/></m>";

        // variants for white space
        assert I.xml(xml).find("[p:A=\"A\"]").size() == 1;
        assert I.xml(xml).find("p|e[p:A=\"A\"]").size() == 1;
        assert I.xml(xml).find("p|e[p|A=\"A\"]").size() == 1;
    }

    @Test
    public void attributeConatainValue() {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert I.xml(xml).find("[A ~= \"A\"]").size() == 1;
        assert I.xml(xml).find("[A ~= \"B\"]").size() == 1;
        assert I.xml(xml).find("[A ~= \"C\"]").size() == 1;
    }

    @Test
    public void attributeConatainText() {
        String xml = "<m><e A='A B C'/><e A='AB'/></m>";

        assert I.xml(xml).find("[A *= \"A\"]").size() == 2;
        assert I.xml(xml).find("[A *= \"B\"]").size() == 2;
        assert I.xml(xml).find("[A *= \"C\"]").size() == 1;
    }

    @Test
    public void attributeStartWith() {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(xml).find("[A ^= \"A\"]").size() == 2;
        assert I.xml(xml).find("[A^= \"A\"]").size() == 2;
        assert I.xml(xml).find("[A ^=\"A\"]").size() == 2;
        assert I.xml(xml).find("[A^=\"A\"]").size() == 2;
        assert I.xml(xml).find("[A ^= \"B\"]").size() == 0;
        assert I.xml(xml).find("[A ^= \"C\"]").size() == 0;
    }

    @Test
    public void attributeEndWith() {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(xml).find("[A $= \"A\"]").size() == 0;
        assert I.xml(xml).find("[A $= \"B\"]").size() == 0;
        assert I.xml(xml).find("[A $= \"C\"]").size() == 2;
        assert I.xml(xml).find("[A $=\"C\"]").size() == 2;
        assert I.xml(xml).find("[A$= \"C\"]").size() == 2;
        assert I.xml(xml).find("[A$=\"C\"]").size() == 2;
    }

    @Test
    public void child() {
        String xml = xml("<m><P><Q/><z><Q/></z><Q/></P><Q/></m>");

        assert I.xml(xml).find("P>Q").size() == 2;
        assert I.xml(xml).find("P > Q").size() == 2;
        assert I.xml(xml).find("P   >    Q").size() == 2;
    }

    @Test
    public void sibling() {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert I.xml(xml).find("P+Q").size() == 2;
        assert I.xml(xml).find("P + Q").size() == 2;
        assert I.xml(xml).find("P   +   Q").size() == 2;
    }

    @Test
    public void siblings() {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert I.xml(xml).find("P~Q").size() == 3;
        assert I.xml(xml).find("P ~ Q").size() == 3;
        assert I.xml(xml).find("P   ~   Q").size() == 3;
    }

    @Test
    public void previous() {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/><P/></m>");

        assert I.xml(xml).find("P<Q").size() == 2;
        assert I.xml(xml).find("P < Q").size() == 2;
        assert I.xml(xml).find("P   <   Q").size() == 2;
    }

    @Test
    public void firstChild() {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:first-child").size() == 1;
        assert I.xml(xml).find("R:first-child").size() == 0;
    }

    @Test
    public void firstOfType() {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:first-of-type").size() == 1;
        assert I.xml(xml).find("R:first-of-type").size() == 1;
    }

    @Test
    public void lastChild() {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:last-child").size() == 0;
        assert I.xml(xml).find("R:last-child").size() == 1;
    }

    @Test
    public void lastOfType() {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:last-of-type").size() == 1;
        assert I.xml(xml).find("R:last-of-type").size() == 1;
    }

    @Test
    public void nthChild() {
        String xml = xml("<m><Q/><Q/><Q/><Q/><P/><Q/><Q/></m>");

        assert I.xml(xml).find("Q:nth-child(1)").size() == 1;
        assert I.xml(xml).find("Q:nth-child(2)").size() == 1;
        assert I.xml(xml).find("Q:nth-child(5)").size() == 0;
        assert I.xml(xml).find("Q:nth-child(100)").size() == 0;
        assert I.xml(xml).find("Q:nth-child(2n)").size() == 3;
        assert I.xml(xml).find("Q:nth-child(3n)").size() == 2;
        assert I.xml(xml).find("Q:nth-child(4n)").size() == 1;
        assert I.xml(xml).find("Q:nth-child(2n+1)").size() == 3;
        assert I.xml(xml).find("Q:nth-child(odd)").size() == 3;
        assert I.xml(xml).find("Q:nth-child(even)").size() == 3;
    }

    @Test
    public void nthOfType() {
        String xml = xml("<m><Q/><P/><Q/><P/><P/><Q/><Q/></m>");

        assert I.xml(xml).find("Q:nth-of-type(1)").size() == 1;
        assert I.xml(xml).find("Q:nth-of-type(2)").size() == 1;
        assert I.xml(xml).find("Q:nth-of-type(5)").size() == 0;
        assert I.xml(xml).find("Q:nth-of-type(n)").size() == 4;
        assert I.xml(xml).find("Q:nth-of-type(2n)").size() == 2;
        assert I.xml(xml).find("Q:nth-of-type(3n)").size() == 1;
        assert I.xml(xml).find("Q:nth-of-type(2n+1)").size() == 2;
        assert I.xml(xml).find("Q:nth-of-type(odd)").size() == 2;
        assert I.xml(xml).find("Q:nth-of-type(even)").size() == 2;
    }

    @Test
    public void nthLastChild() {
        String xml = xml("<m><Q/><Q/><Q/><Q/><P/><Q/><Q/></m>");

        assert I.xml(xml).find("Q:nth-last-child(1)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-child(2)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-child(3)").size() == 0;
        assert I.xml(xml).find("Q:nth-last-child(100)").size() == 0;
        assert I.xml(xml).find("Q:nth-last-child(2n)").size() == 3;
        assert I.xml(xml).find("Q:nth-last-child(3n)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-child(4n)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-child(2n+1)").size() == 3;
        assert I.xml(xml).find("Q:nth-last-child(odd)").size() == 3;
        assert I.xml(xml).find("Q:nth-last-child(even)").size() == 3;
    }

    @Test
    public void nthLastOfType() {
        String xml = xml("<m><Q/><P/><Q/><P/><P/><Q/><Q/></m>");

        assert I.xml(xml).find("Q:nth-last-of-type(1)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-of-type(2)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-of-type(5)").size() == 0;
        assert I.xml(xml).find("Q:nth-last-of-type(n)").size() == 4;
        assert I.xml(xml).find("Q:nth-last-of-type(2n)").size() == 2;
        assert I.xml(xml).find("Q:nth-last-of-type(3n)").size() == 1;
        assert I.xml(xml).find("Q:nth-last-of-type(2n+1)").size() == 2;
        assert I.xml(xml).find("Q:nth-last-of-type(odd)").size() == 2;
        assert I.xml(xml).find("Q:nth-last-of-type(even)").size() == 2;
    }

    @Test
    public void onlyChild() {
        String xml = xml("<m><Q/><r><Q/></r><r><Q/></r></m>");

        assert I.xml(xml).find("Q:only-child").size() == 2;
    }

    @Test
    public void onlyOfType() {
        String xml = xml("<m><Q/><r><Q/><P/></r><r><Q/></r><Q/></m>");

        assert I.xml(xml).find("Q:only-of-type").size() == 2;
    }

    @Test
    public void empty() {
        String xml = xml("<m><Q/><Q>text</Q><Q><r/></Q></m>");

        assert I.xml(xml).find("Q:empty").size() == 1;
    }

    @Test
    public void notElement() {
        String xml = xml("<m><Q><S/></Q><Q><t/></Q></m>");

        assert I.xml(xml).find("Q:not(S)").size() == 1;
    }

    @Test
    public void notAttribute() {
        String xml = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(xml).find("Q:not(.A)").size() == 1;
    }

    @Test
    public void hasElement() {
        String xml = xml("<m><Q><S/></Q><Q><S/><T/></Q><Q><T/></Q></m>");

        assert I.xml(xml).find("Q:has(S)").size() == 2;
        assert I.xml(xml).find("Q:has(T)").size() == 2;
        assert I.xml(xml).find("Q:has(T:first-child)").size() == 1;
        assert I.xml(xml).find("Q:has(S + T)").size() == 1;
    }

    @Test
    public void hasElementNest() {
        String xml = xml("<m><Q><S/></Q><Q><S><T/></S></Q></m>");

        assert I.xml(xml).find("Q:has(S:has(T))").size() == 1;
    }

    @Test
    public void hasAttribute() {
        String xml = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(xml).find("Q:has(.A)").size() == 2;
        assert I.xml(xml).find("Q:has(.B)").size() == 2;
        assert I.xml(xml).find("Q:has(.A.B)").size() == 1;
    }

    @Test
    public void parent() {
        String xml = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(xml).find("Q:parent").size() == 1;
    }

    @Test
    public void parent2() {
        String xml = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(xml).find("Q").parent().size() == 1;
    }

    @Test
    public void root() {
        String xml = xml("<Q><Q/></Q>");

        assert I.xml(xml).find("Q:root").size() == 1;
    }

    @Test
    public void contains() {
        String xml = xml("<m><Q>a</Q><Q>b</Q><Q>aa</Q></m>");

        assert I.xml(xml).find("Q:contains(a)").size() == 2;
        assert I.xml(xml).find("Q:contains(b)").size() == 1;
        assert I.xml(xml).find("Q:contains(aa)").size() == 1;
    }

    @Test
    public void asterisk() {
        String xml = xml("<m><Q><a/><b/><c/></Q></m>");

        assert I.xml(xml).find("Q *").size() == 3;
    }

    @Test
    public void namespaceElement() {
        String xml = xml("<m xmlns:p='p' xmlns:q='q' xmlns:r='r'><p:Q/><q:Q/><r:Q/></m>");

        assert I.xml(xml).find("p|Q").size() == 1;
    }

    @Test
    public void namespaceAsterisk() {
        String xml = xml("<m xmlns:p='p' xmlns:q='q' xmlns:r='r'><p:Q/><q:Q/><r:Q/></m>");

        assert I.xml(xml).find("p|Q").size() == 1;
    }

    @Test
    public void contextual() {
        XML e = I.xml("<Q><Q/><Q/></Q>");

        assert e.find("> Q").size() == 2;
        assert e.find(">Q").find("+Q").size() == 1;
        assert e.find("> Q").find("~Q").size() == 1;
    }

    /**
     * <p>
     * Format to human-redable xml for display when assertion is fail..
     * </p>
     */
    private static final String xml(String text) {
        return text;
    }
}