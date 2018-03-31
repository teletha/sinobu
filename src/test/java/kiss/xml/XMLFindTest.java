/*
 * Copyright (C) 2018 Nameless Production Committee
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

/**
 * @version 2012/02/05 17:08:12
 */
public class XMLFindTest {

    @Test
    public void type() throws Exception {
        String xml = "<m><E/><E/><e><E/></e></m>";

        assert I.xml(xml).find("E").size() == 3;
    }

    @Test
    public void types() throws Exception {
        String xml = "<m><E/><F/><e><G/></e></m>";

        assert I.xml(xml).find("E,F").size() == 2;
        assert I.xml(xml).find("E, F").size() == 2;
        assert I.xml(xml).find(" E , F ").size() == 2;
    }

    @Test
    public void typeWithDot() throws Exception {
        String xml = "<m><E.E.E/></m>";

        assert I.xml(xml).find("E\\.E\\.E").size() == 1;
    }

    @Test
    public void typeWithHyphen() throws Exception {
        String xml = "<m><E-E/></m>";

        assert I.xml(xml).find("E\\-E").size() == 1;
    }

    @Test
    public void clazz() throws Exception {
        String xml = "<m><e class='C'/><e class='none'/></m>";

        assert I.xml(xml).find(".C").size() == 1;
    }

    @Test
    public void clazzWithMultipleValue() throws Exception {
        String xml = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(xml).find(".A").size() == 1;
        assert I.xml(xml).find(".B").size() == 1;
        assert I.xml(xml).find(".C").size() == 1;
    }

    @Test
    public void clazzMultiple() throws Exception {
        String xml = "<m><e class='A B C'/><e class='AA BB CC'/></m>";

        assert I.xml(xml).find(".A.B").size() == 1;
        assert I.xml(xml).find(".B.C").size() == 1;
        assert I.xml(xml).find(".C.A").size() == 1;
    }

    @Test
    public void id() throws Exception {
        String xml = "<m><e id='A'/><e id='AA'/></m>";

        assert I.xml(xml).find("#A").size() == 1;
        assert I.xml(xml).find("#AA").size() == 1;
    }

    @Test
    public void attribute() throws Exception {
        String xml = "<m><e A='A' B='B'/><e A='B' B='A'/></m>";

        assert I.xml(xml).find("[A]").size() == 2;
        assert I.xml(xml).find("[B]").size() == 2;
    }

    @Test
    public void attributeValue() throws Exception {
        String xml = "<m><e A='A'/></m>";

        // variants for white space
        assert I.xml(xml).find("[A=\"A\"]").size() == 1;
        assert I.xml(xml).find("[A = \"A\"]").size() == 1;
        assert I.xml(xml).find("[A       =    \"A\"]").size() == 1;
        assert I.xml(xml).find("[ A = \"A\" ]").size() == 1;
    }

    @Test
    public void attributeConatainValue() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/></m>";

