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

public class AttributeTest {

    @Test
    public void attribute() {
        XML xml = I.xml("""
                <m>
                    <e A='one' B='one'/>
                    <e A='two' B='two'/>
                </m>
                """);

        assert xml.find("[A]").size() == 2;
        assert xml.find("[A=one]").size() == 1;
    }

    @Test
    public void attributeNS() {
        XML xml = I.xml("<m xmlns:p='p'><e p:A='A' B='B'/><e A='B' p:B='A'/></m>");

        assert select(xml, 2, "[A]");
        assert select(xml, 2, "[B]");
    }

    @Test
    public void attributeValue() {
        XML xml = I.xml("<m><e A='a'/><e A='B'/></m>");

        assert select(xml, 1, "[A='a']");
    }

    @Test
    public void attributeSingleQuote() {
        XML xml = I.xml("<m><e A='A'/><e A='B'/></m>");

        assert select(xml, 1, "[A='A']");
    }

    @Test
    public void attributeWithoutQuote() {
        XML xml = I.xml("<m><e A='A'/><e A='B'/></m>");

        assert select(xml, 1, "[A=A]");
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
        assert select(xml, 2, "[name]");
        assert select(xml, 1, "[id]");
        assert select(xml, 1, "[class]");
        assert select(xml, 0, "[unknown]");
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
        assert select(xml, 1, "[name='one']");
        assert select(xml, 1, "[name='two']");
        assert select(xml, 1, "[name='one two']");
        assert select(xml, 1, "[name='ONE']");
        assert select(xml, 0, "[name='three']");
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
        assert select(xml, 3, "[class~='foo']");
        assert select(xml, 2, "[class~='bar']");
        assert select(xml, 1, "[class~='baz']");
        assert select(xml, 1, "[class~='foobar']");

        // word
        assert select(xml, 1, "[class~='foo-bar']");
        assert select(xml, 1, "[class~='other']");
        assert select(xml, 0, "[class~='b']"); // "b" is not a whole word
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
        assert select(xml, 3, "[lang|='en']");
        assert select(xml, 1, "[lang|='fr']");
        assert select(xml, 1, "[lang|='english']");
        assert select(xml, 0, "[lang|='e']");
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
        assert select(xml, 2, "[href^='http://']");
        assert select(xml, 1, "[href^='https://']");
        assert select(xml, 0, "[href^='example']");
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
        assert select(xml, 1, "[src$='.png']");
        assert select(xml, 1, "[src$='.js']");
        assert select(xml, 1, "[src$='.zip']");
        assert select(xml, 1, "[src$='jpeg']");
        assert select(xml, 0, "[src$='photo']");
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
        assert select(xml, 3, "[title*='Chapter']"); // Case sensitive match
        assert select(xml, 1, "[title*='pter 2']");
        assert select(xml, 0, "[title*='summary']"); // Case sensitive
        assert select(xml, 1, "[title*='chapter']"); // Case sensitive match
        assert select(xml, 0, "[title*='xyz']");
    }

    @Test
    public void attributeNameWithHyphen() {
        XML xml = I.xml("<m><e data-test='value'/><invalid/></m>");
        assert select(xml, 1, "[data-test='value']");
        assert select(xml, 1, "[data-test]");
    }

