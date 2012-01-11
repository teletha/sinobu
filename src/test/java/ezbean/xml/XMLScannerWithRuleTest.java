/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.xml;

import static ezunit.Ezunit.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import ezbean.I;

/**
 * @version 2011/03/22 17:22:31
 */
public class XMLScannerWithRuleTest {

    /**
     * Without pursue.
     */
    @Test
    public void testRule1() throws Exception {
        assertXMLIdentical("rule/expected01.xml", "rule/test01.xml", new WithoutPursue());
    }

    /**
     * Without pursue.
     */
    @Test
    public void testRule2() throws Exception {
        assertXMLIdentical("rule/expected02.xml", "rule/test02.xml", new WithoutPursue());
    }

    /**
     * Test namespace.
     */
    @Test
    public void testRule3() throws Exception {
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

        assertXMLIdentical("rule/expected03.xml", "rule/test03.xml", scanner);
    }

    /**
     * Test namespace.
     */
    @Test
    public void testRule4() throws Exception {
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

        assertXMLIdentical("rule/expected04.xml", "rule/test04.xml", scanner);
    }

    @Test
    public void useMethodName() throws Exception {
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

        assertXMLIdentical("rule/expected04.xml", "rule/test04.xml", scanner);
    }

    /**
     * Test direct call.
     */
    @Test
    public void testRule5() throws Exception {
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

        assertXMLIdentical("rule/expected05.xml", "rule/test05.xml", scanner);
    }

    /**
     * Test universal match.
     */
    @Test
    public void testRule6() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root/*")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "match", "match", atts);
                endElement("", "match", "match");
            }
        };

        assertXMLIdentical("rule/expected06.xml", "rule/test06.xml", scanner);
    }

    /**
     * Test universal match.
     */
    @Test
    public void testRule7() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final transient String XMLNS_TEST1 = "test1";

            @SuppressWarnings("unused")
            public static final transient String XMLNS_TEST2 = "test2";

            @SuppressWarnings("unused")
            public static final transient String XMLNS_TEST3 = "test3";

