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
    public void attributeSingleQuote() {
        String text = "<m><e A='A'/><e A='B'/></m>";

        // variants for white space
        assert I.xml(text).find("[A='A']").size() == 1;
        assert I.xml(text).find("[A = 'A']").size() == 1;
        assert I.xml(text).find("[A       =    'A']").size() == 1;
        assert I.xml(text).find("[ A = 'A' ]").size() == 1;
    }

    @Test
    public void attributeWithoutQuote() {
        String text = "<m><e A='A'/><e A='B'/></m>";

        assert I.xml(text).find("[A=A]").size() == 1;
    }

    @Test
    public void attributeExistence() {
        String text = """
                <root>
                    <item name='one'/>
                    <item id='two'/>
                    <item/>
                    <item name='four' class='test'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[name]").size() == 2;
        assert xml.find("[id]").size() == 1;
        assert xml.find("[class]").size() == 1;
        assert xml.find("[unknown]").size() == 0;
    }

    @Test
    public void attributeEquals() {
        String text = """
                <root>
                    <item name='one'/>
                    <item name='two'/>
                    <item name='one two'/>
                    <item name='ONE'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[name='one']").size() == 1;
        assert xml.find("[name='two']").size() == 1;
        assert xml.find("[name='one two']").size() == 1;
        assert xml.find("[name='ONE']").size() == 1; // CSS attribute selectors are case-sensitive
                                                     // by default for values unless specified
        assert xml.find("[name='three']").size() == 0;
    }

    @Test
    public void attributeContainsWord() { // [attr~=value]
        String text = """
                <root>
                    <item class='foo bar baz'/>
                    <item class='foo'/>
                    <item class='bar foo'/>
                    <item class='foobar'/>
                    <item class='other foo-bar'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[class~='foo']").size() == 3;
        assert xml.find("[class~='bar']").size() == 2;
        assert xml.find("[class~='baz']").size() == 1;
        assert xml.find("[class~='foobar']").size() == 1; // This should match if foobar is a whole
                                                          // word
        assert xml.find("[class~='foo-bar']").size() == 1;
        assert xml.find("[class~='other']").size() == 1;
        assert xml.find("[class~='b']").size() == 0; // "b" is not a whole word
    }

    @Test
    public void attributeStartsWithPrefixOrIsExactly() {
        String text = """
                <root>
                    <item lang='en'/>
                    <item lang='en-US'/>
                    <item lang='en-GB'/>
                    <item lang='fr-CA'/>
                    <item lang='english'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[lang|='en']").size() == 3;
        assert xml.find("[lang|='fr']").size() == 1;
        assert xml.find("[lang|='english']").size() == 1;
        assert xml.find("[lang|='e']").size() == 0;
    }

    @Test
    public void attributeStartsWith() { // [attr^=value]
        String text = """
                <root>
                    <item href='http://example.com'/>
                    <item href='https://example.org'/>
                    <item href='http://another.com'/>
                    <item href='test/http://example.com'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[href^='http://']").size() == 2;
        assert xml.find("[href^='https://']").size() == 1;
        assert xml.find("[href^='example']").size() == 0;
    }

    @Test
    public void attributeEndsWith() { // [attr$=value]
        String text = """
                <root>
                    <item src='image.png'/>
                    <item src='script.js'/>
                    <item src='archive.png.zip'/>
                    <item src='photo.jpeg'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[src$='.png']").size() == 1;
        assert xml.find("[src$='.js']").size() == 1;
        assert xml.find("[src$='.zip']").size() == 1;
        assert xml.find("[src$='jpeg']").size() == 1;
        assert xml.find("[src$='photo']").size() == 0;
    }

    @Test
    public void attributeContains() { // [attr*=value]
        String text = """
                <root>
                    <item title='Chapter 1: Introduction'/>
                    <item title='Chapter 2: Details'/>
                    <item title='Summary of Chapters'/>
                    <item title='chapter three'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[title*='Chapter']").size() == 3; // Case sensitive match
        assert xml.find("[title*='pter 2']").size() == 1;
        assert xml.find("[title*='summary']").size() == 0; // Case sensitive
        assert xml.find("[title*='chapter']").size() == 1; // Case sensitive match
        assert xml.find("[title*='xyz']").size() == 0;
    }

    @Test
    public void attributeNameWithHyphen() {
        String text = "<m><e data-test='value'/><invalid/></m>";
        assert I.xml(text).find("[data-test='value']").size() == 1;
        assert I.xml(text).find("[data-test]").size() == 1;
    }

    @Test
    public void attributeNameWithColon() { // XML namespaces are often represented with colons
        String text = "<m xmlns:ns='test'><e ns:attr='value'/><invalid/></m>";
        // Note: CSS selectors treat colons specially for namespace prefixes.
        // For simple attribute name matching, this should work.
        // However, for actual namespace handling, a different approach (or library support) might
        // be needed.
        // The current implementation likely treats "ns:attr" as a literal string.
        assert I.xml(text).find("[ns:attr='value']").size() == 1;
        assert I.xml(text).find("[ns:attr]").size() == 1;
    }

    @Test
    public void attributeNameWithUnderscore() {
        String text = "<m><e my_attr='value'/><invalid/></m>";
        assert I.xml(text).find("[my_attr='value']").size() == 1;
        assert I.xml(text).find("[my_attr]").size() == 1;
    }

    @Test
    public void multipleAttributeSelectors() {
        String text = """
                <root>
                    <item name='one' type='a'/>
                    <item name='one' type='b'/>
                    <item name='two' type='a'/>
                    <item name='one'/>
                </root>
                """;

        XML xml = I.xml(text);
        assert xml.find("[name='one'][type='a']").size() == 1;
        assert xml.find("[name='one'][type='b']").size() == 1;
        assert xml.find("[name='one'][type='c']").size() == 0;
        assert xml.find("[name][type]").size() == 3;
    }

    @Test
    public void tagAndAttributeSelector() {
        String text = """
                <root>
                    <item class='target'>Item 1</item>
                    <div class='target'>Div 1</div>
                    <item>Item 2</item>
                    <item class='other'>Item 3</item>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("item[class='target']").size() == 1;
        assert xml.find("div[class='target']").size() == 1;
        assert xml.find("item[class='other']").size() == 1;
        assert xml.find("span[class='target']").size() == 0;
    }

    @Test
    public void caseSensitivityOfAttributeNamesAndValues() {
        String text = """
                <root>
                    <item dataName='valueOne' data-value='ValueA'/>
                    <item dataname='valueTwo' data-value='valuea'/>
                </root>
                """;
        XML xml = I.xml(text);
        // Attribute names in XML are case-sensitive.
        // CSS attribute selectors are case-sensitive for names.
        assert xml.find("[dataName]").size() == 1;
        assert xml.find("[dataname]").size() == 1;
        assert xml.find("[DATANAME]").size() == 0;

        // Attribute values in CSS selectors are case-sensitive by default.
        assert xml.find("[data-value='ValueA']").size() == 1;
        assert xml.find("[data-value='valuea']").size() == 1;
        assert xml.find("[data-value='VALUEA']").size() == 0;

        // Test with different types of selectors
        assert xml.find("[dataName^='value']").size() == 1;
        assert xml.find("[dataName^='VALUE']").size() == 0;

        assert xml.find("[data-value*='lueA']").size() == 1;
        assert xml.find("[data-value*='luea']").size() == 1;
        assert xml.find("[data-value*='LUEA']").size() == 0;
    }

    @Test
    public void attributeValueWithSpaces() {
        String text = """
                <root>
                    <item title='hello world'/>
                    <item title='helloworld'/>
                </root>
                """;
        XML xml = I.xml(text);
        assert xml.find("[title='hello world']").size() == 1;
        assert xml.find("[title*='hello w']").size() == 1;
        assert xml.find("[title~='hello']").size() == 1;
        assert xml.find("[title~='world']").size() == 1;
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
    public void multiple() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <c/>
                    <d class='value'/>
                    <e class='value'/>
                </root>
                """);

        assert xml.find("a, b").size() == 2;
        assert xml.find("a, .value").size() == 3;
        assert xml.find("a:first-child, .value").size() == 3;
        assert xml.find("b:first-child, .value:last-child").size() == 1;
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