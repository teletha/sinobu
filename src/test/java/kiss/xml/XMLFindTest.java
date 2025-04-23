/*
 * Copyright (C) 2024 The SINOBU Development Team
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
        String text = "<m><E/><E/><e><E/></e></m>";

        assert I.xml(text).find("E").size() == 3;
    }

    @Test
    public void types() {
        String text = "<m><E/><F/><e><G/></e></m>";

        assert I.xml(text).find("E,F").size() == 2;
        assert I.xml(text).find("E, F").size() == 2;
        assert I.xml(text).find(" E , F ").size() == 2;
    }

    @Test
    public void typeWithDot() {
        String text = "<m><E.E.E/></m>";

        assert I.xml(text).find("E\\.E\\.E").size() == 1;
    }

    @Test
    public void typeWithHyphen() {
        String text = "<m><E-E/><E--E/></m>";

        assert I.xml(text).find("E-E").size() == 1;
        assert I.xml(text).find("E--E").size() == 1;
    }

    @Test
    public void typeWithEscapedHyphen() {
        String text = "<m><E-E/><E--E/></m>";

        assert I.xml(text).find("E\\-E").size() == 1;
        assert I.xml(text).find("E\\-\\-E").size() == 1;
    }

    @Test
    public void clazz() {
        String text = "<m><e class='C'/><e class='none'/></m>";

        assert I.xml(text).find(".C").size() == 1;
    }

    @Test
    public void clazzWithHyphen() {
        String text = "<m><e class='a-b'/><e class='a--b'/><e class='none'/></m>";

        assert I.xml(text).find(".a-b").size() == 1;
        assert I.xml(text).find(".a--b").size() == 1;
    }

    @Test
    public void clazzWithEscapedHyphen() {
        String text = "<m><e class='a-b'/><e class='a--b'/><e class='none'/></m>";

        assert I.xml(text).find(".a\\-b").size() == 1;
        assert I.xml(text).find(".a\\-\\-b").size() == 1;
    }

    @Test
    public void clazzWithEscapedDollar() {
        String text = "<m><e class='a\\b'/><e class='none'/></m>";

        assert I.xml(text).find(".a\\\\b").size() == 1;
    }

    @Test
    public void clazzWithMultipleValue() {
        String text = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(text).find(".A").size() == 1;
        assert I.xml(text).find(".B").size() == 1;
        assert I.xml(text).find(".C").size() == 1;
    }

    @Test
    public void clazzMultiple() {
        String text = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(text).find(".A.B").size() == 1;
        assert I.xml(text).find(".B.C").size() == 1;
        assert I.xml(text).find(".C.A").size() == 1;
    }

    @Test
    public void id() {
        String text = "<m><e id='A'/><e id='AA'/></m>";

        assert I.xml(text).find("#A").size() == 1;
        assert I.xml(text).find("#AA").size() == 1;
    }

    @Test
    public void idWithHyphen() {
        String text = "<m><e id='A-A'/><e id='A--A'/></m>";

        assert I.xml(text).find("#A-A").size() == 1;
        assert I.xml(text).find("#A--A").size() == 1;
    }

    @Test
    public void idWithEscapedHyphen() {
        String text = "<m><e id='A-A'/><e id='A--A'/></m>";

        assert I.xml(text).find("#A\\-A").size() == 1;
        assert I.xml(text).find("#A\\-\\-A").size() == 1;
    }

    @Test
    public void attribute() {
        String text = "<m><e A='A' B='B'/><e A='B' B='A'/></m>";

        assert I.xml(text).find("[A]").size() == 2;
        assert I.xml(text).find("[B]").size() == 2;
    }

    @Test
    public void attributeNS() {
        String text = "<m xmlns:p='p'><e p:A='A' B='B'/><e A='B' p:B='A'/></m>";

        assert I.xml(text).find("[p:A]").size() == 1;
        assert I.xml(text).find("[p:B]").size() == 1;
        assert I.xml(text).find("[A]").size() == 1;
        assert I.xml(text).find("[B]").size() == 1;
    }

    @Test
    public void attributeValue() {
        String text = "<m><e A='A'/><e A='B'/></m>";

        // variants for white space
        assert I.xml(text).find("[A=\"A\"]").size() == 1;
        assert I.xml(text).find("[A = \"A\"]").size() == 1;
        assert I.xml(text).find("[A       =    \"A\"]").size() == 1;
        assert I.xml(text).find("[ A = \"A\" ]").size() == 1;
    }

    @Test
    public void attributeValueNS() {
        String text = "<m xmlns:p='p'><p:e p:A='A'/><e A='A'/><e p:A='B'/></m>";

        // variants for white space
        assert I.xml(text).find("[p:A=\"A\"]").size() == 1;
        assert I.xml(text).find("p|e[p:A=\"A\"]").size() == 1;
        assert I.xml(text).find("p|e[p|A=\"A\"]").size() == 1;
    }

    @Test
    public void attributeConatainValue() {
        String text = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert I.xml(text).find("[A ~= \"A\"]").size() == 1;
        assert I.xml(text).find("[A ~= \"B\"]").size() == 1;
        assert I.xml(text).find("[A ~= \"C\"]").size() == 1;
    }

    @Test
    public void attributeConatainText() {
        String text = "<m><e A='A B C'/><e A='AB'/></m>";

        assert I.xml(text).find("[A *= \"A\"]").size() == 2;
        assert I.xml(text).find("[A *= \"B\"]").size() == 2;
        assert I.xml(text).find("[A *= \"C\"]").size() == 1;
    }

    @Test
    public void attributeStartWith() {
        String text = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(text).find("[A ^= \"A\"]").size() == 2;
        assert I.xml(text).find("[A^= \"A\"]").size() == 2;
        assert I.xml(text).find("[A ^=\"A\"]").size() == 2;
        assert I.xml(text).find("[A^=\"A\"]").size() == 2;
        assert I.xml(text).find("[A ^= \"B\"]").size() == 0;
        assert I.xml(text).find("[A ^= \"C\"]").size() == 0;
    }

    @Test
    public void attributeEndWith() {
        String text = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(text).find("[A $= \"A\"]").size() == 0;
        assert I.xml(text).find("[A $= \"B\"]").size() == 0;
        assert I.xml(text).find("[A $= \"C\"]").size() == 2;
        assert I.xml(text).find("[A $=\"C\"]").size() == 2;
        assert I.xml(text).find("[A$= \"C\"]").size() == 2;
        assert I.xml(text).find("[A$=\"C\"]").size() == 2;
    }

    @Test
    public void child() {
        String text = xml("<m><P><Q/><z><Q/></z><Q/></P><Q/></m>");

        assert I.xml(text).find("P>Q").size() == 2;
        assert I.xml(text).find("P > Q").size() == 2;
        assert I.xml(text).find("P   >    Q").size() == 2;
    }

    @Test
    public void sibling() {
        String text = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert I.xml(text).find("P+Q").size() == 2;
        assert I.xml(text).find("P + Q").size() == 2;
        assert I.xml(text).find("P   +   Q").size() == 2;
    }

    @Test
    public void siblings() {
        String text = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert I.xml(text).find("P~Q").size() == 3;
        assert I.xml(text).find("P ~ Q").size() == 3;
        assert I.xml(text).find("P   ~   Q").size() == 3;
    }

    @Test
    public void previous() {
        String text = xml("<m><P><Q/><P/><Q/></P><Q/><Q/><P/></m>");

        assert I.xml(text).find("P<Q").size() == 2;
        assert I.xml(text).find("P < Q").size() == 2;
        assert I.xml(text).find("P   <   Q").size() == 2;
    }

    @Test
    public void firstChild() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:first-child").size() == 1;
        assert I.xml(text).find("R:first-child").size() == 0;
    }

    @Test
    public void firstOfType() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:first-of-type").size() == 1;
        assert I.xml(text).find("R:first-of-type").size() == 1;
    }

    @Test
    public void lastChild() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:last-child").size() == 0;
        assert I.xml(text).find("R:last-child").size() == 1;
    }

    @Test
    public void lastOfType() {
        String text = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(text).find("Q:last-of-type").size() == 1;
        assert I.xml(text).find("R:last-of-type").size() == 1;
    }

    @Test
    public void nthChild() {
        String text = xml("<m><Q/><Q/><Q/><Q/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-child(1)").size() == 1;
        assert I.xml(text).find("Q:nth-child(2)").size() == 1;
        assert I.xml(text).find("Q:nth-child(5)").size() == 0;
        assert I.xml(text).find("Q:nth-child(100)").size() == 0;
        assert I.xml(text).find("Q:nth-child(2n)").size() == 3;
        assert I.xml(text).find("Q:nth-child(3n)").size() == 2;
        assert I.xml(text).find("Q:nth-child(4n)").size() == 1;
        assert I.xml(text).find("Q:nth-child(2n+1)").size() == 3;
        assert I.xml(text).find("Q:nth-child(odd)").size() == 3;
        assert I.xml(text).find("Q:nth-child(even)").size() == 3;
    }

    @Test
    public void nthOfType() {
        String text = xml("<m><Q/><P/><Q/><P/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-of-type(1)").size() == 1;
        assert I.xml(text).find("Q:nth-of-type(2)").size() == 1;
        assert I.xml(text).find("Q:nth-of-type(5)").size() == 0;
        assert I.xml(text).find("Q:nth-of-type(n)").size() == 4;
        assert I.xml(text).find("Q:nth-of-type(2n)").size() == 2;
        assert I.xml(text).find("Q:nth-of-type(3n)").size() == 1;
        assert I.xml(text).find("Q:nth-of-type(2n+1)").size() == 2;
        assert I.xml(text).find("Q:nth-of-type(odd)").size() == 2;
        assert I.xml(text).find("Q:nth-of-type(even)").size() == 2;
    }

    @Test
    public void nthLastChild() {
        String text = xml("<m><Q/><Q/><Q/><Q/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-last-child(1)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(2)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(3)").size() == 0;
        assert I.xml(text).find("Q:nth-last-child(100)").size() == 0;
        assert I.xml(text).find("Q:nth-last-child(2n)").size() == 3;
        assert I.xml(text).find("Q:nth-last-child(3n)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(4n)").size() == 1;
        assert I.xml(text).find("Q:nth-last-child(2n+1)").size() == 3;
        assert I.xml(text).find("Q:nth-last-child(odd)").size() == 3;
        assert I.xml(text).find("Q:nth-last-child(even)").size() == 3;
    }

    @Test
    public void nthLastOfType() {
        String text = xml("<m><Q/><P/><Q/><P/><P/><Q/><Q/></m>");

        assert I.xml(text).find("Q:nth-last-of-type(1)").size() == 1;
        assert I.xml(text).find("Q:nth-last-of-type(2)").size() == 1;
        assert I.xml(text).find("Q:nth-last-of-type(5)").size() == 0;
        assert I.xml(text).find("Q:nth-last-of-type(n)").size() == 4;
        assert I.xml(text).find("Q:nth-last-of-type(2n)").size() == 2;
        assert I.xml(text).find("Q:nth-last-of-type(3n)").size() == 1;
        assert I.xml(text).find("Q:nth-last-of-type(2n+1)").size() == 2;
        assert I.xml(text).find("Q:nth-last-of-type(odd)").size() == 2;
        assert I.xml(text).find("Q:nth-last-of-type(even)").size() == 2;
    }

    @Test
    public void onlyChild() {
        String text = xml("<m><Q/><r><Q/></r><r><Q/></r></m>");

        assert I.xml(text).find("Q:only-child").size() == 2;
    }

    @Test
    public void onlyOfType() {
        String text = xml("<m><Q/><r><Q/><P/></r><r><Q/></r><Q/></m>");

        assert I.xml(text).find("Q:only-of-type").size() == 2;
    }

    @Test
    public void empty() {
        String text = xml("<m><Q/><Q>text</Q><Q><r/></Q></m>");

        assert I.xml(text).find("Q:empty").size() == 1;
    }

    @Test
    public void notElement() {
        String text = xml("<m><Q><S/></Q><Q><t/></Q></m>");

        assert I.xml(text).find("Q:not(S)").size() == 1;
    }

    @Test
    public void notAttribute() {
        String text = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(text).find("Q:not(.A)").size() == 1;
    }

    @Test
    public void hasElement() {
        String text = xml("<m><Q><S/></Q><Q><S/><T/></Q><Q><T/></Q></m>");

        assert I.xml(text).find("Q:has(S)").size() == 2;
        assert I.xml(text).find("Q:has(T)").size() == 2;
        assert I.xml(text).find("Q:has(T:first-child)").size() == 1;
        assert I.xml(text).find("Q:has(S + T)").size() == 1;
    }

    @Test
    public void hasElementNest() {
        String text = xml("<m><Q><S/></Q><Q><S><T/></S></Q></m>");

        assert I.xml(text).find("Q:has(S:has(T))").size() == 1;
    }

    @Test
    public void hasAttribute() {
        String text = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(text).find("Q:has(.A)").size() == 2;
        assert I.xml(text).find("Q:has(.B)").size() == 2;
        assert I.xml(text).find("Q:has(.A.B)").size() == 1;
    }

    @Test
    public void parent() {
        String text = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(text).find("Q:parent").size() == 1;
    }

    @Test
    public void parent2() {
        String text = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(text).find("Q").parent().size() == 1;
    }

    @Test
    public void root() {
        String text = xml("<Q><Q/></Q>");

        assert I.xml(text).find("Q:root").size() == 1;
    }

    @Test
    public void contains() {
        String text = xml("<m><Q>a</Q><Q>b</Q><Q>aa</Q></m>");

        assert I.xml(text).find("Q:contains(a)").size() == 2;
        assert I.xml(text).find("Q:contains(b)").size() == 1;
        assert I.xml(text).find("Q:contains(aa)").size() == 1;
    }

    @Test
    public void asterisk() {
        String text = xml("<m><Q><a/><b/><c/></Q></m>");

        assert I.xml(text).find("Q *").size() == 3;
    }

    @Test
    public void namespaceElement() {
        String text = xml("<m xmlns:p='p' xmlns:q='q' xmlns:r='r'><p:Q/><q:Q/><r:Q/></m>");

        assert I.xml(text).find("p|Q").size() == 1;
    }

    @Test
    public void namespaceAsterisk() {
        String text = xml("<m xmlns:p='p' xmlns:q='q' xmlns:r='r'><p:Q/><q:Q/><r:Q/></m>");

        assert I.xml(text).find("p|Q").size() == 1;
    }

    @Test
    public void contextual() {
        XML root = I.xml("<Q><Q/><Q/></Q>");

        assert root.find("> Q").size() == 2;
        assert root.find(">Q").find("+Q").size() == 1;
        assert root.find("> Q").find("~Q").size() == 1;
    }

    /**
     * <p>
     * Format to human-redable text for display when assertion is fail..
     * </p>
     */
    private static final String xml(String text) {
        return text;
    }
}