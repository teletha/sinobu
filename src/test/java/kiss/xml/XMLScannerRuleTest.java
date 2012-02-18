/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import static antibug.AntiBug.*;
import static antibug.Ezunit.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import kiss.I;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import antibug.util.Note;
import antibug.xml.XML;

/**
 * @version 2011/03/22 17:22:31
 */
public class XMLScannerRuleTest {

    @Test
    public void withoutProceed() throws Exception {
        XML xml = xml("<root/>", new WithoutProceed());
        XML expect = xml("<added/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void withoutProceedWithChild() throws Exception {
        XML xml = xml("<root><child/><child/></root>", new WithoutProceed());
        XML expect = xml("<added/>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * @version 2012/02/18 10:21:54
     */
    private static class WithoutProceed extends XMLScanner {

        @Rule(match = "root")
        @SuppressWarnings("unused")
        public void root(Attributes atts) throws Exception {
            start("added", atts);
            end();
        }
    }

    @Test
    public void namespace() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS = "default";

            @SuppressWarnings("unused")
            public static final String XMLNS_TEST = "test";

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child1(Attributes atts) throws SAXException {
                start("default", atts);
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "test:child")
            public void child2(Attributes atts) throws SAXException {
                start("test:test", atts);
                end();
            }
        };

        XML xml = xml("" +
        /**/"<root xmlns='default' xmlns:test='test' xmlns:alias='default'>" +
        /**/"   <child />" +
        /**/"   <test:child />" +
        /**/"   <alias:child />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root xmlns='default' xmlns:test='test' xmlns:alias='default'>" +
        /**/"   <default />" +
        /**/"   <test:test />" +
        /**/"   <default />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void addNewNamespace() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS_NEW = "new";

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child1(Attributes atts) throws SAXException {
                start("new:child", atts);
                end();
            }
        };

        XML xml = xml("" +
        /**/"<root >" +
        /**/"   <child />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root xmlns:new='new'>" +
        /**/"   <new:child />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * Use overrided namespace in helper method.
     */
    @Test
    public void overridedNamespaceByUsingSaxAPI() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS_NS = "uri";

            @SuppressWarnings("unused")
            @Rule(match = "item")
            public void test() throws SAXException {
                startPrefixMapping("ns", "override");
                start("ns:item");
                end();
                endPrefixMapping("ns");
            }
        };

        XML xml = xml("<item/>", scanner);
        XML expect = xml("<ns:item xmlns:ns='override'/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void ruleCanUseMethodNameAsMatchinPattern() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS_NEW = "new";

            @SuppressWarnings("unused")
            @Rule
            public void child(Attributes atts) throws SAXException {
                start("new:child", atts);
                end();
            }
        };

        XML xml = xml("" +
        /**/"<root >" +
        /**/"   <child />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root xmlns:new='new'>" +
        /**/"   <new:child />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void callSaxApiFromRule() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS_NEW = "new";

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "test", "test", atts);
                endElement("", "test", "test");
            }
        };

