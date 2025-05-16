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

public class FindAttributeTest extends FindTestBase {

    @Test
    public void attribute() {
        XML xml = I.xml("<m><e A='A' B='B'/><e A='B' B='A'/></m>");

        validateAttributeSelector(xml, 2, "[A]");
        validateAttributeSelector(xml, 2, "[B]");
    }

    @Test
    public void attributeNS() {
        XML xml = I.xml("<m xmlns:p='p'><e p:A='A' B='B'/><e A='B' p:B='A'/></m>");

        validateAttributeSelector(xml, 2, "[A]");
        validateAttributeSelector(xml, 2, "[B]");
    }

    @Test
    public void attributeValue() {
        XML xml = I.xml("<m><e A='a'/><e A='B'/></m>");

        validateAttributeSelector(xml, 1, "[A='a']");
    }

    @Test
    public void attributeSingleQuote() {
        XML xml = I.xml("<m><e A='A'/><e A='B'/></m>");

        validateAttributeSelector(xml, 1, "[A='A']");
    }

    @Test
    public void attributeWithoutQuote() {
        XML xml = I.xml("<m><e A='A'/><e A='B'/></m>");

        validateAttributeSelector(xml, 1, "[A=A]");
    }

    @Test
    public void attributeExistence() {
        XML xml = I.xml("""
                <root>
                    <item name='one'/>
                    <item id='two'/>
                    <item/>
                    <item name='four' class='test'/>
                </root>
                """);
        validateAttributeSelector(xml, 2, "[name]");
        validateAttributeSelector(xml, 1, "[id]");
        validateAttributeSelector(xml, 1, "[class]");
        validateAttributeSelector(xml, 0, "[unknown]");
    }

    @Test
    public void attributeEquals() {
        XML xml = I.xml("""
                <root>
                    <item name='one'/>
                    <item name='two'/>
                    <item name='one two'/>
                    <item name='ONE'/>
                </root>
                """);
        validateAttributeSelector(xml, 1, "[name='one']");
        validateAttributeSelector(xml, 1, "[name='two']");
        validateAttributeSelector(xml, 1, "[name='one two']");
        validateAttributeSelector(xml, 1, "[name='ONE']");
        validateAttributeSelector(xml, 0, "[name='three']");
    }

    @Test
    public void attributeContainsWord() { // [attr~=value]
        XML xml = I.xml("""
                <root>
                    <item class='foo bar baz'/>
                    <item class='foo'/>
                    <item class='bar foo'/>
                    <item class='foobar'/>
                    <item class='other foo-bar'/>
                </root>
                """);
        validateAttributeSelector(xml, 3, "[class~='foo']");
        validateAttributeSelector(xml, 2, "[class~='bar']");
        validateAttributeSelector(xml, 1, "[class~='baz']");
        validateAttributeSelector(xml, 1, "[class~='foobar']");

        // word
        validateAttributeSelector(xml, 1, "[class~='foo-bar']");
        validateAttributeSelector(xml, 1, "[class~='other']");
        validateAttributeSelector(xml, 0, "[class~='b']"); // "b" is not a whole word
    }

    @Test
    public void attributeStartsWithPrefixOrIsExactly() {
        XML xml = I.xml("""
                <root>
                    <item lang='en'/>
                    <item lang='en-US'/>
                    <item lang='en-GB'/>
                    <item lang='fr-CA'/>
                    <item lang='english'/>
                </root>
                """);
        validateAttributeSelector(xml, 3, "[lang|='en']");
        validateAttributeSelector(xml, 1, "[lang|='fr']");
        validateAttributeSelector(xml, 1, "[lang|='english']");
        validateAttributeSelector(xml, 0, "[lang|='e']");
    }

    @Test
    public void attributeStartsWith() { // [attr^=value]
        XML xml = I.xml("""
                <root>
                    <item href='http://example.com'/>
                    <item href='https://example.org'/>
                    <item href='http://another.com'/>
                    <item href='test/http://example.com'/>
                </root>
                """);
        validateAttributeSelector(xml, 2, "[href^='http://']");
        validateAttributeSelector(xml, 1, "[href^='https://']");
        validateAttributeSelector(xml, 0, "[href^='example']");
    }

    @Test
    public void attributeEndsWith() { // [attr$=value]
        XML xml = I.xml("""
                <root>
                    <item src='image.png'/>
                    <item src='script.js'/>
                    <item src='archive.png.zip'/>
                    <item src='photo.jpeg'/>
                </root>
                """);
        validateAttributeSelector(xml, 1, "[src$='.png']");
        validateAttributeSelector(xml, 1, "[src$='.js']");
        validateAttributeSelector(xml, 1, "[src$='.zip']");
        validateAttributeSelector(xml, 1, "[src$='jpeg']");
        validateAttributeSelector(xml, 0, "[src$='photo']");
    }