        assert I.xml(xml).find("[A ~= \"A\"]").size() == 1;
        assert I.xml(xml).find("[A ~= \"B\"]").size() == 1;
        assert I.xml(xml).find("[A ~= \"C\"]").size() == 1;
    }

    @Test
    public void attributeConatainText() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AB'/></m>";

        assert I.xml(xml).find("[A *= \"A\"]").size() == 2;
        assert I.xml(xml).find("[A *= \"B\"]").size() == 2;
        assert I.xml(xml).find("[A *= \"C\"]").size() == 1;
    }

    @Test
    public void attributeStartWith() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(xml).find("[A ^= \"A\"]").size() == 2;
        assert I.xml(xml).find("[A^= \"A\"]").size() == 2;
        assert I.xml(xml).find("[A ^=\"A\"]").size() == 2;
        assert I.xml(xml).find("[A^=\"A\"]").size() == 2;
        assert I.xml(xml).find("[A ^= \"B\"]").size() == 0;
        assert I.xml(xml).find("[A ^= \"C\"]").size() == 0;
    }

    @Test
    public void attributeEndWith() throws Exception {
        String xml = "<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>";

        assert I.xml(xml).find("[A $= \"A\"]").size() == 0;
        assert I.xml(xml).find("[A $= \"B\"]").size() == 0;
        assert I.xml(xml).find("[A $= \"C\"]").size() == 2;
        assert I.xml(xml).find("[A $=\"C\"]").size() == 2;
        assert I.xml(xml).find("[A$= \"C\"]").size() == 2;
        assert I.xml(xml).find("[A$=\"C\"]").size() == 2;
    }

    @Test
    public void child() throws Exception {
        String xml = xml("<m><P><Q/><z><Q/></z><Q/></P><Q/></m>");

        assert I.xml(xml).find("P>Q").size() == 2;
        assert I.xml(xml).find("P > Q").size() == 2;
        assert I.xml(xml).find("P   >    Q").size() == 2;
    }

    @Test
    public void sibling() throws Exception {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert I.xml(xml).find("P+Q").size() == 2;
        assert I.xml(xml).find("P + Q").size() == 2;
        assert I.xml(xml).find("P   +   Q").size() == 2;
    }

    @Test
    public void siblings() throws Exception {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/></m>");

        assert I.xml(xml).find("P~Q").size() == 3;
        assert I.xml(xml).find("P ~ Q").size() == 3;
        assert I.xml(xml).find("P   ~   Q").size() == 3;
    }

    @Test
    public void previous() throws Exception {
        String xml = xml("<m><P><Q/><P/><Q/></P><Q/><Q/><P/></m>");

        assert I.xml(xml).find("P-Q").size() == 2;
        assert I.xml(xml).find("P - Q").size() == 2;
        assert I.xml(xml).find("P   -   Q").size() == 2;
    }

    @Test
    public void firstChild() throws Exception {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:first-child").size() == 1;
        assert I.xml(xml).find("R:first-child").size() == 0;
    }

    @Test
    public void firstOfType() throws Exception {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:first-of-type").size() == 1;
        assert I.xml(xml).find("R:first-of-type").size() == 1;
    }

    @Test
    public void lastChild() throws Exception {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:last-child").size() == 0;
        assert I.xml(xml).find("R:last-child").size() == 1;
    }

    @Test
    public void lastOfType() throws Exception {
        String xml = xml("<m><Q/><Q/><R/></m>");

        assert I.xml(xml).find("Q:last-of-type").size() == 1;
        assert I.xml(xml).find("R:last-of-type").size() == 1;
    }

    @Test
    public void nthChild() throws Exception {
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
    public void nthOfType() throws Exception {
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
    public void nthLastChild() throws Exception {
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
    public void nthLastOfType() throws Exception {
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
    public void onlyChild() throws Exception {
        String xml = xml("<m><Q/><r><Q/></r><r><Q/></r></m>");

        assert I.xml(xml).find("Q:only-child").size() == 2;
    }

    @Test
    public void onlyOfType() throws Exception {
        String xml = xml("<m><Q/><r><Q/><P/></r><r><Q/></r><Q/></m>");

        assert I.xml(xml).find("Q:only-of-type").size() == 2;
    }

    @Test
    public void empty() throws Exception {
        String xml = xml("<m><Q/><Q>text</Q><Q><r/></Q></m>");

        assert I.xml(xml).find("Q:empty").size() == 1;
    }

    @Test
    public void notElement() throws Exception {
        String xml = xml("<m><Q><S/></Q><Q><t/></Q></m>");

        assert I.xml(xml).find("Q:not(S)").size() == 1;
    }

    @Test
    public void notAttribute() throws Exception {
        String xml = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(xml).find("Q:not(.A)").size() == 1;
    }

    @Test
    public void hasElement() throws Exception {
        String xml = xml("<m><Q><S/></Q><Q><S/><T/></Q><Q><T/></Q></m>");

        assert I.xml(xml).find("Q:has(S)").size() == 2;
        assert I.xml(xml).find("Q:has(T)").size() == 2;
        assert I.xml(xml).find("Q:has(T:first-child)").size() == 1;
        assert I.xml(xml).find("Q:has(S + T)").size() == 1;
    }

    @Test
    public void hasElementNest() throws Exception {
        String xml = xml("<m><Q><S/></Q><Q><S><T/></S></Q></m>");

        assert I.xml(xml).find("Q:has(S:has(T))").size() == 1;
    }

    @Test
    public void hasAttribute() throws Exception {
        String xml = xml("<m><Q class='A'/><Q class='B'/><Q class='A B'/></m>");

        assert I.xml(xml).find("Q:has(.A)").size() == 2;
        assert I.xml(xml).find("Q:has(.B)").size() == 2;
        assert I.xml(xml).find("Q:has(.A.B)").size() == 1;
    }

    @Test
    public void parent() throws Exception {
        String xml = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(xml).find("Q:parent").size() == 1;
    }

    @Test
    public void parent2() throws Exception {
        String xml = xml("<m><Q/><Q/><Q/></m>");

        assert I.xml(xml).find("Q").parent().size() == 1;
    }

    @Test
    public void root() throws Exception {
        String xml = xml("<Q><Q/></Q>");

        assert I.xml(xml).find("Q:root").size() == 1;
    }

    @Test
    public void contains() throws Exception {
        String xml = xml("<m><Q>a</Q><Q>b</Q><Q>aa</Q></m>");

        assert I.xml(xml).find("Q:contains(a)").size() == 2;
        assert I.xml(xml).find("Q:contains(b)").size() == 1;
        assert I.xml(xml).find("Q:contains(aa)").size() == 1;
    }

    @Test
    public void asterisk() throws Exception {
        String xml = xml("<m><Q><a/><b/><c/></Q></m>");

        assert I.xml(xml).find("Q *").size() == 3;
    }

    @Test
    public void namespaceElement() throws Exception {
        String xml = xml("<m xmlns:p='p' xmlns:q='q' xmlns:r='r'><p:Q/><q:Q/><r:Q/></m>");

        assert I.xml(xml).find("p|Q").size() == 1;
    }

    @Test
    public void namespaceAsterisk() throws Exception {
        String xml = xml("<m xmlns:p='p' xmlns:q='q' xmlns:r='r'><p:Q/><q:Q/><r:Q/></m>");

        assert I.xml(xml).find("p|Q").size() == 1;
    }

    @Test
    public void contextual() throws Exception {
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
