/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.xml;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import ezbean.I;

/**
 * @version 2010/02/05 1:31:48
 */
public class XMLWriterTest {

    /**
     * Empty element.
     */
    @Test
    public void testEmptyElement() {
        assertBinaryXML("format/expected01.xml", "format/test01.xml");
    }

    /**
     * Text contents.
     */
    @Test
    public void testTextContents() {
        assertBinaryXML("format/expected02.xml", "format/test02.xml");
    }

    /**
     * Child element.
     */
    @Test
    public void testChildElement() {
        assertBinaryXML("format/expected03.xml", "format/test03.xml");
    }

    /**
     * Child element and text contents.
     */
    @Test
    public void testChildElementAndTextContents() {
        assertBinaryXML("format/expected04.xml", "format/test04.xml");
    }

    /**
     * Custom character type element.
     */
    @Test
    public void testMixedContents1() {
        assertBinaryXML("format/expected10.xml", "format/test10.xml");
    }

    /**
     * Custom character type element.
     */
    @Test
    public void testMixedContents2() {
        assertBinaryXML("format/expected11.xml", "format/test11.xml");
    }

    /**
     * Custom character type element.
     */
    @Test
    public void testMixedContents3() {
        assertBinaryXML("format/expected10b.xml", "format/test10.xml", EM.class);
    }

    /**
     * Custom character type element.
     */
    @Test
    public void testMixedContents4() {
        assertBinaryXML("format/expected11.xml", "format/test11.xml", EM.class);
    }

    /**
     * Complex contents.
     */
    @Test
    public void testComplexContents() {
        assertBinaryXML("format/expected06.xml", "format/test06.xml", AsCharacter.class);
    }

    /**
     * Test namespace declaration.
     */
    @Test
    public void testNamespace1() {
        assertBinaryXML("format/expectedNamespace01.xml", "format/testNamespace01.xml");
    }

    /**
     * Test namespace declaration.
     */
    @Test
    public void testNamespace2() {
        assertBinaryXML("format/expectedNamespace02.xml", "format/testNamespace02.xml");
    }

    /**
     * Test namespace declaration.
     */
    @Test
    public void testNamespace3() {
        assertBinaryXML("format/expectedNamespace03.xml", "format/testNamespace03.xml");
    }

    /**
     * Test namespace at attribute.
     */
    @Test
    public void testNamespace4() {
        assertBinaryXML("format/expectedNamespace04.xml", "format/testNamespace04.xml");
    }

    /**
     * Test order of namespace declaration and attributes.
     */
    @Test
    public void testNamespace5() {
        assertBinaryXML("format/expectedNamespace05.xml", "format/testNamespace05.xml");
    }

    /**
     * Test prefixed namespace at attribute.
     */
    @Test
    public void testNamespace6() {
        assertBinaryXML("format/expectedNamespace06.xml", "format/testNamespace06.xml");
    }

    /**
     * Test entity.
     */
    @Test
    public void testEntity1() {
        assertBinaryXML("format/expected15.xml", "format/test15.xml");
    }

    /**
     * Test entity.
     */
    @Test
    public void testEntity2() {
        assertBinaryXML("format/expected16.xml", "format/test16.xml");
    }

    /**
     * Processing instruction.
     */
    @Test
    public void testProcessingInstruction() {
        assertBinaryXML("format/expected20.xml", "format/test20.xml");
    }

    @Test
    public void publicDTD() {
        assertBinaryXML("format/expectedPublicDTD.xml", "format/testPublicDTD.xml");
    }

    @Test
    public void systemDTD() {
        assertBinaryXML("format/expectedSystemDTD.xml", "format/testSYstemDTD.xml");
    }

