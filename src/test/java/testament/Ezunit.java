/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package testament;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import kiss.I;
import kiss.xml.XMLWriter;

import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import testament.xml.XML;

/**
 * @version 2012/01/19 12:59:58
 */
public class Ezunit {

    /** The empty attribute for reuse. */
    public static final Attributes EMPTY_ATTR = new AttributesImpl();

    /**
     * <p>
     * Locate a package directory that the specified class exists.
     * </p>
     * 
     * @param clazz A class to resolve location.
     * @return A located package directory.
     * @throws NullPointerException If the class is <code>null</code>.
     */
    public static final Path locatePackage(Class clazz) {
        try {
            return Paths.get(clazz.getResource("").toURI());
        } catch (URISyntaxException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Reads all characters from a FilePath into a {@link String}, using the given character set or
     * {@link I#getEncoding()}.
     * </p>
     * 
     * @param FilePath A FilePath to read from.
     * @param charset A character set used when reading the file.
     * @return A string containing all the characters from the file.
     */
    public static final String read(Path path, Charset... charset) {
        StringBuilder builder = new StringBuilder();

        for (String line : readLines(path, charset)) {
            builder.append(line).append(File.separatorChar);
        }

        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * <p>
     * Reads the first line from a file. The line does not include line-termination characters, but
     * does include other leading and trailing whitespace.
     * </p>
     * 
     * @param FilePath A FilePath to read from
     * @param charset A character set used when writing the file. If you don't specify, Otherwise
     *            {@link I#getEncoding()}.
     * @return the first line, or null if the FilePath is empty
     * @throws IOException if an I/O error occurs
     */
    public static final String readLine(Path path, Charset... charset) {
        List<String> lines = readLines(path, option(charset, I.$encoding), false);

        return lines.size() == 0 ? "" : lines.get(0);
    }

    /**
     * <p>
     * Reads all of the lines from a file. The lines do not include line-termination characters, but
     * do include other leading and trailing whitespace.
     * </p>
     * 
     * @param FilePath A FilePath to read from
     * @param charset A character set used when writing the file. If you don't specify, Otherwise
     *            {@link I#getEncoding()}.
     * @return the first line, or null if the FilePath is empty
     * @throws IOException if an I/O error occurs
     */
    public static final List<String> readLines(Path path, Charset... charset) {
        return readLines(path, option(charset, I.$encoding), true);
    }

    /**
     * Helper method to decide option.
     * 
     * @param <T>
     * @param option
     * @param defaultValue
     * @return
     */
    private static <T> T option(T[] option, T defaultValue) {
        return option != null && option.length != 0 && option[0] != null ? option[0] : defaultValue;
    }

    /**
     * Read FilePath contents actually.
     * 
     * @param path
     * @param charset
     * @param all
     * @return
     */
    private static List<String> readLines(Path path, Charset charset, boolean all) {
        try {
            // convert to native path
            path = Paths.get(path.toString());

            List<String> lines = Files.readAllLines(path, charset);

            return all ? lines : lines.isEmpty() ? Collections.EMPTY_LIST : Collections.singletonList(lines.get(0));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    // ==================================================================== //
    // Informal Methods
    // ==================================================================== //

    /**
     * <p>
     * Locate the specified FilePath name.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @return A located {@link File}.
     */
    public static final Path locate(String filePath) {
        return locateFileFromCaller(filePath);
    }

    /**
     * <p>
     * Locate the specified FilePath name as {@link InputSource}.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @return A located {@link InputSource}.
     */
    public static final InputSource locateSource(String filePath) {
        // API definition
        return locateSource(locateFileFromCaller(filePath));
    }

    /**
     * <p>
     * Locate the specified FilePath name as {@link InputSource}.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @return A located {@link InputSource}.
     */
    public static final InputSource locateSource(Path file) {
        InputSource source = new InputSource(file.toUri().toString());
        source.setEncoding(I.$encoding.name());
        source.setPublicId(file.toString());

        // API definition
        return source;
    }

    /**
     * <p>
     * Build a xml document form the specified FilePath name.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @param filters A list of filters to transform xml.
     * @return A created {@link Document}.
     */
    public static final Document locateDOM(String filePath, XMLFilter... filters) {
        return locateDOM(locateFileFromCaller(filePath), filters);
    }

    /**
     * <p>
     * Build a xml document form the specified FilePath name.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @param filters A list of filters to transform xml.
     * @return A created {@link Document}.
     */
    public static final Document locateDOM(Path file, XMLFilter... filters) {
        // build xml pipe
        SAXBuilder builder = new SAXBuilder();

        List<XMLFilter> list = new ArrayList();
        list.addAll(Arrays.asList(filters));
        list.add(new IgnoreWhitspaceFilter());
        list.add(builder);

        // start parsing
        I.parse(file, list.toArray(new XMLFilter[list.size()]));

        // retrive result DOM document
        Document document = builder.getDocument();

        // check null
        assert document != null;

        // API definition
        return document;
    }

    /**
     * <p>
     * Locate the specified FilePath name with the context which is located by the caller class.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @return A located file.
     */
    private static Path locateFileFromCaller(String filePath) {
        Class caller = getCaller();
        URL url = caller.getResource(filePath);

        if (url == null) {
            throw new AssertionError("The resource is not found. [" + filePath + "]");
        }

        // resolve FilePath location
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Search caller testcase class.
     * </p>
     * 
     * @return A testcase class.
     */
    public static final Class getCaller() {
        // caller
        Exception e = new Exception();
        StackTraceElement[] elements = e.getStackTrace();

        for (StackTraceElement element : elements) {
            String name = element.getClassName();

            if (name.endsWith("Test")) {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException classNotFoundException) {
                    // If this exception will be thrown, it is bug of this program. So we must
                    // rethrow the wrapped error in here.
                    throw new Error(classNotFoundException);
                }
            }
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error("Testcas is not found.");
    }

    /**
     * Assert that two XML documents are similar.
     * 
     * @param expected A XML to be compared against.
     * @param tested A XML to be tested.
     */
    public static final void assertXMLEqual(Document expected, Document tested) {
        Diff diff = new Diff(expected, tested);

        if (!diff.similar()) {
            System.out.println("The given document :");
            dumpXML(tested);

            System.out.println("The expected document :");
            dumpXML(expected);

            throw new AssertionError(diff.toString());
        }
    }

    /**
     * <p>
     * Assert that two XML documents are similar.
     * </p>
     * 
     * @param expectedXMLFilePath A XML to be compared against.
     * @param testedXMLFilePath A XML to be tested.
     * @param filters A list of filters to transform xml.
     */
    public static final void assertXMLEqual(String expectedXMLFilePath, String testedXMLFilePath, XMLFilter... filters) {
        assertXMLEqual(locateDOM(expectedXMLFilePath), locateDOM(testedXMLFilePath, filters));
    }

    public static final XML xml(String path, XMLFilter... filters) {
        return new XML(locateFileFromCaller(path));
    }

    /**
     * Assert that two XML documents are identical.
     * 
     * @param expected A XML to be compared against.
     * @param tested A XML to be tested.
     */
    public static final void assertXMLIdentical(Document expected, Document tested) {
        Diff diff = new Diff(expected, tested);

        if (!diff.identical()) {
            System.out.println("The given document :");
            dumpXML(tested);

            System.out.println("The expected document :");
            dumpXML(expected);

            throw new AssertionError(diff.toString());
        }
    }

    /**
     * Assert that two XML documents are identical.
     * 
     * @param expectedXMLFilePath A XML to be compared against.
     * @param testedXMLFilePath A XML to be tested.
     * @param filters A list of filters to transform xml.
     */
    public static final void assertXMLIdentical(String expectedXMLFilePath, String testedXMLFilePath, XMLFilter... filters) {
        assertXMLIdentical(locateDOM(expectedXMLFilePath), locateDOM(testedXMLFilePath, filters));
    }

    /**
     * Assert that the XML documents has the expected value which is result of XPath evaluation.
     * 
     * @param expected A expected value.
     * @param tested A XML to be tested.
     * @param xpath A XPath expression.
     */
    public static final void assertXPathEqual(String expected, String testedXMLFilePath, String xpath) {
        assertXPathEqual(expected, locateFileFromCaller(testedXMLFilePath), xpath);
    }

    /**
     * Assert that the XML documents has the expected value which is result of XPath evaluation.
     * 
     * @param expected A expected value.
     * @param tested A XML to be tested.
     * @param xpath A XPath expression.
     */
    public static final void assertXPathEqual(String expected, Path testedXMLFile, String xpath) {
        assertXPathEqual(expected, testedXMLFile, xpath, null);
    }

    /**
     * Assert that the XML documents has the expected value which is result of XPath evaluation.
     * 
     * @param expected A expected value.
     * @param tested A XML to be tested.
     * @param xpath A XPath expression.
     */
    public static final void assertXPathEqual(String expected, Path testedXMLFile, String xpath, Map<String, String> namespaces) {
        try {
            XPath context = XPathFactory.newInstance().newXPath();
            context.setNamespaceContext(new Namespaces(namespaces));
            String result = context.evaluate(xpath, locateSource(testedXMLFile));

            assertEquals(expected, result);
        } catch (XPathExpressionException e) {
            throw new AssertionError(e.getLocalizedMessage());
        }
    }

    /**
     * <p>
     * Helper method to dump XML data to system output.
     * </p>
     * 
     * @param document A target document to dump.
     */
    public static final void dumpXML(Document document) {
        try {
            XMLWriter formatter = new XMLWriter(System.out);
            // create SAX result
            SAXResult result = new SAXResult(formatter);
            result.setLexicalHandler(formatter);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), result);

            System.out.println("");
            System.out.println("");
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @version 2011/03/23 8:07:19
     */
    private static class IgnoreWhitspaceFilter extends XMLFilterImpl {

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            for (int i = start; i < start + length; i++) {
                if (!Character.isWhitespace(ch[i])) {
                    super.characters(ch, start, length);
                    return;
                }
            }
        }
    }

    /**
     * @version 2012/01/13 11:56:03
     */
    private static class Namespaces implements NamespaceContext {

        /** The actual mapping. */
        private final Map<String, String> mapping;

        /**
         * @param mapping
         */
        private Namespaces(Map<String, String> mapping) {
            this.mapping = mapping == null ? Collections.EMPTY_MAP : mapping;
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
         */
        public String getNamespaceURI(String prefix) {
            return mapping.get(prefix);
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
         */
        public String getPrefix(String namespaceURI) {
            return null;
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
         */
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