    @Test
    public void attributeContains() { // [attr*=value]
        XML xml = I.xml("""
                <root>
                    <item title='Chapter 1: Introduction'/>
                    <item title='Chapter 2: Details'/>
                    <item title='Summary of Chapters'/>
                    <item title='chapter three'/>
                </root>
                """);
        validateAttributeSelector(xml, 3, "[title*='Chapter']"); // Case sensitive match
        validateAttributeSelector(xml, 1, "[title*='pter 2']");
        validateAttributeSelector(xml, 0, "[title*='summary']"); // Case sensitive
        validateAttributeSelector(xml, 1, "[title*='chapter']"); // Case sensitive match
        validateAttributeSelector(xml, 0, "[title*='xyz']");
    }

    @Test
    public void attributeNameWithHyphen() {
        XML xml = I.xml("<m><e data-test='value'/><invalid/></m>");
        validateAttributeSelector(xml, 1, "[data-test='value']");
        validateAttributeSelector(xml, 1, "[data-test]");
    }

    @Test
    public void attributeNameWithUnderscore() {
        XML xml = I.xml("<m><e my_attr='value'/><invalid/></m>");
        validateAttributeSelector(xml, 1, "[my_attr='value']");
        validateAttributeSelector(xml, 1, "[my_attr]");
    }

    @Test
    public void attributeExactValue() {
        XML xml = I.xml("""
                <root>
                    <item data-id="1"></item>
                    <item data-id='2'></item>
                    <item data-id="value"></item>
                    <item data-name="test-name"></item>
                    <item data-foo="bar" data-bar='baz'></item>
                </root>
                """);
        validateAttributeSelector(xml, 1, "[data-id=1]");
        validateAttributeSelector(xml, 1, "[data-id='2']");
        validateAttributeSelector(xml, 1, "[data-id=\"value\"]");
        validateAttributeSelector(xml, 1, "item[data-name=test-name]");
        validateAttributeSelector(xml, 1, "[data-foo=\"bar\"][data-bar='baz']");
        validateAttributeSelector(xml, 0, "[data-id=nonexistent]");
    }

    @Test
    public void attributeWhitespaceSeparatedList() {
        XML xml = I.xml("""
                <root>
                    <div class="alpha beta gamma"></div>
                    <div class="beta delta"></div>
                    <div class="epsilon"></div>
                    <span class="alpha"></span>
                </root>
                """);
        validateAttributeSelector(xml, 2, "[class~=alpha]");
        validateAttributeSelector(xml, 2, "[class~=beta]");
        validateAttributeSelector(xml, 1, "[class~=gamma]");
        validateAttributeSelector(xml, 1, "[class~=epsilon]");
        validateAttributeSelector(xml, 0, "[class~=zeta]");
    }

    @Test
    public void attributeHyphenSeparatedList() {
        XML xml = I.xml("""
                <root>
                    <p lang="en"></p>
                    <p lang="en-US"></p>
                    <p lang="en-GB"></p>
                    <p lang="fr"></p>
                    <p lang="de-DE"></p>
                </root>
                """);
        validateAttributeSelector(xml, 3, "[lang|=en]");
        validateAttributeSelector(xml, 1, "[lang|=fr]");
        validateAttributeSelector(xml, 1, "[lang|=de]");
        validateAttributeSelector(xml, 1, "[lang|=de-DE]");
        validateAttributeSelector(xml, 0, "[lang|=es]");
    }

    @Test
    public void attributeSubstringContains() {
        XML xml = I.xml("""
                <root>
                    <a title="main link"></a>
                    <a title="secondary link here"></a>
                    <a title="another unrelated"></a>
                </root>
                """);
        validateAttributeSelector(xml, 2, "[title*=link]");
        validateAttributeSelector(xml, 1, "[title*=main]");
        validateAttributeSelector(xml, 1, "[title*=secondary]");
        validateAttributeSelector(xml, 1, "[title*=other]");
        validateAttributeSelector(xml, 0, "[title*=xyz]");
    }

    @Test
    public void multipleAttributes() {
        XML xml = I.xml("""
                <root>
                    <item class="widget" data-type="A" lang="en"></item>
                    <item class="widget" data-type="B" lang="fr"></item>
                    <item class="tool" data-type="A" lang="en"></item>
                    <item class="widget" data-type="A"></item> <!-- No lang -->
                </root>
                """);
        validateAttributeSelector(xml, 2, "item[class=widget][data-type=A]");
        validateAttributeSelector(xml, 1, "item[class=widget][lang=en]");
        validateAttributeSelector(xml, 2, "item[data-type=A][lang=en]");
        validateAttributeSelector(xml, 1, "item[class=widget][data-type=A][lang=en]");
    }