    /**
     * With XMLScanner.
     */
    @Test
    public void testXMLScannerOutput1() {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS_NS = "uri";

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root() throws SAXException {
                start("ns:root");
                end();
            }
        };
        assertBinaryXML("format/expected21.xml", "format/test21.xml", scanner);
    }

    /**
     * With XMLScanner.
     */
    @Test
    public void testXMLScannerOutput2() {
        XMLScanner scanner = new XMLScanner() {

            @SuppressWarnings("unused")
            public static final String XMLNS = "uri";

            @SuppressWarnings("unused")
            @Rule(match = "root")
            public void root() throws SAXException {
                start("item", "name", "value");
                end();
            }
        };
        assertBinaryXML("format/expected22.xml", "format/test22.xml", scanner);
    }

    @Test
    public void testXMLWriter() throws SAXException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        XMLWriter writer = new XMLWriter(new OutputStreamWriter(output));
        writer.startDocument();
        writer.start("root");
        writer.end();
        writer.endDocument();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", new String(output.toByteArray()).replaceAll("\\r\\n", ""));
    }

    @Test
    public void omitXMLDeclaration() throws SAXException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        XMLWriter writer = new OmitXMLDeclaration(new OutputStreamWriter(output));
        writer.setContentHandler(writer);

        writer.startDocument();
        writer.start("root");
        writer.end();
        writer.endDocument();

        assertEquals("<root/>", new String(output.toByteArray()));
    }

    /**
     * Helper method to assert the given xmls in binary level.
     * 
     * @param expectedXMLFilePath
     * @param testedXMLFilePath
     */
    private void assertBinaryXML(String expectedXMLFilePath, String testedXMLFilePath) {
        assertBinaryXML(expectedXMLFilePath, testedXMLFilePath, XMLWriter.class);
    }

    /**
     * Helper method to assert the given xmls in binary level.
     * 
     * @param expectedXMLFilePath
     * @param testedXMLFilePath
     */
    private void assertBinaryXML(String expectedXMLFilePath, String testedXMLFilePath, XMLFilter... filters) {
        assertBinaryXML(expectedXMLFilePath, testedXMLFilePath, XMLWriter.class, filters);
    }

    /**
     * Helper method to assert the given xmls in binary level.
     * 
     * @param expectedXMLFilePath
     * @param testedXMLFilePath
     */
    private void assertBinaryXML(String expectedXMLFilePath, String testedXMLFilePath, Class<? extends XMLWriter> clazz) {
        try {
            // tested
            StringBuilder testedOutput = new StringBuilder();
            XMLWriter formatter = clazz.getConstructor(Appendable.class).newInstance(testedOutput);
            I.parse(locateSource(testedXMLFilePath), formatter);
            String[] testedResult = line(testedOutput);

            // expected
            ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
            I.copy(Files.newInputStream(locate(expectedXMLFilePath)), expectedOutput);

            String[] expectedResult = line(expectedOutput.toByteArray());

            // assert
            if (expectedResult.length != testedResult.length) {
                System.out.println("expected :");

                for (String line : expectedResult) {
                    System.out.println(line);
                }

                System.out.println("");
                System.out.println("");
                System.out.println("tested :");

                for (String line : testedResult) {
                    System.out.println(line);
                }

                assertEquals(Arrays.toString(expectedResult), Arrays.toString(testedResult));
                assertEquals(expectedResult.length, testedResult.length);
            }

            for (int i = 0; i < expectedResult.length; i++) {
                assertEquals("At line " + i + ".", expectedResult[i], testedResult[i]);
            }
        } catch (Exception e) {
            fail(e.getMessage());
            throw new Error(e);
        }
    }

    /**
     * Helper method to assert the given xmls in binary level.
     * 
     * @param expectedXMLFilePath
     * @param testedXMLFilePath
     */
    private void assertBinaryXML(String expectedXMLFilePath, String testedXMLFilePath, Class<? extends XMLWriter> clazz, XMLFilter... filters) {
        try {
            // tested
            StringBuilder testedOutput = new StringBuilder();
            filters = Arrays.copyOf(filters, filters.length + 1);
            filters[filters.length - 1] = clazz.getConstructor(Appendable.class).newInstance(testedOutput);

            I.parse(locateSource(testedXMLFilePath), filters);
            String[] testedResult = line(testedOutput);

            // expected
            ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
            I.copy(Files.newInputStream(locate(expectedXMLFilePath)), expectedOutput);
            String[] expectedResult = line(expectedOutput.toByteArray());

            // assert
            if (expectedResult.length != testedResult.length) {
                System.out.println("expected :");

                for (String line : expectedResult) {
                    System.out.println(line);
                }

                System.out.println("");
                System.out.println("");
                System.out.println("tested :");

                for (String line : testedResult) {
                    System.out.println(line);
                }

                assertEquals(Arrays.toString(expectedResult), Arrays.toString(testedResult));
                assertEquals(expectedResult.length, testedResult.length);
            }

            for (int i = 0; i < expectedResult.length; i++) {
                assertEquals("At line " + i + ".", expectedResult[i], testedResult[i]);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Divide by line.
     * 
     * @param bytes
     * @return
     */
    private String[] line(byte[] bytes) {
        return line(new String(bytes));
    }

    /**
     * Divide by line.
     * 
     * @param bytes
     * @return
     */
    private String[] line(CharSequence xml) {
        StringTokenizer tokenizer = new StringTokenizer(xml.toString(), "\n\r");

        int i = 0;
        String[] lines = new String[tokenizer.countTokens()];

        while (tokenizer.hasMoreTokens()) {
            lines[i++] = tokenizer.nextToken();
        }

        return lines;
    }

    /**
     * @version 2010/12/12 9:10:56
     */
    private static class EM extends XMLWriter {

        /**
         * Create EM instance.
         * 
         * @param out
         */
        public EM(Appendable out) {
            super(out);
        }

        /**
         * @see ezbean.xml.XMLWriter#asCharacter(java.lang.String, java.lang.String)
         */
        @Override
        protected boolean asCharacter(String uri, String local) {
            return local.equals("em");
        }

    }

    /**
     * @version 2010/12/12 9:10:52
     */
    private static class AsCharacter extends XMLWriter {

        private String[] characters = new String[] {"a", "img"};

        /**
         * Create AsCharacter instance.
         * 
         * @param out
         */
        public AsCharacter(Appendable out) {
            super(out);
        }

        /**
         * @see ezbean.XMLOut.XMLFormatter#asCharacter(java.lang.String, java.lang.String)
         */
        @Override
        protected boolean asCharacter(String namespaceURI, String localName) {
            for (String test : characters) {
                if (test.equals(localName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @version 2010/12/12 9:11:28
     */
    private static class OmitXMLDeclaration extends XMLWriter {

        /**
         * @param out
         */
        public OmitXMLDeclaration(Appendable out) {
            super(out);
        }

        /**
         * @see ezbean.xml.XMLWriter#startDocument()
         */
        @Override
        public void startDocument() {
            // omit
        }
    }
}