    @Test
    public void attributeNameWithUnderscore() {
        XML xml = I.xml("<m><e my_attr='value'/><invalid/></m>");
        assert select(xml, 1, "[my_attr='value']");
        assert select(xml, 1, "[my_attr]");
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
        assert select(xml, 1, "[data-id=1]");
        assert select(xml, 1, "[data-id='2']");
        assert select(xml, 1, "[data-id=\"value\"]");
        assert select(xml, 1, "item[data-name=test-name]");
        assert select(xml, 1, "[data-foo=\"bar\"][data-bar='baz']");
        assert select(xml, 0, "[data-id=nonexistent]");
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
        assert select(xml, 2, "[class~=alpha]");
        assert select(xml, 2, "[class~=beta]");
        assert select(xml, 1, "[class~=gamma]");
        assert select(xml, 1, "[class~=epsilon]");
        assert select(xml, 0, "[class~=zeta]");
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
        assert select(xml, 3, "[lang|=en]");
        assert select(xml, 1, "[lang|=fr]");
        assert select(xml, 1, "[lang|=de]");
        assert select(xml, 1, "[lang|=de-DE]");
        assert select(xml, 0, "[lang|=es]");
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
        assert select(xml, 2, "[title*=link]");
        assert select(xml, 1, "[title*=main]");
        assert select(xml, 1, "[title*=secondary]");
        assert select(xml, 1, "[title*=other]");
        assert select(xml, 0, "[title*=xyz]");
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
        assert select(xml, 2, "item[class=widget][data-type=A]");
        assert select(xml, 1, "item[class=widget][lang=en]");
        assert select(xml, 2, "item[data-type=A][lang=en]");
        assert select(xml, 1, "item[class=widget][data-type=A][lang=en]");
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

        assert select(xml, 1, "[name='one'][type='a']");
        assert select(xml, 1, "[name='one'][type='b']");
        assert select(xml, 0, "[name='one'][type='c']");
        assert select(xml, 3, "[name][type]");
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
        assert select(xml, 1, "item[class='target']");
        assert select(xml, 1, "div[class='target']");
        assert select(xml, 1, "item[class='other']");
        assert select(xml, 0, "span[class='target']");
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
        assert select(xml, 1, "[dataName]");
        assert select(xml, 1, "[dataname]");
        assert select(xml, 0, "[DATANAME]");

        // Attribute values in CSS selectors are case-sensitive by default.
        assert select(xml, 1, "[data-value='ValueA']");
        assert select(xml, 1, "[data-value='valuea']");
        assert select(xml, 0, "[data-value='VALUEA']");

        // Test with different types of selectors
        assert select(xml, 1, "[dataName^='value']");
        assert select(xml, 0, "[dataName^='VALUE']");

        assert select(xml, 1, "[data-value*='lueA']");
        assert select(xml, 1, "[data-value*='luea']");
        assert select(xml, 0, "[data-value*='LUEA']");
    }

    @Test
    public void attributeValueWithSpaces() {
        XML xml = I.xml("""
                <root>
                    <item title='hello world'/>
                    <item title='helloworld'/>
                </root>
                """);
        assert select(xml, 1, "[title='hello world']");
        assert select(xml, 1, "[title*='hello w']");
        assert select(xml, 1, "[title~='hello']");
        assert select(xml, 1, "[title~='world']");
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
        assert select(xml, 2, "[A='a']");
    }

    @Test
    public void attributeConatainValue() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AA BB CC'/></m>");

        assert select(xml, 1, "[A ~= 'A']");
        assert select(xml, 1, "[A ~= 'B']");
        assert select(xml, 1, "[A ~= 'C']");
    }

    @Test
    public void attributeConatainText() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AB'/></m>");

        assert select(xml, 2, "[A *= 'A']");
        assert select(xml, 2, "[A *= 'B']");
        assert select(xml, 1, "[A *= 'C']");
    }

    @Test
    public void attributeStartWith() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>");

        assert select(xml, 2, "[A ^= 'A']");
        assert select(xml, 2, "[A^= 'A']");
        assert select(xml, 2, "[A ^='A']");
        assert select(xml, 2, "[A^='A']");
        assert select(xml, 0, "[A ^= 'B']");
        assert select(xml, 0, "[A ^= 'C']");
    }

    @Test
    public void attributeEndWith() {
        XML xml = I.xml("<m><e A='A B C'/><e A='AA BB CC'/><e A='D'/></m>");

        assert select(xml, 0, "[A $= 'A']");
        assert select(xml, 0, "[A $= 'B']");
        assert select(xml, 2, "[A $= 'C']");
        assert select(xml, 2, "[A $='C']");
        assert select(xml, 2, "[A$= 'C']");
        assert select(xml, 2, "[A$='C']");
    }
}