    @Test
    public void multipleAttributeSelectors() {
        XML xml = I.xml("""
                <root>
                    <item name='one' type='a'/>
                    <item name='one' type='b'/>
                    <item name='two' type='a'/>
                    <item name='one'/>
                </root>
                """);

        validateAttributeSelector(xml, 1, "[name='one'][type='a']");
        validateAttributeSelector(xml, 1, "[name='one'][type='b']");
        validateAttributeSelector(xml, 0, "[name='one'][type='c']");
        validateAttributeSelector(xml, 3, "[name][type]");
    }

    @Test
    public void tagAndAttributeSelector() {
        XML xml = I.xml("""
                <root>
                    <item class='target'>Item 1</item>
                    <div class='target'>Div 1</div>
                    <item>Item 2</item>
                    <item class='other'>Item 3</item>
                </root>
                """);
        validateAttributeSelector(xml, 1, "item[class='target']");
        validateAttributeSelector(xml, 1, "div[class='target']");
        validateAttributeSelector(xml, 1, "item[class='other']");
        validateAttributeSelector(xml, 0, "span[class='target']");
    }

    @Test
    public void caseSensitivityOfAttributeNamesAndValues() {
        XML xml = I.xml("""
                <root>
                    <item dataName='valueOne' data-value='ValueA'/>
                    <item dataname='valueTwo' data-value='valuea'/>
                </root>
                """);
        // Attribute names in XML are case-sensitive.
        // CSS attribute selectors are case-sensitive for names.
        validateAttributeSelector(xml, 1, "[dataName]");
        validateAttributeSelector(xml, 1, "[dataname]");
        validateAttributeSelector(xml, 0, "[DATANAME]");

        // Attribute values in CSS selectors are case-sensitive by default.
        validateAttributeSelector(xml, 1, "[data-value='ValueA']");
        validateAttributeSelector(xml, 1, "[data-value='valuea']");
        validateAttributeSelector(xml, 0, "[data-value='VALUEA']");

        // Test with different types of selectors
        validateAttributeSelector(xml, 1, "[dataName^='value']");
        validateAttributeSelector(xml, 0, "[dataName^='VALUE']");

        validateAttributeSelector(xml, 1, "[data-value*='lueA']");
        validateAttributeSelector(xml, 1, "[data-value*='luea']");
        validateAttributeSelector(xml, 0, "[data-value*='LUEA']");
    }

    @Test
    public void attributeValueWithSpaces() {
        XML xml = I.xml("""
                <root>
                    <item title='hello world'/>
                    <item title='helloworld'/>
                </root>
                """);
        validateAttributeSelector(xml, 1, "[title='hello world']");
        validateAttributeSelector(xml, 1, "[title*='hello w']");
        validateAttributeSelector(xml, 1, "[title~='hello']");
        validateAttributeSelector(xml, 1, "[title~='world']");
    }

    @Test
    public void attributeValueNS() {
        XML xml = I.xml("""
                <m xmlns:p='p' xmlns:z='z'>
                    <e p:A='a'/>
                    <e p:A='b'/>
                    <e z:A='a'/>
                    <e A='b'/>
                </m>
                """);

        // variants for white space
        validateAttributeSelector(xml, 2, "[A='a']");
    }

    @Test
    public void attributeConatainValue() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AA BB CC'/></m>");

        validateAttributeSelector(xml, 1, "[A ~= 'A']");
        validateAttributeSelector(xml, 1, "[A ~= 'B']");
        validateAttributeSelector(xml, 1, "[A ~= 'C']");
    }

    @Test
    public void attributeConatainText() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AB'/></m>");

        validateAttributeSelector(xml, 2, "[A *= 'A']");
        validateAttributeSelector(xml, 2, "[A *= 'B']");
        validateAttributeSelector(xml, 1, "[A *= 'C']");
    }

    @Test
    public void attributeStartWith() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>");

        validateAttributeSelector(xml, 2, "[A ^= 'A']");
        validateAttributeSelector(xml, 2, "[A^= 'A']");
        validateAttributeSelector(xml, 2, "[A ^='A']");
        validateAttributeSelector(xml, 2, "[A^='A']");
        validateAttributeSelector(xml, 0, "[A ^= 'B']");
        validateAttributeSelector(xml, 0, "[A ^= 'C']");
    }

    @Test
    public void attributeEndWith() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>");

        validateAttributeSelector(xml, 0, "[A $= 'A']");
        validateAttributeSelector(xml, 0, "[A $= 'B']");
        validateAttributeSelector(xml, 2, "[A $= 'C']");
        validateAttributeSelector(xml, 2, "[A $='C']");
        validateAttributeSelector(xml, 2, "[A$= 'C']");
        validateAttributeSelector(xml, 2, "[A$='C']");
    }
}