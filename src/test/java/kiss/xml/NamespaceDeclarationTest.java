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

import static ezunit.Ezunit.*;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import kiss.I;

/**
 * @version 2011/03/22 17:24:09
 */
public class NamespaceDeclarationTest {

    /**
     * Default namespace declaration.
     */
    @Test
    public void testDefaultNamaspaceDeclaration() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS = "default";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 1 == counter.start;
        assert 1 == counter.end;
    }

    /**
     * Named namespace declaration.
     */
    @Test
    public void testNormalNamaspaceDeclaration() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS_NAME = "uri";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 1 == counter.start;
        assert 1 == counter.end;
    }

    /**
     * Default and named namespace declarations.
     */
    @Test
    public void testDefaultAndNormalNamaspaceDeclaration() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS = "default";

            public static final String XMLNS_NAME = "uri";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Multiple named namespace declarations.
     */
    @Test
    public void testMultipleNamaspaceDeclarations() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS_ONE = "first";

            public static final String XMLNS_TWO = "second";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Exclude result prefix at default namespace declaration.
     */
    @Test
    public void testExcludeResultPrefixOnDefault() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final transient String XMLNS = "none";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 0 == counter.start;
        assert 0 == counter.end;
    }

    /**
     * Exclude result prefix at named namespace declaration.
     */
    @Test
    public void testExcludeResultPrefixOnNormal() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final transient String XMLNS_EXCLUDE = "none";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 0 == counter.start;
        assert 0 == counter.end;
    }

    /**
     * Exclude result prefix.
     */
    @Test
    public void testExcludeResultPrefixOnDefaultAndNormal() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final transient String XMLNS = "none1";

            public static final transient String XMLNS_EXCLUDE = "none2";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 0 == counter.start;
        assert 0 == counter.end;
    }

    /**
     * Exclude result prefix.
     */
    @Test
    public void testMultipleExcludeResultPrefix() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final transient String XMLNS_ONE = "none1";

            public static final transient String XMLNS_TWO = "none2";

            public static final transient String XMLNS_THREE = "none3";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 0 == counter.start;
        assert 0 == counter.end;
    }

    /**
     * Exclude result prefix.
     */
    @Test
    public void testExcludeResultPrefixMustAffectSameURI() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS_ONE = "same";

            public static final String XMLNS_TWO = "same";

            public static final transient String XMLNS_THREE = "same";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 0 == counter.start;
        assert 0 == counter.end;
    }

    /**
     * Mixed pattern.
     */
    @Test
    public void testMixedNamespaceDeclarations() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner scanner = new XMLScanner() {

            public static final String XMLNS = "default";

            public static final String XMLNS_ONE = "first";

            public static final transient String XMLNS_TWO = "second";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), scanner, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Pipelined.
     */
    @Test
    public void testPipelinedDifferentNamaspaceDeclarationOnNormal() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner first = new XMLScanner() {

            public static final String XMLNS_ONE = "first";
        };
        @SuppressWarnings("unused")
        XMLScanner second = new XMLScanner() {

            public static final String XMLNS_TWO = "second";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), first, second, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Pipelined.
     */
    @Test
    public void testPipelinedSameNamaspaceDeclarationOnNormal() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner first = new XMLScanner() {

            public static final String XMLNS_ONE = "first";
        };
        @SuppressWarnings("unused")
        XMLScanner second = new XMLScanner() {

            public static final String XMLNS_ONE = "first";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), first, second, counter);

        // assertion
        assert 1 == counter.start;
        assert 1 == counter.end;
    }

    /**
     * Pipelined.
     */
    @Test
    public void testPipelinedSameURINamaspaceDeclarationOnNormal() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner first = new XMLScanner() {

            public static final String XMLNS_ONE = "first";
        };
        @SuppressWarnings("unused")
        XMLScanner second = new XMLScanner() {

            public static final String XMLNS_TWO = "first";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), first, second, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Pipelined.
     */
    @Test
    public void testPipelinedSamePrefixNamaspaceDeclarationOnNormal() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner first = new XMLScanner() {

            public static final String XMLNS_ONE = "first";
        };
        @SuppressWarnings("unused")
        XMLScanner second = new XMLScanner() {

            public static final String XMLNS_ONE = "second";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), first, second, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Pipelined.
     */
    @Test
    public void testPipelinedDifferentNamaspaceDeclarationOnDefault() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner first = new XMLScanner() {

            public static final String XMLNS = "first";
        };
        @SuppressWarnings("unused")
        XMLScanner second = new XMLScanner() {

            public static final String XMLNS = "second";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), first, second, counter);

        // assertion
        assert 2 == counter.start;
        assert 2 == counter.end;
    }

    /**
     * Pipelined.
     */
    @Test
    public void testPipelinedSameNamaspaceDeclarationOnDefault() throws Exception {
        // build pipeline components
        @SuppressWarnings("unused")
        XMLScanner first = new XMLScanner() {

            public static final String XMLNS = "same";
        };
        @SuppressWarnings("unused")
        XMLScanner second = new XMLScanner() {

            public static final String XMLNS = "same";
        };
        Counter counter = new Counter();

        // parse
        I.parse(locateSource("dummy.xml"), first, second, counter);

        // assertion
        assert 1 == counter.start;
        assert 1 == counter.end;
    }

    /**
     * @version 2011/03/22 17:23:59
     */
    private static class Counter extends XMLFilterImpl {

        private int start = 0;

        private int end = 0;

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#startPrefixMapping(java.lang.String,
         *      java.lang.String)
         */
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            start++;
            super.startPrefixMapping(prefix, uri);
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#endPrefixMapping(java.lang.String)
         */
        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            end++;
            super.endPrefixMapping(prefix);
        }
    }
}