            @SuppressWarnings("unused")
            @Rule(match = "root/*:item")
            public void child1(Attributes atts) throws SAXException {
                startElement("", "match", "match", atts);
                endElement("", "match", "match");
            }
        };

        assertXMLIdentical("rule/expected07.xml", "rule/test07.xml", scanner);
    }

    /**
     * Test universal match.
     */
    @Test
    public void testRule8() throws Exception {
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

        assertXMLIdentical("rule/expected08.xml", "rule/test08.xml", scanner);
    }

    /**
     * Test universal match.
     */
    @Test
    public void testRule9() throws Exception {
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

        assertXMLIdentical("rule/expected09.xml", "rule/test09.xml", scanner);
    }

    /**
     * Test rule without parameter.
     */
    @Test
    public void testRule10() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root/item")
            public void item() throws SAXException {
                startElement("", "match", "match", new AttributesImpl());
                endElement("", "match", "match");
            }
        };

        assertXMLIdentical("rule/expected10.xml", "rule/test10.xml", scanner);
    }

    /**
     * Test rule priority.
     */
    @Test
    public void testRule11() throws Exception {
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

        assertXMLIdentical("rule/expected11.xml", "rule/test11.xml", scanner);
    }

    /**
     * Test rule priority.
     */
    @Test
    public void testRule12() throws Exception {
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

        assertXMLIdentical("rule/expected12.xml", "rule/test12.xml", scanner);
    }

    /**
     * Test exclude-result-prefixes.
     */
    @Test
    public void testRule13() throws Exception {
        assertXMLIdentical("rule/expected13.xml", "rule/test13.xml", new ExcludeResultPrefixes());
    }

    /**
     * Test startElement with attributes strings.
     */
    @Test
    public void testRule14() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "test")
            public void test() throws SAXException {
                start("new", "title", "test");
                end();
            }
        };

        assertXMLIdentical("rule/expected14.xml", "rule/test14.xml", scanner);
    }

    /**
     * Test startElement without attributes strings.
     */
    @Test
    public void testRule15() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "test")
            public void test() throws SAXException {
                start("new");
                end();
            }
        };

        assertXMLIdentical("rule/expected15.xml", "rule/test15.xml", scanner);
    }

    /**
     * Test startElement with invalid attributes strings.
     */
    @Test
    public void testRule16() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                start("new", "ignored");
                end();
            }
        };

        assertXMLIdentical("rule/expected16.xml", "rule/test16.xml", scanner);
    }

    /**
     * Test element helper method.
     */
    @Test
    public void testElement() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new");
            }
        };

        assertXMLIdentical("rule/expected17.xml", "rule/test17.xml", scanner);
    }

    @Test
    public void parentRule() throws Exception {

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

        assertXMLIdentical("rule/expected17.xml", "rule/test17.xml", new Child());
    }

    /**
     * Test element helper method.
     */
    @Test
    public void testElementWithAttribute() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new", "name", "value");
            }
        };

        assertXMLIdentical("rule/expected18.xml", "rule/test18.xml", scanner);
    }

    /**
     * Test element helper method.
     */
    @Test
    public void testElementWithAttributeAndContent() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new", "name", "value", "text");
            }
        };

        assertXMLIdentical("rule/expected22.xml", "rule/test22.xml", scanner);
    }

    /**
     * Test element helper method.
     */
    @Test
    public void testElementWithContent() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                element("new", "text");
            }
        };

        assertXMLIdentical("rule/expected23.xml", "rule/test23.xml", scanner);
    }

    /**
     * Test pass-through namespace..
     */
    @Test
    public void testRule19() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "test")
            public void test() throws SAXException {
                start("new");
                end();
            }
        };

        assertXMLIdentical("rule/expected19.xml", "rule/test19.xml", scanner);
    }

    /**
     * Rule method throws checked exception.
     */
    @Test(expected = SAXException.class)
    public void testCheckedExceptionInRuleMethod() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "dummy")
            public void test() throws SAXException {
                throw new SAXException();
            }
        };
        I.parse(locateSource("dummy.xml"), scanner);
    }

    /**
     * Rule method throws unchecked exception.
     */
    @Test(expected = ArithmeticException.class)
    public void testUncheckedExceptionInRuleMethod() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "dummy")
            public void test() throws SAXException {
                throw new ArithmeticException();
            }
        };
        I.parse(locateSource("dummy.xml"), scanner);
    }

    /**
     * Rule method throws unchecked exception.
     */
    @Test(expected = FactoryConfigurationError.class)
    public void testErroInRuleMethod() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            @Rule(match = "dummy")
            public void test() throws SAXException {
                throw new FactoryConfigurationError();
            }
        };
        I.parse(locateSource("dummy.xml"), scanner);
    }

    /**
     * Use defiened namespace in helper method.
     */
    @Test
    public void testDefinedNamespace() throws Exception {
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS_NS = "uri";

            @Rule(match = "item")
            public void test() throws SAXException {
                start("ns:item");
                end();
            }
        };
        assertXMLIdentical("rule/expected20.xml", "rule/test20.xml", scanner);
    }

    /**
     * Use overrided namespace in helper method.
     */
    @Test
    public void testOverridedNamespace() throws Exception {
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
        assertXMLIdentical("rule/expected21.xml", "rule/test21.xml", scanner);
    }

    /**
     * With pursue.
     */
    @Test
    public void testRule30() throws Exception {
        assertXMLIdentical("rule/expected30.xml", "rule/test30.xml", new WithPursue());
    }

    /**
     * With pursue.
     */
    @Test
    public void testRule31() throws Exception {
        assertXMLIdentical("rule/expected31.xml", "rule/test31.xml", new WithPursue());
    }

    /**
     * With pursue.
     */
    @Test
    public void testRule32() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Attributes atts) throws SAXException {
                start("root-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child(Attributes atts) throws SAXException {
                start("child-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "item")
            public void item(Attributes atts) throws SAXException {
                start("item-change", atts);
                proceed();
                end();
            }
        };

        assertXMLIdentical("rule/expected32.xml", "rule/test32.xml", scanner);
    }

    /**
     * Use contents.
     */
    @Test
    public void testRule60() throws Exception {
        assertXMLIdentical("rule/expected60.xml", "rule/test60.xml", new UseContents());
    }

    /**
     * Use contents.
     */
    @Test
    public void testRule61() throws Exception {
        assertXMLIdentical("rule/expected61.xml", "rule/test61.xml", new UseContents());
    }

    /**
     * Use contents with pursue.
     */
    @Test
    public void testRule62() throws Exception {
        assertXMLIdentical("rule/expected62.xml", "rule/test62.xml", new UseContentsWithPursue());
    }

    /**
     * Nest.
     */
    @Test
    public void testRule63() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Attributes atts) throws SAXException {
                start("root-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child(Attributes atts) throws SAXException {
                start("child-change", atts);
                proceed();
                end();
            }
        };

        assertXMLIdentical("rule/expected63.xml", "rule/test63.xml", scanner);
    }

    /**
     * Nest.
     */
    @Test
    public void testRule64() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Attributes atts) throws SAXException {
                start("root-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child(Attributes atts) throws SAXException {
                start("child-change", atts);
                proceed();
                end();
            }
        };

        assertXMLIdentical("rule/expected64.xml", "rule/test64.xml", scanner);
    }

    /**
     * Nest.
     */
    @Test
    public void testRule65() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Attributes atts) throws SAXException {
                start("root-change", atts);
                proceed();
                end();
            }

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child(String contents, AttributesImpl atts) throws SAXException {
                atts.addAttribute("", "contents", "contents", "CDATA", contents);

                start("child-change", atts);
                proceed();
                end();
            }
        };

        assertXMLIdentical("rule/expected65.xml", "rule/test65.xml", scanner);
    }

    /**
     * Text removal.
     */
    @Test
    public void testRule66() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "text")
            public void text() throws SAXException {
                // remoce all contents
            }
        };

        assertXMLIdentical("rule/expected66.xml", "rule/test66.xml", scanner);
    }

    /**
     * Test characters.
     */
    @Test
    public void testRule67A() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "text")
            public void text() throws SAXException {
                text("text");
            }
        };

        assertXMLIdentical("rule/expected67.xml", "rule/test67.xml", scanner);
    }

    /**
     * Test characters.
     */
    @Test
    public void testRule67B() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "text")
            public void text() throws SAXException {
                characters("text".toCharArray(), 0, 4);
            }
        };

        assertXMLIdentical("rule/expected67.xml", "rule/test67.xml", scanner);
    }

    /**
     * Do nothing Test.
     */
    @Test
    public void testRule68A() throws Exception {
        XMLScanner scanner = new XMLScanner() {
            // pass all
        };

        assertXMLIdentical("rule/expected68.xml", "rule/test68.xml", scanner);
    }

    /**
     * Do nothing Test.
     */
    @Test
    public void testRule68B() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "unused")
            public void unused() throws SAXException {
                // do nothing
            }
        };

        assertXMLIdentical("rule/expected68.xml", "rule/test68.xml", scanner);
    }

    @Test
    public void multipleValue() throws Exception {
        final List<String> events = new ArrayList();

        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "item1 item2")
            public void root() throws SAXException {
                events.add("matched");
            }
        };

        I.parse(locateSource("rule/test06.xml"), scanner);

        assert events.size() == 2;
    }

    @Test
    public void bits() throws Exception {
        final List events = new ArrayList();

        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(Bits bits) throws SAXException {
                events.addAll(bits.bits);
            }
        };

        I.parse(locateSource("rule/test01.xml"), scanner);

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

        I.parse(locateSource("rule/test02.xml"), scanner);

        assert events.size() == 7;
    }

    @Test
    public void invokeProceedMethodForChildRule() throws Exception {
        final List events = new ArrayList();

        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root() throws SAXException {
                events.add(1);

                proceed();
            }

            @SuppressWarnings("unused")
            @Rule(match = "child")
            public void child() throws SAXException {
                events.add(1);
            }
        };

        I.parse(locateSource("rule/test02.xml"), scanner);

        assert events.size() == 3;
    }

    @Test
    public void nestWithContent() throws Exception {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root(String text) throws SAXException {
                start(text);
                proceed();
                end();
            }
        };

        assertXMLIdentical("rule/expected69.xml", "rule/test69.xml", scanner);
    }

    /**
     * Private rule class. (Same package of {@link XMLScanner})
     */
    @Test
    public void testPrivateClass() throws Exception {
        XMLScanner scanner = new PrivateRuleScanner();
        assert scanner != null;
    }

    /**
     * @version 2010/12/13 23:04:36
     */
    private static class PrivateRuleScanner extends XMLScanner {

        @SuppressWarnings("unused")
        public static final String XMLNS = "test";
    }

    /**
     * Process terminator.
     */
    @Test(expected = RuntimeException.class)
    public void terminator() throws Exception {
        Terminator scanner = new Terminator();
        I.parse(locateSource("rule/terminator.xml"), scanner);

        assert scanner.count == 0;
    }

    /**
     * Test invalid rule. Using unknown namespace prefix.
     */
    @Test
    public void testInvalidRule1() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "unknown:namespace")
            public void rule(Attributes atts) throws SAXException {
                // this method will be ignored
            }
        };
    }

    /**
     * Test invalid rule. Using invalid parameter.
     */
    @Test
    public void testInvalidRule2() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(int type) throws SAXException {
            }
        };
    }

    /**
     * Test invalid rule. Using invalid parameter.
     */
    @Test
    public void testInvalidRule3() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(String contents, boolean test) throws SAXException {
            }
        };
    }

    /**
     * Test invalid rule. Using invalid parameter.
     */
    @Test
    public void testInvalidRule4() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(String contents, Attributes atts, boolean test) throws SAXException {
            }
        };
    }

    /**
     * Test invalid rule. Using invalid order.
     */
    @Test
    public void testInvalidRule5() throws Exception {
        new XMLScanner() {

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void rule(Attributes atts, String contents) throws SAXException {
            }
        };
    }

    /**
     * @version 2010/12/13 23:04:30
     */
    private static class WithoutPursue extends XMLScanner {

        @Rule(match = "root")
        @SuppressWarnings("unused")
        public void root(Attributes atts) throws Exception {
            start("added", atts);
            end();
        }
    }

    /**
     * @version 2010/12/13 23:04:06
     */
    private static class WithPursue extends XMLScanner {

        @Rule(match = "root")
        @SuppressWarnings("unused")
        public void root(Attributes atts) throws Exception {
            start("added", atts);
            proceed();
            end();
        }
    }

    /**
     * @version 2010/12/13 23:04:10
     */
    private static class UseContents extends XMLScanner {

        @Rule(match = "root")
        @SuppressWarnings("unused")
        public void root(String contents, Attributes atts) throws Exception {
            start(contents, atts);
            end();
        }
    }

    /**
     * @version 2010/12/13 23:04:15
     */
    private static class UseContentsWithPursue extends XMLScanner {

        @Rule(match = "root")
        @SuppressWarnings("unused")
        public void root(String contents, AttributesImpl atts) throws Exception {
            atts.addAttribute("", "title", "title", "CDATA", contents);

            start("root", atts);
            proceed();
            end();
        }
    }

    /**
     * @version 2010/12/13 23:04:20
     */
    private static class ExcludeResultPrefixes extends XMLScanner {

        @SuppressWarnings("unused")
        public static final String XMLNS$EXCLUDED = "excluded";

        @Rule(match = "excluded:item")
        @SuppressWarnings("unused")
        public void item() throws SAXException {
            start("item", new AttributesImpl());
            end();
        }
    }

    /**
     * @version 2010/12/13 23:04:24
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
}