        XML xml = xml("" +
        /**/"<root >" +
        /**/"   <child />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root>" +
        /**/"   <test />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void universalName() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root/*")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "match", "match", atts);
                endElement("", "match", "match");
            }
        };

        XML xml = xml("" +
        /**/"<root >" +
        /**/"   <item1 />" +
        /**/"   <item2 />" +
        /**/"   <item3 />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root>" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void universalPrefix() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root/*:item")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "match", "match", atts);
                endElement("", "match", "match");
            }
        };

        XML xml = xml("" +
        /**/"<root >" +
        /**/"   <item xmlns='test1'/>" +
        /**/"   <not xmlns='test2'/>" +
        /**/"   <item xmlns='test3'/>" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root>" +
        /**/"   <match />" +
        /**/"   <not xmlns='test2'/>" +
        /**/"   <match />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void universalLocalName() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS_TEST = "test";

            @SuppressWarnings("unused")
            @Rule(match = "root/test:*")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "match", "match", atts);
                endElement("", "match", "match");
            }
        };

        XML xml = xml("" +
        /**/"<root xmlns:test='test'>" +
        /**/"   <test:item />" +
        /**/"   <test:child />" +
        /**/"   <item />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root>" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"   <item />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void universalBoth() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final transient String XMLNS_OTHER = "other";

            @SuppressWarnings("unused")
            @Rule(match = "root/*:*")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "match", "match", atts);
                endElement("", "match", "match");
            }
        };

        XML xml = xml("" +
        /**/"<root xmlns:test='test'>" +
        /**/"   <test:item />" +
        /**/"   <test:child />" +
        /**/"   <item />" +
        /**/"   <item xmlns='other'/>" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root>" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void noParameter() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root/item")
            public void item() throws SAXException {
                startElement("", "match", "match", new AttributesImpl());
                endElement("", "match", "match");
            }
        };

        XML xml = xml("" +
        /**/"<root>" +
        /**/"   <item position='1' />" +
        /**/"   <item position='2' />" +
        /**/"   <item position='3' />" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root>" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"   <match />" +
        /**/"</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void priority() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "test", priority = 10)
            public void middle() throws SAXException {
                start("middle", new AttributesImpl());
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "test", priority = 1)
            public void low() throws SAXException {
                start("low", new AttributesImpl());
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "test", priority = 100)
            public void high() throws SAXException {
                start("high", new AttributesImpl());
                end();
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<high/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void prioriteDefault() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "test")
            public void high() throws SAXException {
                start("high", new AttributesImpl());
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "test", priority = -1)
            public void low() throws SAXException {
                start("low", new AttributesImpl());
                end();
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<high/>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * Test exclude-result-prefixes.
     */
    @Test
    public void excludeResultPrefix() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS$EXCLUDED = "excluded";

            @Rule(match = "excluded:item")
            @SuppressWarnings("unused")
            public void item() throws SAXException {
                start("item", new AttributesImpl());
                end();
            }
        };

        XML xml = xml("<excluded:item xmlns:excluded='excluded'/>", scanner);
        XML expect = xml("<item/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void start() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "test")
            public void test() throws SAXException {
                start("new");
                end();
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void startWithAttributes() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "test")
            public void test() throws SAXException {
                start("new", "title", "test");
                end();
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new title='test'/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void startWithInvalidAttribute() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                start("new", "ignored");
                end();
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new/>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * Use defiened namespace in helper method.
     */
    @Test
    public void startWithPrefix() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS_NS = "uri";

            @Rule(match = "item")
            public void test() throws SAXException {
                start("ns:item");
                end();
            }
        };

        XML xml = xml("<item/>", scanner);
        XML expect = xml("<ns:item xmlns:ns='uri'/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void ruleIsInheritable() throws Exception {

        /**
         * @version 2011/04/11 14:39:34
         */
        @SuppressWarnings("unused")
        class Parent extends XMLScanner {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new");
            }
        }

        /**
         * @version 2011/04/11 14:39:37
         */
        class Child extends Parent {
        }

        XML xml = xml("<test/>", new Child());
        XML expect = xml("<new/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void element() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new");
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new/>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * Test element helper method.
     */
    @Test
    public void elementWithAttributes() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new", "name", "value");
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new name='value'/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void elementWithContents() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new", "text");
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new>text</new>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void elementWithAttributesAndContents() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new", "name", "value", "text");
            }
        };

        XML xml = xml("<test/>", scanner);
        XML expect = xml("<new name='value'>text</new>");

        assert xml.isIdenticalTo(expect);
    }

    @Test(expected = SAXException.class)
    public void throwCheckedExceptionInRuleMethod() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "dummy")
            public void test() throws SAXException {
                throw new SAXException();
            }
        };
        I.parse(locateSource("dummy.xml"), scanner);
    }

    @Test(expected = ArithmeticException.class)
    public void throwUncheckedExceptionInRuleMethod() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "dummy")
            public void test() throws SAXException {
                throw new ArithmeticException();
            }
        };
        I.parse(locateSource("dummy.xml"), scanner);
    }

    @Test(expected = FactoryConfigurationError.class)
    public void throwErroInRuleMethod() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "dummy")
            public void test() throws SAXException {
                throw new FactoryConfigurationError();
            }
        };
        I.parse(locateSource("dummy.xml"), scanner);
    }

    @Test
    public void proceedText() throws Exception {
        XML xml = xml("<root>text</root>", new WithProceed());
        XML expect = xml("<added>text</added>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void proceedElement() throws Exception {
        XML xml = xml("<root><child/></root>", new WithProceed());
        XML expect = xml("<added><child/></added>");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * @version 2012/02/18 10:21:57
     */
    private static class WithProceed extends XMLScanner {

        @Rule(match = "root")
        @SuppressWarnings("unused")
        public void root(Attributes atts) throws Exception {
            start("added", atts);
            proceed();
            end();
        }
    }

    @Test
    public void processNest() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void root(Attributes atts) throws SAXException {
                start("root-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule
            public void child(Attributes atts) throws SAXException {
                start("child-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule
            public void item(Attributes atts) throws SAXException {
                start("item-change", atts);
                proceed();
                end();
            }
        };

        XML xml = xml("" +
        /**/"<root>" +
        /**/"   <child>" +
        /**/"       <item />" +
        /**/"       <item name='value' />" +
        /**/"   </child>" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root-change>" +
        /**/"   <child-change>" +
        /**/"       <item-change />" +
        /**/"       <item-change name='value' />" +
        /**/"   </child-change>" +
        /**/"</root-change>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void processNestForNestedElement() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void root(Attributes atts) throws SAXException {
                start("root-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule
            public void child(Attributes atts) throws SAXException {
                start("child-change", atts);
                proceed();
                end();
            }
        };

        XML xml = xml("" +
        /**/"<root>" +
        /**/"   <child>" +
        /**/"       <child />" +
        /**/"       <child>text</child>" +
        /**/"   </child>" +
        /**/"</root>", scanner);

        XML expect = xml("" +
        /**/"<root-change>" +
        /**/"   <child-change>" +
        /**/"       <child-change />" +
        /**/"       <child-change>text</child-change>" +
        /**/"   </child-change>" +
        /**/"</root-change>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void textContents() throws Exception {
        XML xml = xml("<root>contents</root>", new UseContents());
        XML expect = xml("<contents />");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void joinAllTextContents() throws Exception {
        XML xml = xml("<root><child>cont</child><child>ents</child></root>", new UseContents());
        XML expect = xml("<contents />");

        assert xml.isIdenticalTo(expect);
    }

    /**
     * @version 2012/02/18 10:22:01
     */
    private static class UseContents extends XMLScanner {

        @Rule
        @SuppressWarnings("unused")
        public void root(String contents, Attributes atts) throws Exception {
            start(contents, atts);
            end();
        }
    }

    @Test
    public void processWithContents() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @Rule
            @SuppressWarnings("unused")
            public void root(String contents, AttributesImpl atts) throws Exception {
                atts.addAttribute("", "title", "title", "CDATA", contents);

                start("root", atts);
                proceed();
                end();
            }
        };

        XML xml = xml("<root>title</root>", scanner);
        XML expect = xml("<root title='title'>title</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void clear() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void item() throws SAXException {
                // remoce all contents
            }
        };

        XML xml = xml("<root><item>title</item></root>", scanner);
        XML expect = xml("<root/>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void text() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void item() throws SAXException {
                text("text");
            }
        };

        XML xml = xml("<root><item/></root>", scanner);
        XML expect = xml("<root>text</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void textForSaxAPI() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void item() throws SAXException {
                characters("text".toCharArray(), 0, 4);
            }
        };

        XML xml = xml("<root><item/></root>", scanner);
        XML expect = xml("<root>text</root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void noRules() throws Exception {
        XMLScanner scanner = new XMLScanner() {
            // pass all
        };

        XML xml = xml("<root><item/></root>", scanner);
        XML expect = xml("<root><item/></root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void unusedRules() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void unused() throws SAXException {
                // do nothing
            }
        };

        XML xml = xml("<root><item/></root>", scanner);
        XML expect = xml("<root><item/></root>");

        assert xml.isIdenticalTo(expect);
    }

    @Test
    public void multiplePatterns() throws Exception {
        final List<String> events = new ArrayList();

        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "item1 item2")
            public void root() throws SAXException {
                events.add("matched");
            }
        };

        I.parse(note("<m><item1/><item2/></m>"), scanner);

        assert events.size() == 2;
    }

    @Test
    public void bits() throws Exception {
        final List events = new ArrayList();

        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule
            public void root(Bits bits) throws SAXException {
                events.addAll(bits.bits);
            }
        };

        I.parse(note("<root/>"), scanner);

        assert events.size() == 0;
    }

    @Test
    public void bitsWithChild() throws Exception {
        final List events = new ArrayList();

        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Bits bits) throws SAXException {
                events.addAll(bits.bits);
            }
        };

        I.parse(note("<root><child/><child/></root>"), scanner);

        assert events.size() == 4;
    }

    /**
     * Private rule class. (Same package of {@link XMLScanner})
     */
    @Test
    public void testPrivateClass() throws Exception {
        assert new PrivateRuleScanner() != null;
    }

    /**
     * @version 2012/02/18 12:59:35
     */
    private static class PrivateRuleScanner extends XMLScanner {
    }

    @Test(expected = RuntimeException.class)
    public void terminator() throws Exception {
        Terminator scanner = new Terminator();

        Note xml = note("" +
        /**/"<root >" +
        /**/"   <reach />" +
        /**/"   <reach />" +
        /**/"   <unreach />" +
        /**/"   <unreach />" +
        /**/"</root>");

        I.parse(xml, scanner);

        assert scanner.count == 0;
    }

    /**
     * @version 2012/02/18 10:22:10
     */
    private class Terminator extends XMLScanner {

        private int count = 0;

        @Rule(match = "root/reach")
        @SuppressWarnings("unused")
        public void reach() throws SAXException {
            throw new RuntimeException();
        }

        @Rule(match = "root/unreach")
        @SuppressWarnings("unused")
        public void unreach() throws SAXException {
            count++;
        }
    }

    @Test
    public void invalidUnknownNamespacePrefix() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "unknown:namespace")
            public void rule(Attributes atts) throws SAXException {
                // this method will be ignored
            }
        };
    }

    @Test
    public void invalidParameterType() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(int type) throws SAXException {
                // this method will be ignored
            }
        };
    }

    @Test
    public void invalidParameterTypes1() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(String contents, boolean test) throws SAXException {
                // this method will be ignored
            }
        };
    }

    @Test
    public void invalidParameterTypes2() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(String contents, Attributes atts, boolean test) throws SAXException {
                // this method will be ignored
            }
        };
    }

    @Test
    public void invalidParameterOrder() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(Attributes atts, String contents) throws SAXException {
                // this method will be ignored
            }
        };
    }
}
