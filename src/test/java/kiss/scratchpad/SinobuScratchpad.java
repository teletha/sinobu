/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import static kiss.I.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.SAXParserFactory;

import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import kiss.scratchpad.ExtensionKeyTest.Extension;
import kiss.scratchpad.ExtensionKeyTest.ExtensionKeyProvider;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * @version 2008/06/18 8:42:37
 */
@Manageable(lifestyle = Singleton.class)
public class SinobuScratchpad {

    /** The sax parser factory for reuse. */
    private static final SAXParserFactory sax = SAXParserFactory.newInstance();

    static {
        try {
            // configure sax parser
            sax.setNamespaceAware(true);
            sax.setXIncludeAware(true);
            sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // sax.setFeature("http://xml.org/sax/features/string-interning", true);
            // sax.setFeature("http://xml.org/sax/features/external-general-entities", false);
            // sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            // sax.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            // sax.setFeature("http://xml.org/sax/features/xmlns-uris", true);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Type inference create method. It's cooool!
     * </p>
     * 
     * <pre>
     * Person person = I.create();
     * </pre>
     * 
     * @param <M>
     * @param m
     * @return A created object.
     */
    public static <M> M create(M... m) {
        return (M) I.make(m.getClass().getComponentType());
    }

    /**
     * @param <E>
     * @param extensionPoint
     * @param key
     * @return An extension.
     */
    public static <E extends Extension> E find(Class<E> extensionPoint, Class<?> key) {
        return null;
    }

    /**
     * @param <E>
     * @param extensionPoint
     * @param key
     * @return An extension.
     */
    public static <E extends Extension<? extends ExtensionKeyProvider<? super K>>, K> E find(Class<E> extensionPoint, K key) {
        return null;
    }

    /**
     * Copy the specified object deeply.
     * 
     * @param <M>
     * @param model
     * @return A clone object.
     */
    public static <M> M xerox(M model) {
        return model;
    }

    /**
     * <p>
     * Parse the specified xml {@link Path} using the specified sequence of {@link XMLFilter} . The
     * application can use this method to instruct the XML reader to begin parsing an XML document
     * from the specified path.
     * </p>
     * <p>
     * Sinobu use the {@link XMLReader} which has the following features.
     * </p>
     * <ul>
     * <li>Support XML namespaces.</li>
     * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude) Version
     * 1.0</a>.</li>
     * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
     * <li><em>Not</em> support external DTD completely (parser doesn't even access DTD, using
     * "http://apache.org/xml/features/nonvalidating/load-external-dtd" feature).</li>
     * </ul>
     * 
     * @param source A path to xml source.
     * @param filters A list of filters to parse a sax event. This may be <code>null</code>.
     * @throws NullPointerException If the specified source is <code>null</code>. If one of the
     *             specified filter is <code>null</code>.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream or character
     *             stream supplied by the application.
     */
    public static void parse(Path source, XMLFilter... filters) {
        try {
            InputSource input = new InputSource(Files.newBufferedReader(source, $encoding));
            input.setPublicId(source.toString());

            parse(input, filters);
        } catch (Exception e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Parse the specified xml {@link InputSource} using the specified sequence of {@link XMLFilter}
     * . The application can use this method to instruct the XML reader to begin parsing an XML
     * document from any valid input source (a character stream, a byte stream, or a URI).
     * </p>
     * <p>
     * Sinobu use the {@link XMLReader} which has the following features.
     * </p>
     * <ul>
     * <li>Support XML namespaces.</li>
     * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude) Version
     * 1.0</a>.</li>
     * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
     * <li><em>Not</em> support external DTD completely (parser doesn't even access DTD, using
     * "http://apache.org/xml/features/nonvalidating/load-external-dtd" feature).</li>
     * </ul>
     * 
     * @param source A xml source.
     * @param filters A list of filters to parse a sax event. This may be <code>null</code>.
     * @throws NullPointerException If the specified source is <code>null</code>. If one of the
     *             specified filter is <code>null</code>.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream or character
     *             stream supplied by the application.
     */
    public static void parse(InputSource source, XMLFilter... filters) {
        try {
            // create new xml reader
            XMLReader reader = sax.newSAXParser().getXMLReader();

            // chain filters if needed
            for (int i = 0; i < filters.length; i++) {
                // find the root filter of the current multilayer filter
                XMLFilter filter = filters[i];

                while (filter.getParent() instanceof XMLFilter) {
                    filter = (XMLFilter) filter.getParent();
                }

                // the root filter makes previous filter as parent xml reader
                filter.setParent(reader);

                if (filter instanceof LexicalHandler) {
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", filter);
                }

                // current filter is a xml reader in next step
                reader = filters[i];
            }

            // start parsing
            reader.parse(source);
        } catch (Exception e) {
            // We must throw the checked exception quietly and pass the original exception instead
            // of wrapped exception.
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Query and calculate the object graph by using the XPath engine which is provided by J2SE.
     * </p>
     * 
     * <pre>
     * School school = I.create(School.class);
     * List&lt;Student&gt; list = new ArrayList();
     * school.setStudents(list);
     * 
     * Student person = I.create(Student.class);
     * person.setAge(1);
     * list.add(person);
     * 
     * person = I.create(Student.class);
     * person.setAge(2);
     * list.add(person);
     * 
     * person = I.create(Student.class);
     * person.setAge(3);
     * list.add(person);
     * 
     * int sum = I.xpath(school, &quot;sum(/School/students/item/@age)&quot;);
     * assertEquals(6, sum);
     * </pre>
     * 
     * @param model
     * @param xpath
     */
    public static void xpath(Object model, String xpath) {
        // create writer
        // SAXBuilder converter = new SAXBuilder();
        // XMLReader reader = XMLUtil.getXMLReader(converter);
        // ContentHandler handler = reader.getContentHandler();

        // xml start
        // handler.startDocument();
        // handler.startPrefixMapping("ez", "http://ez.bean/");

        // ModelWalker walker = new ModelWalker(model);
        // walker.addListener(new ConfigurationWriter(handler));
        // walker.traverse();

        // xml end
        // handler.endDocument();

        // XPath path = XPathFactory.newInstance().newXPath();
        // return path.evaluate(xpath, converter.getDocument());
    }
}
